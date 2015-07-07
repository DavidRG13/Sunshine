package com.android.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import com.android.sunshine.app.R;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.model.OWMResponse;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.weather.WeatherFetcher;
import com.android.sunshine.app.weather.WeatherRepository;
import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private final LocationProvider locationProvider;
    private final WeatherRepository weatherRepository;
    private final ServerStatusChanger serverStatusChanger;
    private final WeatherFetcher weatherFetcher;
    private final Context context;

    @Inject
    public SyncAdapter(final LocationProvider locationProvider, final WeatherRepository weatherRepository, final ServerStatusChanger serverStatusChanger,
        final WeatherFetcher weatherFetcher, final Context context, @Named("autoInitialize") boolean autoInitialize) {
        super(context, autoInitialize);
        this.locationProvider = locationProvider;
        this.weatherRepository = weatherRepository;
        this.serverStatusChanger = serverStatusChanger;
        this.weatherFetcher = weatherFetcher;
        this.context = context;
    }

    @Override
    public void onPerformSync(final Account account, final Bundle extras, final String authority, final ContentProviderClient provider, final SyncResult syncResult) {
        final String location = locationProvider.getPostCode();

        OWMResponse response = weatherFetcher.forecastForLocation(location);

        int responseCode = Integer.parseInt(response.getCod());
        serverStatusChanger.fromResponseCode(responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            weatherRepository.saveWeatherForLocation(response, location);
        }
    }

    public void syncImmediately() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(), context.getString(R.string.content_authority), bundle);
    }

    public void initializeSyncAdapter() {
        getSyncAccount();
    }

    private Account getSyncAccount() {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount);
        }
        return newAccount;
    }

    private void onAccountCreated(final Account newAccount) {
        configurePeriodicSync(newAccount, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately();
    }

    private void configurePeriodicSync(final Account newAccount, final int syncInterval, final int flexTime) {
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime).setSyncAdapter(newAccount, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(newAccount, authority, new Bundle(), syncInterval);
        }
    }
}

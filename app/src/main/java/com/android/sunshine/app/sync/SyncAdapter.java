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
import com.android.sunshine.app.weather.OWM;
import com.android.sunshine.app.weather.WeatherRepository;
import java.net.HttpURLConnection;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private final LocationProvider locationProvider;
    private final WeatherRepository weatherRepository;
    private final ServerStatusChanger serverStatusChanger;

    public SyncAdapter(final LocationProvider locationProvider, final WeatherRepository weatherRepository, final ServerStatusChanger serverStatusChanger, Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.locationProvider = locationProvider;
        this.weatherRepository = weatherRepository;
        this.serverStatusChanger = serverStatusChanger;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final String location = locationProvider.getLocation();

        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(OWM.API_URL)
            .setConverter(new JacksonConverter())
            .build();

        OWM weather = restAdapter.create(OWM.class);
        OWMResponse response = weather.fetch(location, "json", "metric", "14");

        int responseCode = Integer.parseInt(response.getCod());
        serverStatusChanger.fromResponseCode(responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            weatherRepository.saveWeatherForLocation(response, location);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }
}
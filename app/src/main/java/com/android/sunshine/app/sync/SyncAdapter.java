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
import com.android.sunshine.app.repository.ForecastRepository;
import com.android.sunshine.app.repository.PreferenceRepository;
import com.android.sunshine.app.utils.Weather;
import com.android.sunshine.app.utils.WeatherJsonParser;
import java.util.ArrayList;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private final PreferenceRepository preferenceRepository;
    private final WeatherJsonParser weatherJsonParser;
    private final ForecastRepository forecastRepository;
    private final WeatherDataSource weatherDataSource;
    private final Notifier notifier;

    public SyncAdapter(final Context context, final boolean autoInitialize, final PreferenceRepository preferenceRepository, final WeatherJsonParser weatherJsonParser,
            final ForecastRepository forecastRepository, final WeatherDataSource weatherDataSource, final Notifier notifier) {
        super(context, autoInitialize);
        this.preferenceRepository = preferenceRepository;
        this.weatherJsonParser = weatherJsonParser;
        this.forecastRepository = forecastRepository;
        this.weatherDataSource = weatherDataSource;
        this.notifier = notifier;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final String locationSettings = preferenceRepository.getLocation();
        String jsonResponse = weatherDataSource.getForecastFor(locationSettings);
        long locationId = forecastRepository.addLocation(weatherJsonParser.parseLocation(jsonResponse, locationSettings));
        ArrayList<Weather> weathers = weatherJsonParser.parseWeatherDataFromJson(jsonResponse, locationId);
        forecastRepository.saveWeathers(weathers);
        notifier.notifyWeather();
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
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).build();
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
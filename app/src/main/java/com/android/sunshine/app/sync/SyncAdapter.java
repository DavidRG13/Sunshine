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
import com.android.sunshine.app.weather.WeatherRepository;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private final WeatherRepository weatherRepository;

    @Inject
    public SyncAdapter(final WeatherRepository weatherRepository, final Context context, @Named("autoInitialize") boolean autoInitialize) {
        super(context, autoInitialize);
        this.weatherRepository = weatherRepository;
        getSyncAccount();
    }

    @Override
    public void onPerformSync(final Account account, final Bundle extras, final String authority, final ContentProviderClient provider, final SyncResult syncResult) {
        weatherRepository.syncImmediately();
    }

    public void syncImmediately() {
        weatherRepository.syncImmediately();
    }

    private Account getSyncAccount() {
        AccountManager accountManager = (AccountManager) getContext().getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(getContext().getString(R.string.app_name), getContext().getString(R.string.sync_account_type));

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
        ContentResolver.setSyncAutomatically(newAccount, getContext().getString(R.string.content_authority), true);
        syncImmediately();
    }

    private void configurePeriodicSync(final Account newAccount, final int syncInterval, final int flexTime) {
        String authority = getContext().getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime).setSyncAdapter(newAccount, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(newAccount, authority, new Bundle(), syncInterval);
        }
    }
}

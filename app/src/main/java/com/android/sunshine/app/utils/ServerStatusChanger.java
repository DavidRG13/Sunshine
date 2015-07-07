package com.android.sunshine.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import com.android.sunshine.app.R;
import com.android.sunshine.app.sync.ServerStatus;
import javax.inject.Inject;

import static com.android.sunshine.app.sync.ServerStatus.NO_NETWORK_AVAILABLE;
import static com.android.sunshine.app.sync.ServerStatus.SERVER_STATUS_OK;
import static com.android.sunshine.app.sync.ServerStatus.SERVER_STATUS_UNKNOWN;

public class ServerStatusChanger {

    private Context context;

    @Inject
    public ServerStatusChanger(final Context context) {
        this.context = context;
    }

    public void fromResponseCode(final int responseCode) {
        setServerStatus(ServerStatus.fromResponseCode(responseCode));
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public ServerStatus getServerStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String string = preferences.getString(context.getString(R.string.prefs_server_status), SERVER_STATUS_UNKNOWN.toString());
        ServerStatus serverStatus = ServerStatus.valueOf(string);
        if (serverStatus == SERVER_STATUS_OK && !isNetworkAvailable()) {
            return NO_NETWORK_AVAILABLE;
        }
        return serverStatus;
    }

    public void resetServerStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.prefs_server_status), SERVER_STATUS_UNKNOWN.toString());
        editor.apply();
    }

    private void setServerStatus(final ServerStatus serverStatus) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.prefs_server_status), serverStatus.toString());
        editor.apply();
    }
}

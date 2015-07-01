package com.android.sunshine.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.R;
import com.android.sunshine.app.sync.ServerStatus;
import java.net.HttpURLConnection;

public class ServerStatusChanger {

    private Context context;

    public ServerStatusChanger(final Context context) {
        this.context = context;
    }

    public void fromResponseCode(final int responseCode) {
        if (responseCode == HttpURLConnection.HTTP_OK) {
            setServerStatus(ServerStatus.SERVER_STATUS_OK);
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            setServerStatus(ServerStatus.SERVER_STATUS_LOCATION_INVALID);
        } else {
            setServerStatus(ServerStatus.SERVER_STATUS_DOWN);
        }
    }

    private void setServerStatus(final @ServerStatus int serverStatus) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.prefs_server_status), serverStatus);
        editor.apply();
    }
}

package com.android.sunshine.app.utils;

import com.android.sunshine.app.R;
import java.net.HttpURLConnection;

public enum ServerStatus {

    SERVER_STATUS_OK(R.string.ok),
    SERVER_STATUS_DOWN(R.string.server_down),
    SERVER_STATUS_INVALID(R.string.server_error),
    SERVER_STATUS_UNKNOWN(R.string.noWeatherInfoAvailable),
    SERVER_STATUS_LOCATION_INVALID(R.string.invalid_location),
    NO_NETWORK_AVAILABLE(R.string.noWeatherInfoAvailableNoNetwork);

    private final int messageResource;

    ServerStatus(final int messageResource) {
        this.messageResource = messageResource;
    }

    public static ServerStatus fromResponseCode(final int responseCode) {
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return SERVER_STATUS_OK;
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            return SERVER_STATUS_LOCATION_INVALID;
        } else {
            return ServerStatus.SERVER_STATUS_DOWN;
        }
    }

    public int getMessageResource() {
        return messageResource;
    }
}

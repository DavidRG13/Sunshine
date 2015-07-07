package com.android.sunshine.app.utils;

import com.android.sunshine.app.sync.ServerStatus;

public interface ServerStatusListener {

    void onServerStatusChanged(ServerStatus status);
}

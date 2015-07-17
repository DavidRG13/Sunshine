package com.android.sunshine.app.utils;

import android.test.AndroidTestCase;
import java.net.HttpURLConnection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ServerStatusChangerTest extends AndroidTestCase{

    public void testNotifyOnStatusChanged() {
        ServerStatusChanger serverStatusChanger = new ServerStatusChanger(mContext);
        ServerStatusListener statusListener = mock(ServerStatusListener.class);
        serverStatusChanger.addListener(statusListener);

        serverStatusChanger.fromResponseCode(HttpURLConnection.HTTP_OK);

        verify(statusListener).onServerStatusChanged(ServerStatus.SERVER_STATUS_OK);
    }

    public void testStoreTheCorrectStatus() {
        ServerStatusChanger serverStatusChanger = new ServerStatusChanger(mContext);

        serverStatusChanger.fromResponseCode(HttpURLConnection.HTTP_OK);

        assertEquals(ServerStatus.SERVER_STATUS_OK, serverStatusChanger.getServerStatus());
    }

    public void testOnResetShouldChangeTheStateToUnknown() {
        ServerStatusChanger serverStatusChanger = new ServerStatusChanger(mContext);

        serverStatusChanger.resetServerStatus();

        assertEquals(ServerStatus.SERVER_STATUS_UNKNOWN, serverStatusChanger.getServerStatus());
    }
}
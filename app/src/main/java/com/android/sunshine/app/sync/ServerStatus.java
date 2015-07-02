package com.android.sunshine.app.sync;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({ ServerStatus.SERVER_STATUS_OK, ServerStatus.SERVER_STATUS_DOWN, ServerStatus.SERVER_STATUS_INVALID,
    ServerStatus.SERVER_STATUS_UNKNOWN, ServerStatus.SERVER_STATUS_LOCATION_INVALID })
public @interface ServerStatus {

    int SERVER_STATUS_OK = 0;
    int SERVER_STATUS_DOWN = 1;
    int SERVER_STATUS_INVALID = 2;
    int SERVER_STATUS_UNKNOWN = 3;
    int SERVER_STATUS_LOCATION_INVALID = 4;
}

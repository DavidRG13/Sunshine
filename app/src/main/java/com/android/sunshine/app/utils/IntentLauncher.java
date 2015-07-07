package com.android.sunshine.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.activities.SettingsActivity;
import com.android.sunshine.app.location.LatLong;
import javax.inject.Inject;

public class IntentLauncher {

    @Inject
    public IntentLauncher() {
    }

    public void displayMapWithLocation(final Context context, final LatLong latLong) {
        Uri geoLocation = Uri.parse("geo:" + latLong.getLatitude() + "," + latLong.getLongitude());

        System.out.println("geoLocation = " + geoLocation);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        context.startActivity(intent);
    }

    public void launchSettingsActivity(final Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    public void transitionToDetails(final Activity activity, final long date, final View sharedView) {
        final Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(DetailActivity.DATE_KEY, date);
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
            new Pair<>(sharedView, activity.getString(R.string.detail_icon_transition_name)));
        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
    }
}

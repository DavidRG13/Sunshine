package com.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.android.sunshine.app.App;
import com.android.sunshine.app.R;
import com.android.sunshine.app.utils.Navigator;
import com.android.sunshine.app.weather.WeatherRepository;
import java.util.ArrayList;
import javax.inject.Inject;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    @Inject
    WeatherRepository weatherRepository;

    @Inject
    Navigator navigator;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        ((App) getApplication()).getComponent().inject(this);
        return new RemoteViewsFactory() {
            private ArrayList<ForecastDetailWidget> forecasts;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                final long identityToken = Binder.clearCallingIdentity();
                forecasts = weatherRepository.getForecastForDetailWidget();
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
            }

            @Override
            public int getCount() {
                return forecasts == null ? 0 : forecasts.size();
            }

            @Override
            public RemoteViews getViewAt(final int position) {
                if (position == AdapterView.INVALID_POSITION
                    || forecasts == null || forecasts.isEmpty()) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
                ForecastDetailWidget forecast = forecasts.get(position);
                views.setImageViewResource(R.id.widget_icon, forecast.getIconResourceId());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, forecast.getDescription());
                }
                views.setTextViewText(R.id.widget_date, forecast.getDate());
                views.setTextViewText(R.id.widget_description, forecast.getDescription());
                views.setTextViewText(R.id.widget_high_temperature, forecast.getMaxTemp());
                views.setTextViewText(R.id.widget_low_temperature, forecast.getMinTemp());

                views.setOnClickFillInIntent(R.id.widget_list_item, navigator.displayDetailsWith(forecast.getPostCode(), forecast.getDateInMillis()));
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(final RemoteViews views, final String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(final int position) {
                return forecasts.get(position).getWeatherId();
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

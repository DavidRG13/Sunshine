package com.android.sunshine.app.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherDetails implements Parcelable {

    public static final WeatherDetails INVALID_OBJECT = new WeatherDetails(-1, "", "", "", "", "", "", "");

    public static final Parcelable.Creator<WeatherDetails> CREATOR = new Parcelable.Creator<WeatherDetails>() {
        public WeatherDetails createFromParcel(Parcel in) {
            return new WeatherDetails(in);
        }

        public WeatherDetails[] newArray(int size) {
            return new WeatherDetails[size];
        }
    };

    private int iconResourceId;
    private String description;
    private String date;
    private String wind;
    private String pressure;
    private String humidity;
    private String max;
    private String min;

    public WeatherDetails(final int iconResourceId, final String description, final String date, final String wind, final String pressure, final String humidity, final String max, final String min) {
        this.iconResourceId = iconResourceId;
        this.description = description;
        this.date = date;
        this.wind = wind;
        this.pressure = pressure;
        this.humidity = humidity;
        this.max = max;
        this.min = min;
    }

    public WeatherDetails(final Parcel in) {
        this.iconResourceId = in.readInt();
        this.description = in.readString();
        this.date = in.readString();
        this.wind = in.readString();
        this.pressure = in.readString();
        this.humidity = in.readString();
        this.max = in.readString();
        this.min = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(iconResourceId);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeString(wind);
        dest.writeString(pressure);
        dest.writeString(humidity);
        dest.writeString(max);
        dest.writeString(min);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getWind() {
        return wind;
    }

    public String getPressure() {
        return pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getMax() {
        return max;
    }

    public String getMin() {
        return min;
    }
}

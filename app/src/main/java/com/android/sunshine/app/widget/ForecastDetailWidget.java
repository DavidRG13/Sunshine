package com.android.sunshine.app.widget;

public class ForecastDetailWidget {

    private int weatherId;
    private final int iconResourceId;
    private final String description;
    private long dateInMillis;
    private final String date;
    private final String maxTemp;
    private final String minTemp;
    private String postCode;

    public ForecastDetailWidget(final int weatherId, final int iconResourceId, final String description, final long dateInMillis, final String date, final String maxTemp, final String minTemp, final String postCode) {
        this.weatherId = weatherId;
        this.iconResourceId = iconResourceId;
        this.description = description;
        this.dateInMillis = dateInMillis;
        this.date = date;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.postCode = postCode;
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

    public String getMaxTemp() {
        return maxTemp;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public long getDateInMillis() {
        return dateInMillis;
    }

    public String getPostCode() {
        return postCode;
    }
}

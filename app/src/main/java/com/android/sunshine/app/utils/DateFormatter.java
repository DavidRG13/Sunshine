package com.android.sunshine.app.utils;

import android.content.Context;
import com.android.sunshine.app.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateFormatter {

    private final String todayString;
    private final String tomorrowString;

    public DateFormatter(final String todayString, final String tomorrowString) {
        this.todayString = todayString;
        this.tomorrowString = tomorrowString;
    }

    public String getFriendlyDay(final long dateInMillis, final boolean displayLongToday) {
        int inputDay = getDayOfYearFor(dateInMillis);
        int today = getDayOfYearForToday();

        if (displayLongToday && inputDay == today) {
            return todayString + ", " + getFormattedMonthDay(dateInMillis);
        } else if (inputDay < today + 7) {
            return getDayName(dateInMillis);
        } else {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.getDefault());
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    public String getFullFriendlyDayString(final Context context, final long dateInMillis) {
        String day = getDayName(dateInMillis);
        int formatId = R.string.format_full_friendly_date;
        return context.getString(formatId, day, getFormattedMonthDay(dateInMillis));
    }

    private String getDayName(long dateInMillis) {
        int inputDay = getDayOfYearFor(dateInMillis);
        int today = getDayOfYearForToday();
        if (inputDay == today) {
            return todayString;
        } else if (inputDay == today + 1) {
            return tomorrowString;
        } else {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            return dayFormat.format(dateInMillis);
        }
    }

    private String getFormattedMonthDay(final long dateInMillis) {
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return monthDayFormat.format(dateInMillis);
    }

    private int getDayOfYearForToday() {
        return new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
    }

    private int getDayOfYearFor(final long dateInMillis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(dateInMillis);
        return calendar.get(Calendar.DAY_OF_YEAR);
    }
}

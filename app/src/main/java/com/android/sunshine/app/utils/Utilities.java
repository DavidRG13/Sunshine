package com.android.sunshine.app.utils;

import android.content.Context;
import com.android.sunshine.app.R;
import com.android.sunshine.app.model.Contract;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utilities {

    public static String getFriendlyDay(Context context, String dateStr) {
        Date todayDate = new Date();
        String todayStr = Contract.getDbDateString(todayDate);
        Date inputDate = Contract.getDateFromDb(dateStr);

        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            return context.getString(
                    R.string.format_full_friendly_date,
                    today,
                    getFormattedMonthDay(dateStr));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = Contract.getDbDateString(cal.getTime());

            if (dateStr.compareTo(weekFutureString) < 0) {
                return getDayName(context, dateStr);
            } else {
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Contract.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            if (Contract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (Contract.getDbDateString(tomorrowDate).equals(
                        dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFormattedMonthDay(String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Contract.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            return monthDayFormat.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
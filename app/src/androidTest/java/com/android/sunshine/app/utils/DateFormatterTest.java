package com.android.sunshine.app.utils;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateFormatterTest {

    private static final String TODAY = "today";
    private static final String TOMORROW = "tomorrow";

    @Test
    public void shouldPrintTodayProperly() {
        DateFormatter dateFormatter = new DateFormatter(TODAY, TOMORROW);
        GregorianCalendar todayCalendar = new GregorianCalendar();

        String result = dateFormatter.getFriendlyDay(todayCalendar.getTimeInMillis(), true);

        assertEquals("today, " + getMonthOf(todayCalendar) + " " + getDayOf(todayCalendar), result);
    }

    @Test
    public void shouldPrintTomorrowProperly() {
        DateFormatter dateFormatter = new DateFormatter(TODAY, TOMORROW);
        GregorianCalendar tomorrowCalendar = new GregorianCalendar();
        tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);

        String result = dateFormatter.getFriendlyDay(tomorrowCalendar.getTimeInMillis(), true);

        assertEquals(TOMORROW, result);
    }

    private String getDayOf(final GregorianCalendar todayCalendar) {
        int day = todayCalendar.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            return "0" + String.valueOf(day);
        } else {
            return String.valueOf(day);
        }
    }

    private String getMonthOf(final GregorianCalendar todayCalendar) {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        return months[todayCalendar.get(Calendar.MONTH)];
    }
}
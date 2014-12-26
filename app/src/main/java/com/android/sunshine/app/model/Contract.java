package com.android.sunshine.app.model;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Contract {

    public static final String CONTENT_AUTHORITY = "com.android.sunshine.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ARTICLES = "articles";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:SS'Z'";

    public static final class ArticleEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTICLES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_ARTICLES;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_ARTICLES;
        public static final String TABLE_NAME = "articles";

        public static final String COLUMN_URL = "url";
        public static final String COLUMN_SNIPPET = "snippet";
        public static final String COLUMN_DATE = "pub_date";
        public static final String COLUMN_SECTION_NAME = "section_name";
        public static final String COLUMN_SHORT_DESCRIPTION = "short_description";
        public static final String COLUMN_THUMBNAIL = "thumbnail";
        public static final String COLUMN_LARGE_IMAGE = "large_image";
        public static final String COLUMN_ARTICLE_ID = "article_id";

        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildArticlesWithStartDate(String startDate) {
            return CONTENT_URI.buildUpon().appendQueryParameter(ArticleEntry.COLUMN_DATE, startDate).build();
        }
    }

    public static String getDbDateString(final Date date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(date);
    }

    public static Date getDateFromDb(String date) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date parsedDate = null;
        try {
            parsedDate = simpleDateFormat.parse(date);
            return parsedDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
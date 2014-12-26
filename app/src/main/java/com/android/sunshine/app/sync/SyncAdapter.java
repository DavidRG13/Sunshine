package com.android.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.android.sunshine.app.R;
import com.android.sunshine.app.model.Article;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.android.sunshine.app.model.Contract.ArticleEntry;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final String RECENT_NEWS_URI = "http://api.nytimes.com/svc/search/v2/articlesearch.json?sort=newest&api-key=b63145ac800ee1af1914f28a58aad6d2%3A9%3A70505141";
    private static final String SECTION_NAME = "section_name";
    private static final String WEB_URL = "web_url";
    private static final String PUB_DATE = "pub_date";
    private static final String DOCS = "docs";
    private static final String SNIPPET = "snippet";
    private static final String HEADLINE = "headline";
    private static final String MAIN = "main";
    private static final String MULTIMEDIA = "multimedia";
    private static final String SUBTYPE = "subtype";
    private static final String URL = "url";
    private static final String ID = "_id";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(this.getClass().getName(), "onPerformSync");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

        try {
            URL url = new URL(RECENT_NEWS_URI);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() > 0) {
                    forecastJsonStr = buffer.toString();
                }
                parseArticlesFrom(forecastJsonStr);
            }
        } catch (IOException | JSONException e) {
            Log.e("WeatherRequester", "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        Log.d(SyncAdapter.class.getName(), "initializeSyncAdapter");
        getSyncAccount(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private void parseArticlesFrom(String jsonStr) throws JSONException {
        JSONObject forecastJson = new JSONObject(jsonStr);
        JSONObject response = forecastJson.getJSONObject("response");
        JSONArray newsArray = response.getJSONArray(DOCS);

        Vector<ContentValues> cVVector = new Vector<>(newsArray.length());

        for (int i = 0; i < newsArray.length(); i++) {
            Article article = new Article();
            JSONObject actualNew = newsArray.getJSONObject(i);

            article.setPubDate(actualNew.getString(PUB_DATE));
            article.setUrl(actualNew.getString(WEB_URL));
            article.setId(actualNew.getString(ID));
            article.setSectionName(actualNew.getString(SECTION_NAME));
            article.setSnippet(actualNew.getString(SNIPPET));
            JSONObject headline = actualNew.getJSONObject(HEADLINE);
            article.setShortDescription(headline.getString(MAIN));
            JSONArray jsonArray = actualNew.getJSONArray(MULTIMEDIA);
            for (int j = 0; j < jsonArray.length(); j++){
                JSONObject actualMultimedia = jsonArray.getJSONObject(j);
                String subtype = actualMultimedia.getString(SUBTYPE);
                if("thumbnail".equals(subtype)){
                    article.setThumbnail(actualMultimedia.getString(URL));
                }else{
                    article.setxLargeImage(actualMultimedia.getString(URL));
                }
            }

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(ArticleEntry.COLUMN_ARTICLE_ID, article.getId());
            weatherValues.put(ArticleEntry.COLUMN_URL, article.getUrl());
            weatherValues.put(ArticleEntry.COLUMN_SNIPPET, article.getSnippet());
            weatherValues.put(ArticleEntry.COLUMN_SHORT_DESCRIPTION, article.getShortDescription());
            weatherValues.put(ArticleEntry.COLUMN_DATE, article.getPubDate());
            weatherValues.put(ArticleEntry.COLUMN_SECTION_NAME, article.getSectionName());
            weatherValues.put(ArticleEntry.COLUMN_THUMBNAIL, article.getThumbnail());
            weatherValues.put(ArticleEntry.COLUMN_LARGE_IMAGE, article.getxLargeImage());

            cVVector.add(weatherValues);
        }
        if (cVVector.size() > 0) {
            ContentValues[] contentValues = new ContentValues[cVVector.size()];
            cVVector.toArray(contentValues);
            getContext().getContentResolver().bulkInsert(ArticleEntry.CONTENT_URI, contentValues);

            //Calendar cal = Calendar.getInstance();
            //cal.add(Calendar.DATE, -1);
            //String yesterdayDate = Contract.getDbDateString(cal.getTime());
            //getContext().getContentResolver().delete(ArticleEntry.CONTENT_URI,
            //        ArticleEntry.COLUMN_DATE + " <= ?", new String[] {yesterdayDate});
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }
}
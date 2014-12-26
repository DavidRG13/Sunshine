package com.android.sunshine.app.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class BitmapUtils {

    public static final String NY_TIMES_IMAGES_BUCKET = "http://graphics8.nytimes.com/";

    private static final DisplayImageOptions roundedDisplayOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .showImageOnLoading(android.R.color.transparent)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
            .displayer(new RoundedBitmapDisplayer(1000))
            .build();
    private static final DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .build();

    public static void displayImageIn(final ImageView imageView, final String uri){
        ImageLoader.getInstance().displayImage(getUriFromNYTimes(uri), imageView, displayOptions);
    }

    public static void displayImage(String url, ImageView imageView, int defaultBitmapResource) {
        if (url == null) {
            imageView.setImageResource(defaultBitmapResource);
        } else {
            displayImageIn(imageView, url);
        }
    }

    private static String getUriFromNYTimes(String uri) {
        return NY_TIMES_IMAGES_BUCKET + uri;
    }
}

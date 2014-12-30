package com.android.sunshine.app.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class ViewInWebButton extends Button implements View.OnClickListener {
    private String url;

    public ViewInWebButton(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public ViewInWebButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public ViewInWebButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        getContext().startActivity(i);
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

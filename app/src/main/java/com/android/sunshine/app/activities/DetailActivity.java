package com.android.sunshine.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.android.sunshine.app.App;
import com.android.sunshine.app.R;
import com.android.sunshine.app.utils.IntentLauncher;
import javax.inject.Inject;

public class DetailActivity extends AppCompatActivity {

    @Inject
    IntentLauncher intentLauncher;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ((App) getApplication()).getComponent().inject(this);

        if (savedInstanceState == null) {
            final long date = getIntent().getLongExtra(IntentLauncher.DATE_KEY, 0);
            intentLauncher.detailsWithTransitionEnabled(date, this);
        }
    }
}

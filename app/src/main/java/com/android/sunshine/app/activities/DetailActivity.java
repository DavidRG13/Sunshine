package com.android.sunshine.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.android.sunshine.app.R;
import com.android.sunshine.app.fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity {

    public static final String DATE_KEY = "forecast_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            final long date = getIntent().getLongExtra(DATE_KEY, 0);
            final Bundle bundle = new Bundle();
            bundle.putLong(DATE_KEY, date);
            bundle.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);
            final DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_detail_container, detailFragment)
                    .commit();
            supportPostponeEnterTransition();
        }
    }
}
package com.android.sunshine.app.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.android.sunshine.app.R;
import com.android.sunshine.app.fragments.DetailFragment;

public class DetailActivity extends ActionBarActivity {

    public static final String ID_KEY = "article_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            final long id = getIntent().getLongExtra(ID_KEY, 0);
            final Bundle bundle = new Bundle();
            bundle.putLong(ID_KEY, id);
            final DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_detail_container, detailFragment)
                    .commit();
        }
    }
}
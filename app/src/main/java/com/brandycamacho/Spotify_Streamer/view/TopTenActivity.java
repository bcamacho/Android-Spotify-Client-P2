package com.brandycamacho.Spotify_Streamer.view;

import android.content.res.Configuration;
import android.os.Bundle;

import com.brandycamacho.Spotify_Streamer.R;

public class TopTenActivity extends ActivityStandardActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten);
    }

    // If orientation change complete task
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        finish();
    }
}

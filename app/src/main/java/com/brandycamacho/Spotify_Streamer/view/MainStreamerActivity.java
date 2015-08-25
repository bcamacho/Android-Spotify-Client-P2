package com.brandycamacho.Spotify_Streamer.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.AudioService;

/**
 * Created by brandycamacho on 6/10/15.
 */


public class MainStreamerActivity extends ActivityStandardActionBar {
    String TAG = this.toString();
    private AudioService audioService;
    private Intent mediaPlayerIntent;
    private boolean audioServiceBound = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We need a way to communicate with upNaviation to provide R.Id for actionBar
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int stackHeight = getSupportFragmentManager().getBackStackEntryCount();
                if (stackHeight > 0) { // if we have something on the stack (doesn't include the current shown fragment)
                    getActionBar().setHomeButtonEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getActionBar().setDisplayHomeAsUpEnabled(false);
                    getActionBar().setHomeButtonEnabled(false);
                }
            }

        });
        // set content
        setContentView(R.layout.fragment_container);
    }

    //connect to the service
    private ServiceConnection musicServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioMusicBinder binder = (AudioService.AudioMusicBinder) service;
            //get service
            audioService = binder.getService();
            audioServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioServiceBound = false;
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "Saved");
        super.onSaveInstanceState(outState);
        outState.putString("saved", "true");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mediaPlayerIntent == null && audioServiceBound == false) {
            mediaPlayerIntent = new Intent(this, AudioService.class);
            startService(mediaPlayerIntent);
        }
    }


}
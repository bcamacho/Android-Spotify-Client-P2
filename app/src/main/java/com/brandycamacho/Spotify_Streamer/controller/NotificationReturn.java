package com.brandycamacho.Spotify_Streamer.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by brandycamacho on 8/21/15.
 */
public class NotificationReturn extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = (String) getIntent().getExtras().get("DO");

        switch (action) {
            case "pause":
                Log.i("NotificationReturnSlot", "PAUSE");
                Intent i = new Intent("trackPause");
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                break;
            case "next":
                Log.i("NotificationReturnSlot", "NEXT");
                Intent i2 = new Intent("nextTrack");
                LocalBroadcastManager.getInstance(this).sendBroadcast(i2);
                break;
            case "previous":
                Log.i("NotificationReturnSlot", "PREVIOUS");
                Intent i3 = new Intent("previousTrack");
                LocalBroadcastManager.getInstance(this).sendBroadcast(i3);
                break;
        }
        finish();
    }


}
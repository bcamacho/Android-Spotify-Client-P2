package com.brandycamacho.Spotify_Streamer.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.AudioService;

/**
 * Created by brandycamacho on 8/21/15.
 */
public class ActivityStandardActionBar extends FragmentActivity {

    Menu actionBarMenu;

    Boolean isStarted = false;
    Handler timerHandler = new Handler();
    Runnable runAutoMenuUpdate = new Runnable() {
        @Override
        public void run() {
            invalidateOptionsMenu();
            //menuRefresh();
            timerHandler.postDelayed(runAutoMenuUpdate, 1000);
        }
    };
    int screenSize;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, R.string.action_settings).setIcon(android.R.drawable.ic_menu_preferences).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        getMenuInflater().inflate(R.menu.menu_track_player, actionBarMenu);
        if (AudioService.isPlaying) {
            actionBarMenu.add(0, 1, 0, "Now Playing")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        if (!isStarted) {
            timerHandler.postDelayed(runAutoMenuUpdate, 50);
            isStarted = true;
        }
        screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Log.v("MENU", "----------> Finish was called <-----------");
                super.onBackPressed();
                return true;
            case 0:
                Log.v("ACTIONBAR_MENU", "Settings was selected");
                DialogSettingsFragment d = DialogSettingsFragment.newInstance();
                d.show(getFragmentManager(), "dialog");
                d.setCancelable(false);
                d.setRetainInstance(true);
                break;
            case 1:
                Log.v("ACTIONBAR_MENU", "Now Playing was selected");
                if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                    Intent i = new Intent("showTrackplayerDialog");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                } else {
                    Intent i = new Intent("showTrackplayer");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }
}

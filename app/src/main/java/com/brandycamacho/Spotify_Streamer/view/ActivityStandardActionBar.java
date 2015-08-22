package com.brandycamacho.Spotify_Streamer.view;

import android.content.Intent;
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
public class ActivityStandardActionBar extends FragmentActivity{

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_player, actionBarMenu);
        if (AudioService.isPlaying) {
            actionBarMenu.add(0, 0, 0, "Now Playing")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        if (!isStarted) {
            timerHandler.postDelayed(runAutoMenuUpdate, 50);
            isStarted = true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                Log.v("ACTIONBAR_MENU", "Settings was selected");
                // Usinf DialogFragment for media Player
                DialogSettingsFragment d = DialogSettingsFragment.newInstance();
//                d.setArguments(mArtistBundle);
                d.show(getFragmentManager() , "dialog");
                d.setCancelable(false);
                d.setRetainInstance(true);
                break;
            case 0:
                Log.v("ACTIONBAR_MENU", "Now Playing was selected");
                // Creating new intent activity to display results within new window
                Intent i = new Intent("showTrackplayer");
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void menuRefresh(){
        actionBarMenu.clear();
        onCreateOptionsMenu(actionBarMenu);
    }


}

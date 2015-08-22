package com.brandycamacho.Spotify_Streamer.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.AudioService;

public class TrackPlayer extends FragmentActivity {
    private static ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_player);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.menu_share, menu);

        menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 1, 0, "Stop Track").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 2, 0, R.string.action_settings).setIcon(android.R.drawable.ic_menu_preferences).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // Locate MenuItem with ShareActionProvider
        mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
        // Fetch and store ShareActionProvider
        mShareActionProvider.setShareIntent(getDefaultShareIntent());
        return super.onCreateOptionsMenu(menu);
    }


    private static Intent getDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this song \n");
        intent.putExtra(Intent.EXTRA_TEXT, "Group = " + AudioService.artistName + "\nTitle = " + AudioService.trackTitle + "\nPreview = " + AudioService.trackUrl);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {

            case 1:
                Log.v("ACTIONBAR_MENU", "Stop track request");
                Intent i = new Intent("stopTrack");
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                this.finish();
                break;
            case 2:
                Log.v("ACTIONBAR_MENU", "Settings was selected");
                DialogSettingsFragment d = DialogSettingsFragment.newInstance();
                d.show(getFragmentManager(), "dialog");
                d.setCancelable(false);
                d.setRetainInstance(true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

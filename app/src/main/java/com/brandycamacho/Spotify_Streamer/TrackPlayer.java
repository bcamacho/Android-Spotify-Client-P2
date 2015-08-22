package com.brandycamacho.Spotify_Streamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.ShareActionProvider;

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
        // Locate MenuItem with ShareActionProvider
        mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
        // Fetch and store ShareActionProvider
        mShareActionProvider.setShareIntent(getDefaultShareIntent());

        return super.onCreateOptionsMenu(menu);
    }

    public static void refreshShare() {
        // Fetch and store ShareActionProvider
        mShareActionProvider.setShareIntent(getDefaultShareIntent());
    }

    private static Intent getDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this song \n");
        intent.putExtra(Intent.EXTRA_TEXT, "Group = " + AudioService.artistName + "\nTitle = " + AudioService.trackTitle + "\nPreview = " + AudioService.trackUrl);
        return intent;
    }

}

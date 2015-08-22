package com.brandycamacho.Spotify_Streamer.view;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.AudioService;
import com.brandycamacho.Spotify_Streamer.model.Artist;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    String TAG = this.toString();
    Boolean isFirstRun, autoPlay;
    TextView tv_album_title, tv_track_name, tv_artist_name;
    ImageView iv_album_art;
    Bundle mGetArtistBundle;
    ProgressDialog progressDialog;
    // timer control
    int defaultTimerDelay = 1500;
    int mTimeLeft;
    int mDuration;
    Handler timerHandler = new Handler();
    Runnable runPlayStatusCheck = new Runnable() {
        @Override
        public void run() {
            mDuration = AudioService.getDuration();
            seekbar.setMax(mDuration);
            mTimeLeft = AudioService.getGetCurrentPosition();
            if (mTimeLeft > 1) {
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    // :TODO provide a better solution for progress dialog
                }
            }
            if (AudioService.isPaused) {
                btn_play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));
            } else if (!AudioService.isPlaying) {
                seekbar.setProgress(0);
                btn_play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));
            } else if (AudioService.isPlaying) {
                btn_play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
                // Format time to xx:xxx
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                symbols.setGroupingSeparator(':');
                DecimalFormat formater = new DecimalFormat("###,###.###", symbols);
                // set time to text view
                tv_elapsed_time.setText("Elapsed Time " + String.valueOf(formater.format(mTimeLeft)));
                tv_track_duration.setText("Track Duration " + String.valueOf(formater.format(mDuration)));
            }
            seekbar.setProgress(AudioService.getGetCurrentPosition());
            // repeat
            timerHandler.postDelayed(runPlayStatusCheck, 350);
        }
    };

    Runnable runAutoPlay = new Runnable() {
        @Override
        public void run() {
            Intent i = new Intent("trackPlayerPlayTrack");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
            startProgressDialog();
        }
    };

    Runnable runAutoSetUserSelected = new Runnable() {
        @Override
        public void run() {
            AudioService.isUserSelected = false;
            timerHandler.postDelayed(runAutoSetUserSelected, defaultTimerDelay);
        }
    };
    // Track control
    ArrayList<Artist> mArtistTopTrackList = new ArrayList<>();
    ImageView btn_play;
    public static int position;
    boolean hasStarted = false;
    int seekToPosition = 0;
    SeekBar seekbar;
    TextView tv_track_duration, tv_elapsed_time;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mGetArtistBundle = getActivity().getIntent().getExtras();
        // using BroadcastManager to allow future control over track player
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiverTrackPlayerPlayTrack,
                new IntentFilter("trackPlayerPlayTrack"));
        // configure view
        v = inflater.inflate(R.layout.fragment_track_player, container, false);
        seekbar = (SeekBar) v.findViewById(R.id.progressSeekBar);
        btn_play = (ImageButton) v.findViewById(R.id.btn_play);
        tv_elapsed_time = (TextView) v.findViewById(R.id.tv_elapsed_time);
        tv_album_title = (TextView) v.findViewById(R.id.tv_album_title);
        tv_track_name = (TextView) v.findViewById(R.id.tv_track_title);
        tv_artist_name = (TextView) v.findViewById(R.id.tv_artist_name);
        iv_album_art = (ImageView) v.findViewById(R.id.iv_album_art);
        ImageView btn_previous = (ImageButton) v.findViewById(R.id.btn_previous);
        ImageView btn_next = (ImageButton) v.findViewById(R.id.btn_next);
        tv_track_duration = (TextView) v.findViewById(R.id.tv_track_duration);
        btn_previous.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);
        btn_play.setOnClickListener(this);

        // Format time to xx:xxx
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(':');
        DecimalFormat formater = new DecimalFormat("###,###.###", symbols);
        // Verify saveInstanceState is null otherwise we are returning to activity and must use saveInstanceState to return data to end user
        if (savedInstanceState == null) {
            Log.e(TAG, "No data??? - " + hasStarted);
            autoPlay = mGetArtistBundle.getBoolean("autoPlay") != false;
        } else {
            autoPlay = false;
            hasStarted = savedInstanceState.getBoolean("hasStarted");
            Log.e(TAG, "Theres data!!! - " + savedInstanceState.getBoolean("hasStarted"));
            mDuration = savedInstanceState.getInt("duration");
            mTimeLeft = savedInstanceState.getInt("elapseTime");
            position = savedInstanceState.getInt("position");
            Log.v(TAG, String.valueOf(savedInstanceState.getInt("seekPosition")));
        }

        if (mGetArtistBundle != null) {
            // Using bundle to pass data between fragment activities
            if (savedInstanceState == null) {
                position = mGetArtistBundle.getInt("position");
            }
            mArtistTopTrackList = mGetArtistBundle.getParcelableArrayList("topTrackList");
        }

        tv_elapsed_time.setText("Elapsed Time " + formater.format(mTimeLeft));
        tv_track_duration.setText("Track Duration " + formater.format(mDuration));
        tv_artist_name.setText(mArtistTopTrackList.get(position).getName());

        if (autoPlay) {
            timerHandler.postDelayed(runAutoPlay, 5);
        }

        // Update gui
        updateTrackInfo();
        // start timer
        timerUpdate();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle trackPlayer) {
        super.onSaveInstanceState(trackPlayer);
        Log.v(TAG, "Saved Instance State Started, saving timeLeft " + mTimeLeft);
        trackPlayer.putBoolean("hasStarted", hasStarted);
        trackPlayer.putInt("elapseTime", mTimeLeft);
        trackPlayer.putInt("duration", mDuration);
        trackPlayer.putInt("position", position);
        trackPlayer.putInt("seekPosition", seekbar.getProgress());
    }

    @Override
    public void onClick(View v) {
        startProgressDialog();
        int count = mArtistTopTrackList.size() - 1;
        Log.v(TAG, "v.GetId = " + v.getId());
        int currentTrack = AudioService.trackPosition;
//        timerHandler.removeCallbacks(runAutoSetUserSelected);
        switch (v.getId()) {
            // Play track
            case R.id.btn_play:
                Log.v(TAG, "Play");
                if (isFirstRun == false && AudioService.isPlaying) {
                    pauseTrack();
                } else {
                    isFirstRun = false;
                    stopTrack();
                    playTrack(currentTrack, true);
                }
                break;
            // Next Track
            case R.id.btn_next:
                if (position == count) {
                    Toast.makeText(getActivity(), "Unable to play, you reached the last track", Toast.LENGTH_SHORT).show();
                } else {

                    currentTrack++;
                    stopTrack();
                    playTrack(currentTrack, true);
                }
                updateTrackInfo();
                updateListView(currentTrack);
                break;
            // Previous Track
            case R.id.btn_previous:
                Log.v(TAG, "Previous");
                if (currentTrack == 0) {
                    stopTrack();
                    playTrack(currentTrack, true);
                    Toast.makeText(getActivity(), "You reached the first track", Toast.LENGTH_SHORT).show();
                } else {
                    currentTrack--;
                    stopTrack();
                    playTrack(currentTrack, true);
                }
                updateTrackInfo();
                updateListView(currentTrack);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Stop timer task");
        timerHandler.removeCallbacks(runPlayStatusCheck);
        timerHandler.removeCallbacks(runAutoSetUserSelected);
        Log.e(TAG, "TrackPlayer Has been destroyed");
    }

    private void playTrack(final int trackNumber, boolean isUserSelected) {
        position = trackNumber;
        Intent i = new Intent("trackPlay");
        i.putParcelableArrayListExtra("topTrackList", mArtistTopTrackList);
        i.putExtra("trackNumber", trackNumber);
        i.putExtra("userSelected", isUserSelected);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
    }

    private void pauseTrack() {
        Intent i = new Intent("trackPause");
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
    }

    private void stopTrack() {
        Intent i = new Intent("stopTrack");
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
    }


    private void updateListView(int position) {
        Intent i = new Intent("updateTrackPosition");
        i.putExtra("position", position);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
    }

    private void timerUpdate() {
        timerHandler.postDelayed(runPlayStatusCheck, 500);
        timerHandler.postDelayed(runAutoSetUserSelected, 1000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            seekToPosition = progress;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Intent i = new Intent("trackSeek");
        i.putExtra("seekTo", seekToPosition);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
    }

    private void updateTrackInfo() {
        Artist artist = mArtistTopTrackList.get(position);
        tv_album_title.setText(artist.getAlbum());
        tv_track_name.setText(artist.getTrackTitle());
        iv_album_art = (ImageView) v.findViewById(R.id.iv_album_art);
        Picasso.with(getActivity()).load(artist.getAlbum_art()).into(iv_album_art);
        isFirstRun = false;
    }

    private BroadcastReceiver mBroadcastReceiverTrackPlayerPlayTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (position < mArtistTopTrackList.size())
                playTrack(position, true);
        }
    };

    private void startProgressDialog() {
        // instantiate new progress dialog
        progressDialog = new ProgressDialog(getActivity());
        // spinner (wheel) style dialog
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // better yet - use a string resource getString(R.string.your_message)
        progressDialog.setMessage("Loading data");
        // display dialog
        try {
            progressDialog.show();
        } catch (Exception e) {
            Log.v(TAG, String.valueOf(e));
        }
    }

}

package com.brandycamacho.Spotify_Streamer.controller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.brandycamacho.Spotify_Streamer.model.Artist;
import com.brandycamacho.Spotify_Streamer.view.TrackPlayer;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brandycamacho on 8/6/15.
 */
public class AudioService extends Service implements MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    String TAG = this.getClass().getName();
    SpotifyApi api;
    SpotifyService spotify;
    public static int trackPosition;
    public static boolean KEYBOARD_VISUAL_STATE = false;
    public static boolean isPaused = false;
    private MediaPlayer mediaPlayer;
    private final IBinder mAudioMusicBind = new AudioMusicBinder();
    public int currentPosition;
    public static boolean isPlaying = false;
    private Boolean isResume;
    private int lastKnownResumePosition;
    public static Boolean isUserSelected;
    private static int mGetCurrentPosition;
    public static int mGetDuration;
    public static String artistName, trackTitle, trackUrl, albumArt;
    SharedPreferences sharedPrefs;
    boolean allowNotifications;
    ArrayList<Artist> mArtistTopTrackList = new ArrayList<>();


    // Timer to prevent user request overload using Spotify API and retrofit
    Handler timerHandler = new Handler();
    Runnable runTrackRequest;

    public void setCurrentPosition(int CurrentPosition) {
        mGetCurrentPosition = CurrentPosition;
    }

    public void setDuration(int duration) {
        mGetDuration = duration;
    }

    public static int getGetCurrentPosition() {
        return mGetCurrentPosition;
    }

    public static int getDuration() {
        return mGetDuration;
    }


    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    // service state
    public enum serviceState {
        RETRIEVE, STOPPED, PREPARE, PLAYING, PAUSED
    }

    public static serviceState mMediaState = serviceState.RETRIEVE;

    @Override
    public void onCreate() {

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverPlayTrack,
                new IntentFilter("trackPlay"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverPauseTrack,
                new IntentFilter("trackPause"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverSeekToActivity,
                new IntentFilter("trackSeek"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverStopActivity,
                new IntentFilter("stopTrack"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverNextTrackActivity,
                new IntentFilter("nextTrack"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverPreviousTrackActivity,
                new IntentFilter("previousTrack"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverShowTrackPlayerActivity,
                new IntentFilter("showTrackplayer"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverShowTrackPlayerDialog,
                new IntentFilter("showTrackplayerDialog"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverUpdateTrackInfo,
                new IntentFilter("updateTrackInfo"));

        // We only want to call one instance to prevent duplicate activities which causes a mess!
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
        api = new SpotifyApi();
        spotify = api.getService();

        // Obtain SharedPref
        sharedPrefs = getApplication().getSharedPreferences("AppDataPref", Context.MODE_PRIVATE);

        //create the service
        super.onCreate();
        Log.v(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return mAudioMusicBind;
    }

    //  This will execute when the user exits the app, at which point we will stop the service.
    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommmand " + isPlaying);
        if (KEYBOARD_VISUAL_STATE == true) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                }
            }).run();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        //stop();
        // mediaPlayer.release();
    }

    public void initMediaPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        mMediaState = serviceState.PLAYING;
        isPlaying = true;
        if (lastKnownResumePosition > 0) {
            mediaPlayer.seekTo(lastKnownResumePosition);
            isResume = false;
            lastKnownResumePosition = 0;
        }
        currentPosition = mediaPlayer.getCurrentPosition();
        mediaPlayer.setOnCompletionListener(this);
        new SeekBarHandler().execute();
    }

    private void stop(boolean isNewArtistSelection) {
        Log.v(TAG, "Stoping song");
        if (isNewArtistSelection) {
            isResume = true;
            lastKnownResumePosition = mediaPlayer.getCurrentPosition();
            Log.v(TAG, "Stoping song");
        } else {
            isResume = false;
            isUserSelected = true;
            Log.v(TAG, "Stoping song");
        }
        mediaPlayer.stop();
        mediaPlayer.reset();
        mMediaState = serviceState.STOPPED;
        isPlaying = false;
        MediaNotification.cancel(getApplication());
    }

    private void pause() {
        if (!isPaused) {
            Log.v(TAG, "Pausing song");
            mediaPlayer.pause();
            mMediaState = serviceState.PAUSED;
            isPaused = true;
        } else {
            Log.v(TAG, "un-Pausing song");
            mediaPlayer.start();
            mMediaState = serviceState.PLAYING;
            isPaused = false;
        }
    }


    // SeekBar AsyncTask to pass data regarding track playhead position
    public class SeekBarHandler extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void result) {
            Log.d("Seek Bar Handle", "Destroyed");
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            while (mMediaState == serviceState.PLAYING || mMediaState == serviceState.PAUSED) {
                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setCurrentPosition(mediaPlayer.getCurrentPosition());
                // Log.v(TAG, String.valueOf(mediaPlayer.getCurrentPosition()));

                setDuration(mediaPlayer.getDuration());
                // Log.v(TAG, "Durration = "+mediaPlayer.getDuration());
            }
            return null;
        }

    }

    public void play(String urlData) {

        Log.v(TAG, "starting mediaPlayer");
        try {
            Log.v(TAG, "media url = " + urlData);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(urlData);
            // media player start
            mMediaState = serviceState.PREPARE;
            mediaPlayer.prepareAsync();
            isPlaying = true;
        } catch (Exception e) {
            Log.v(TAG, String.valueOf(e));
        }

    }

    private void updateTrackInfo(){
        albumArt = mArtistTopTrackList.get(trackPosition).getAlbum_art();
        String artistName = mArtistTopTrackList.get(trackPosition).getName();
        String trackTitle = mArtistTopTrackList.get(trackPosition).getTrackTitle();
        AudioService.artistName = artistName;
        AudioService.trackTitle = trackTitle;
    }

    private void playTrack(final int trackNumber, Boolean isAuto, final int seekTo) {
        trackPosition = trackNumber;
        if (isAuto) {
            trackPosition++;
        }
        if (trackPosition < mArtistTopTrackList.size() && trackPosition > -1) {

            updateTrackInfo();
            Log.v(TAG, "Play track # " + trackPosition);
            allowNotifications = sharedPrefs.getBoolean("allowNotifications", true);
            Artist artistTrackId = mArtistTopTrackList.get(trackPosition);
            String selectedTrackId = artistTrackId.getTrackId();
            final String artistName = mArtistTopTrackList.get(trackPosition).getName();
            final String trackTitle = mArtistTopTrackList.get(trackPosition).getTrackTitle();
            // checking notification settings
            if (allowNotifications) {
                new MediaNotification(getApplication(), artistName, trackTitle, albumArt);
            } else {
                MediaNotification.cancel(getApplication());
            }
            spotify.getTrack(selectedTrackId, new Callback<Track>() {
                @Override
                public void success(Track track, Response response) {
                    Log.v(TAG, "Tack preview url = " + track.preview_url);

                    String trackUrl = track.preview_url;
                    // create notification controller
                    AudioService.artistName = artistName;
                    AudioService.trackTitle = trackTitle;
                    AudioService.trackUrl = trackUrl;
                    if (seekTo > 0) {
                        lastKnownResumePosition = seekTo;
                        play(trackUrl);
                    } else {
                        play(trackUrl);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                }
            });
        } else {
            Log.v(TAG, "reached end of playlist");
        }
    }

    public class AudioMusicBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }

    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mBroadcastReceiverPlayTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            timerHandler.removeCallbacks(runTrackRequest);
            // Extract data included in the Intent
            mArtistTopTrackList = intent.getParcelableArrayListExtra("topTrackList");
            final int trackNumber = intent.getIntExtra("trackNumber", 0);
            isUserSelected = intent.getBooleanExtra("userSelected", true);
//            String message = intent.getStringExtra("url");
            runTrackRequest = new Runnable() {
                @Override
                public void run() {
                    Log.d("Timer", "Request Play track");
                    playTrack(trackNumber, false, 1);
                }
            };
            timerHandler.postDelayed(runTrackRequest, 500);

            Log.d("receiver", "Got message: prepairing to play track number #" + trackNumber);
        }
    };

    private BroadcastReceiver mBroadcastReceiverPauseTrack = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            pause();
            Log.d("receiver", "Got message: Pause ");
        }
    };

    private BroadcastReceiver mBroadcastReceiverSeekToActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            int message = intent.getIntExtra("seekTo", 0);
            mediaPlayer.seekTo(message);
            Log.d("receiver", "Got message: Seeking track to " + message);
        }
    };

    private BroadcastReceiver mBroadcastReceiverStopActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stop(intent.getBooleanExtra("isNewArtistSelection", false));
            Log.d("receiver", "Got message: Stopping track");
        }
    };

    private BroadcastReceiver mBroadcastReceiverNextTrackActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            timerHandler.removeCallbacks(runTrackRequest);
            isUserSelected = true;
            stop(intent.getBooleanExtra("isNewArtistSelection", false));
            final int playTrack = (trackPosition) + 1;
            Log.d("receiver", "Got message: Play Next track");
            runTrackRequest = new Runnable() {
                @Override
                public void run() {
                    Log.d("Timer", "Request Next track");
                    playTrack(playTrack, false, 0);
                }
            };
            timerHandler.postDelayed(runTrackRequest, 500);
        }
    };

    private BroadcastReceiver mBroadcastReceiverPreviousTrackActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            timerHandler.removeCallbacks(runTrackRequest);
            isUserSelected = true;
            final int playTrack = (trackPosition) - 1;
            Log.d("receiver", "Got message: Play Previous track");
            runTrackRequest = new Runnable() {
                @Override
                public void run() {
                    Log.d("Timer", "Request Previous track");
                    playTrack(playTrack, false, 0);
                }
            };
            timerHandler.postDelayed(runTrackRequest, 500);

        }
    };

    private BroadcastReceiver mBroadcastReceiverShowTrackPlayerDialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Creating new intent activity to display results within new window
            Bundle mArtistBundle = new Bundle();
            mArtistBundle.putInt("position", trackPosition);
            mArtistBundle.putBoolean("autoPlay", false);
            mArtistBundle.putBoolean("hideMenu", true);
            // send list of songs
            mArtistBundle.putParcelableArrayList("topTrackList", mArtistTopTrackList);
            Intent i = new Intent();
            i.setClass(context, TrackPlayer.class);
            i.putExtras(mArtistBundle);
            i.putExtra("autoPlay", false);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            Log.d("receiver", "Got message: Display Now Playing Track Player");
        }
    };

    private BroadcastReceiver mBroadcastReceiverShowTrackPlayerActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Creating new intent activity to display results within new window
            Bundle mArtistBundle = new Bundle();
            mArtistBundle.putInt("position", trackPosition);
            mArtistBundle.putBoolean("autoPlay", false);
            mArtistBundle.putBoolean("hideMenu", false);
            // send list of songs
            mArtistBundle.putParcelableArrayList("topTrackList", mArtistTopTrackList);
            Intent i = new Intent();
            i.setClass(context, TrackPlayer.class);
            i.putExtras(mArtistBundle);
            i.putExtra("autoPlay", false);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            Log.d("receiver", "Got message: Display Now Playing Track Player");
        }
    };

    private BroadcastReceiver mBroadcastReceiverUpdateTrackInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Creating new intent activity to display results within new window
            updateTrackInfo();
            Log.d("receiver", "Got message: Updating Track Info");
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.v(TAG, "onCompletion\nTrack Completion caused by end user is \"" + isUserSelected + "\"");
        mMediaState = serviceState.STOPPED;
        isPlaying = false;
        if (isResume) {
            playTrack(trackPosition, false, lastKnownResumePosition);
        } else if (!isUserSelected) {
            playTrack(trackPosition, true, 0);
        }
    }

    // Suggested during code review, great solution!
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        MediaNotification.cancel(getApplication());
        stopSelf();
    }
}

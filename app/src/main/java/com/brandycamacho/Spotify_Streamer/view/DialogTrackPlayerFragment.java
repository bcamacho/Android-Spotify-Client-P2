package com.brandycamacho.Spotify_Streamer.view;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.AudioService;
import com.brandycamacho.Spotify_Streamer.controller.PaletteGeneratorTransformation;
import com.brandycamacho.Spotify_Streamer.model.Artist;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class DialogTrackPlayerFragment extends DialogFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
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
    View v;
    /**
     * DialogFragments issues
     * I tried several times to prevent softKeyboard from appearing upon orentation change
     * Efforts included the following:
     * <p/>
     * //to hide keyboard when showing dialog fragment
     * getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     * <p/>
     * if (v != null) {
     * InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
     * imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
     * }
     * <p/>
     * I created a hack that uses a timerHandler to repeat request to dismiss softKeyboard
     * which works. However, I still prefer a regular fragment over DialogFragment due to this
     * issue. The use of full screen seems more appealing to me. Yet, during my last code review I was
     * told its a requirment which is why I ended up with this code to fullfill reviewers request.
     * <p/>
     * I could use some help on how to impliment seperate class to handle this hack.
     * I created seperate class to handle keyBoard Hack as I would use it within DialogSetting fragment.
     * However, the problem I have is no matter how I pass Context with View, InputManager will not work for me :(
     * Class is located under com.brandycamacho.Spotify_Streamer/controller/KeyBoardHack
     */
    int softKeyBaordHackAttemptsCount;
    boolean cancelHack = false;
    Handler timerHandler = new Handler();
    Runnable runSoftKeyBoardHack = new Runnable() {
        @Override
        public void run() {
            if (!cancelHack) {
                Log.v("HACK", "SoftKeyBoard Dismiss Attempt Count = " + softKeyBaordHackAttemptsCount);
                try {
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    if (softKeyBaordHackAttemptsCount < 5) {
                        timerHandler.postDelayed(runSoftKeyBoardHack, 100);
                        softKeyBaordHackAttemptsCount++;
                    }
                } catch (Exception e) {
                    Log.e("HACK", String.valueOf(e));
                }
            }
        }
    };
    int updateTrackCount;
    Runnable runUpdateBackgroundImage = new Runnable() {
        @Override
        public void run() {
            if (!cancelHack) {
                updateTrackCount++;
                updateBackgroundImage();
                if (updateTrackCount < 5) {
                    timerHandler.postDelayed(runUpdateBackgroundImage, 350);
                }

            }
        }
    };

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
    LinearLayout ll;
    ImageButton btn_settings, btn_share;
    Button btn_stop;
//    private static ShareActionProvider mShareActionProvider;


    public static DialogTrackPlayerFragment newInstance() {
        DialogTrackPlayerFragment d = new DialogTrackPlayerFragment();
        d.setRetainInstance(true);
        return d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGetArtistBundle = getArguments();
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
        btn_stop = (Button)v.findViewById(R.id.btn_stop);
        btn_settings = (ImageButton)v.findViewById(R.id.btn_settings);
        btn_share = (ImageButton)v.findViewById(R.id.btn_share);
        btn_previous.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);
        btn_play.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_settings.setOnClickListener(this);
        btn_share.setOnClickListener(this);

//        // Locate MenuItem with ShareActionProvider
//        mShareActionProvider = (ShareActionProvider) v.findViewById(R.id.btn_share).getActionProvider();
//        // Fetch and store ShareActionProvider
//        mShareActionProvider.setShareIntent(getDefaultShareIntent());

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
            case R.id.btn_settings:
                Log.v("FAKE_ACTIONBAR_MENU", "Settings was selected");
                DialogSettingsFragment d = DialogSettingsFragment.newInstance();
                d.show(getFragmentManager(), "dialog");
                d.setCancelable(false);
                d.setRetainInstance(true);
                break;
            case R.id.btn_stop:
                Log.v("FAKE_ACTIONBAR_MENU", "Stop track request");
                Intent i = new Intent("stopTrack");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
                progressDialog.dismiss();
                getDialog().dismiss();
                break;
            case R.id.btn_share:
                startActivity(getDefaultShareIntent());
                break;
        }
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
        ll = (LinearLayout) v.findViewById(R.id.ll_album_info);
        // use picasso to load album art to layout background
        // for some reason there is a delay loading background image?
        updateBackgroundImage();
        // there is a delay setting background image, using timer resolves this issue with limited loop count

        updateTrackCount = 0;
        timerHandler.postDelayed(runUpdateBackgroundImage, 350);
        isFirstRun = false;
    }

    private void updateBackgroundImage() {
        Artist artist = mArtistTopTrackList.get(position);
        Picasso.with(getActivity())
                .load(artist.getAlbum_art())
                .transform(new PaletteGeneratorTransformation(100))
                .into(new Target() {
                    @Override
                    @TargetApi(16)
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        int sdk = android.os.Build.VERSION.SDK_INT;
                        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            ll.setBackgroundDrawable(new BitmapDrawable(bitmap));
                            ll.getBackground().setAlpha(70);
                        } else {
                            ll.setBackground(new BitmapDrawable(getResources(), bitmap));
                            ll.getBackground().setAlpha(70);

                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        // use error drawable if desired
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // use placeholder drawable if desired
                    }
                });
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

    // Retain Dialog after orientation change
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        // call keyBoard hack to force hiding of KeyBoard
        if (!cancelHack) {
            softKeyBaordHackAttemptsCount = 0;
            killKeyboard();
        }
        super.onDestroyView();
    }

    private void killKeyboard() {
        timerHandler.postDelayed(runSoftKeyBoardHack, 50);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelHack = true;
        Log.e(TAG, "Stop timer task");
        timerHandler.removeCallbacks(runPlayStatusCheck);
        timerHandler.removeCallbacks(runAutoSetUserSelected);
        timerHandler.removeCallbacks(runSoftKeyBoardHack);
        timerHandler.removeCallbacks(runUpdateBackgroundImage);

        Log.e(TAG, "TrackPlayer Has been destroyed");
    }

    private static Intent getDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this song \n");
        intent.putExtra(Intent.EXTRA_TEXT, "Group = " + AudioService.artistName + "\nTitle = " + AudioService.trackTitle + "\nPreview = " + AudioService.trackUrl);
        return intent;
    }

}

package com.brandycamacho.Spotify_Streamer.view;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.ArtistTopTracksAdapter;
import com.brandycamacho.Spotify_Streamer.model.Artist;
import com.nhaarman.listviewanimations.appearance.simple.SwingRightInAnimationAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by brandycamacho on 6/10/15.
 */
public class ResultsTopTenFragment extends Fragment {

    private final String TAG = ArtistResultsFragment.class.getSimpleName();
    public static ArtistTopTracksAdapter<Artist> mArtistTopTenAdapter;
    SwingRightInAnimationAdapter animAdapter;
    public static ArrayList<Artist> mArtistTopTrackList = new ArrayList<>();
    String artistId = "";
    String artistName = "";
    ProgressDialog progressDialog;
    View v;
    public ListView lv_results_artist;
    Bundle mGetArtistBundle;
    int screenSize;

    // Configuration of menu options
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Log.v(TAG, "OnCreate");

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiverUpdatePosition,
                new IntentFilter("updateTrackPosition"));

        // Verify saveInstanceState is null otherwise we are returing to activity and must use saveInstanceState to return data to end user
        if (saveInstanceState == null) {
            // Check to see if Intent contains data, if set bundle resource to retrieve from intent extras
            Log.e(TAG, "GET FRAGMENT ARGUMENTS");
            mGetArtistBundle = this.getArguments();

            if (mGetArtistBundle != null) {
                // Using bundle to pass data between fragment activities
                artistId = mGetArtistBundle.getString("artistId");
                artistName = mGetArtistBundle.getString("artistName");
            }
        }

        if (saveInstanceState == null) {
            mArtistTopTenAdapter = new ArtistTopTracksAdapter<>(getActivity(), R.layout.list_view_artist_top_tracks, mArtistTopTrackList);
            FetchTopTenTracks fa = new FetchTopTenTracks();
            fa.execute();
        } else {
            saveInstanceState.getSerializable("topTenAdapter");
            artistName = saveInstanceState.getString("artistName");
        }
        animAdapter = new SwingRightInAnimationAdapter(mArtistTopTenAdapter);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("topTenAdapter", mArtistTopTenAdapter);
        outState.putString("artistName", artistName);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.v(TAG, "onViewCreated");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // apply view by using inflater to inflate view with fragment data
        v = inflater.inflate(R.layout.fragment_activity_results_artist, container, false);

        screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        lv_results_artist = (ListView) v.findViewById(R.id.lv_results_artist);
        lv_results_artist.setAdapter(animAdapter);
        animAdapter.setAbsListView(lv_results_artist);

        lv_results_artist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mArtistTopTenAdapter.getItem((int) id).getTrackTitle().equals("ERROR")) {
                    Toast.makeText(getActivity(), "Please verify internet connectivity and country code within settings", Toast.LENGTH_SHORT).show();
                } else {
                    // We will use Spotify ID to implement a details fragment activity that will provide details along with ability to listen to song.
                    Bundle mArtistBundle = new Bundle();
                    // send selected song
                    mArtistBundle.putString("artistName", artistName);
                    mArtistBundle.putString("trackId", mArtistTopTenAdapter.getItem((int) id).getTrackId());
                    mArtistBundle.putString("trackName", mArtistTopTenAdapter.getItem((int) id).getTrackTitle());
                    mArtistBundle.putString("albumName", mArtistTopTenAdapter.getItem((int) id).getAlbum());
                    mArtistBundle.putString("albumArt", mArtistTopTenAdapter.getItem((int) id).getAlbum_art());
                    mArtistBundle.putBoolean("autoPlay", true);
                    mArtistBundle.putInt("position", position);
                    // send list of songs
                    mArtistBundle.putParcelableArrayList("topTrackList", mArtistTopTrackList);

                    // If stream is playing we need to stop it otherwise we will get caught up waiting for the track to complete. Worse, if the user pauses the track and attempts to load new artist they will wait forever :(
                    Intent stopTrack = new Intent("stopTrack");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(stopTrack);

                    if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                        DialogTrackPlayerFragment d = DialogTrackPlayerFragment.newInstance();
                        d.setArguments(mArtistBundle);
                        d.show(getFragmentManager(), "dialog");
                    } else {
                        Intent i = new Intent();
                        i.setClass(getActivity(), TrackPlayer.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.putExtra("autoPlay", true);
                        i.putExtras(mArtistBundle);
                        startActivity(i);
                    }

                }
            }
        });

        FrameLayout.LayoutParams lv_params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        lv_results_artist.setLayoutParams(lv_params);

        /** Wrapping list adapter with nHaarman animationAdapter Library
         retrieved from: http://nhaarman.github.io/ListViewAnimations/#appearance-animations */
        return v;
    }


    /**
     * There was issues using spotify callback which resulted in using AsyncTask
     * Error --> java.lang.IndexOutOfBoundsException: Invalid index 10, size is 10
     * After trial and error I noticed first few times calling activity were a success.
     * Yet, after a few more executions I would start to receive IndexOutOfBounds error.
     * I think it has to do with time to execute within callback which is why I elected to use
     * AsyncTask as I can control pre/background/post events. Using AsyncTask solved my issue.
     * Also, its worth noting that I think this is related to the use of fragments versus starting
     * a new activity. Also, I am using Android Studio Canary Channel which might be causing this issue.
     */
    public class FetchTopTenTracks extends AsyncTask<Void, Void, Void> {
        SpotifyApi api;
        SpotifyService spotify;
        Map<String, Object> map = new HashMap<>();
        Tracks artistTrack;
        Artist testTrack;
        String artistName, trackTitle, trackId, album, url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                mArtistTopTrackList.clear();
                // instantiate new progress dialog
                progressDialog = new ProgressDialog(getActivity());
                // spinner (wheel) style dialog
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                // better yet - use a string resource getString(R.string.your_message)
                progressDialog.setMessage("Loading data");
                // display dialog
                progressDialog.show();

                api = new SpotifyApi();
                spotify = api.getService();

                SharedPreferences sp = getActivity().getSharedPreferences("AppDataPref", Context.MODE_PRIVATE);
                String countryCode = sp.getString("countryCode", getResources().getConfiguration().locale.getCountry());
                map.put("country", countryCode);
            } catch (Exception e) {
                Log.v(TAG, String.valueOf(e));
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                artistTrack = spotify.getArtistTopTrack(artistId, map);
            } catch (Exception e) {
                Log.v(TAG, String.valueOf(e));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            artistName = mGetArtistBundle.getString("artistName");
            Log.v(TAG, "------- ( " + artistName + " ) --------");
            if (artistTrack != null) {
                if (artistTrack.tracks.size() == 0) {
                    mArtistTopTenAdapter.add(new Artist(null, "ERROR", null, null, null, "Artist has no songs on Spotify, sorry :("));
                } else {
                    //Loop through tracks and add to list
                    for (int i = 0; i < artistTrack.tracks.size(); i++) {
                        Track data = artistTrack.tracks.get(i);
                        trackTitle = data.name;
                        trackId = data.id;
                        album = data.album.name;

                        if (data.album.images.size() != 0) {
                            url = data.album.images.get(0).url;
                        } else {
                            url = null;
                        }
                        testTrack = new Artist(artistName, trackTitle, trackId, artistId, url, album);
                        mArtistTopTrackList.add(testTrack);
                    }
                }
            } else {
                mArtistTopTenAdapter.add(new Artist(null, "ERROR", null, null, null, "Verify internet connectivity"));
            }
            // notify our adapter that the data in the list has changed
            mArtistTopTenAdapter.notifyDataSetChanged();
            // stop progress dialog
            progressDialog.dismiss();
        }

    }

    private BroadcastReceiver mBroadcastReceiverUpdatePosition = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            int trackPosition = intent.getIntExtra("position", 0);
            lv_results_artist.setSelectionFromTop(0, trackPosition);
//            mArtistTopTenAdapter.notifyDataSetChanged();
            Log.d("receiver", "Got message: Update Track List Position to " + String.valueOf(trackPosition));
        }
    };

}

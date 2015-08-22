package com.brandycamacho.Spotify_Streamer.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.ArtistNameAdapter;
import com.brandycamacho.Spotify_Streamer.model.Artist;
import com.nhaarman.listviewanimations.appearance.simple.SwingRightInAnimationAdapter;

import java.util.ArrayList;

/**
 * Created by brandycamacho on 6/10/15.
 */
public class ArtistResultsFragment extends Fragment {

    private static final String LIST_INSTANCE_STATE = "lv_instance_state";
    int mPosition;
    ListView lv_results_artist;
    private final String TAG = ArtistResultsFragment.class.getSimpleName();
    public static ArtistNameAdapter<Artist> mArtistAdapter;
    public static ArrayList<Artist> mArtistList = new ArrayList<>();

    View v;

    // Configuration of menu options is not needed considering there are no options needed for end user. The only option needed for Spotify API is location which is automatically generated
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "On create");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "Save Instance State to Bundle");
        // save instance state to bundle
        if (mPosition != AdapterView.INVALID_POSITION){
            outState.putInt(LIST_INSTANCE_STATE, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        Log.v(TAG, "On resume");
        // if bundle contains key for stored position then set to method varible mPosition
        if (mPosition != -1){
            lv_results_artist.setSelection(mPosition);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "On paused");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.v(TAG, "On stopped");
        super.onStop();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // apply view by using inflater to inflate view with fragment data
        v = inflater.inflate(R.layout.fragment_activity_results_artist, container, false);

        /** Wrapping adapter with nHaarman animationAdapter Library
        retrieved from: http://nhaarman.github.io/ListViewAnimations/#appearance-animations
         This allowed me to experiment with annimations for list view items*/

        mArtistAdapter = new ArtistNameAdapter<>(getActivity(), R.layout.list_view_artist, mArtistList);
        // If mArtistAdapter is empty prompt user to use EditText to search for artist and display results.
        if (mArtistAdapter.isEmpty()){
            // populating a single item with ID null to prevent onClick method from implementing
            mArtistAdapter.add(new Artist("Type artist name above to start search",null,null));
        }
        lv_results_artist = (ListView) v.findViewById(R.id.lv_results_artist);
        SwingRightInAnimationAdapter animAdapter = new SwingRightInAnimationAdapter(mArtistAdapter);
        animAdapter.setAbsListView(lv_results_artist);
        lv_results_artist.setAdapter(animAdapter);
        lv_results_artist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                 // check to determine if we need to kick off a new intent for portrait mode
                 TopTenActivityFragment frag = (TopTenActivityFragment)getActivity().getFragmentManager().findFragmentById(R.id.topTen);
                 // If stream is playing we need to stop it otherwise we will get caught up waiting for the track to complete. Worse, if the user pauses the track and attempts to load new artist they will wait forever :(
                 Intent stopTrack = new Intent("stopTrack");
                 stopTrack.putExtra("isNewArtistSelection", true);
                 LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(stopTrack);
                 if (frag != null && frag.isVisible()) {

                         // Use bundle to pass data to calling fragment
                         Bundle mArtistBundle = new Bundle();
                         mArtistBundle.putString("artistName", mArtistAdapter.getItem((int) id).getName());
                         mArtistBundle.putString("artistId", mArtistAdapter.getItem((int) id).getId());

                         Fragment fragTopTen = new ResultsTopTenFragment();
                         Fragment fragTitle = new TitleFragment();
                         //get fragment manager
                         FragmentManager fm = getFragmentManager();

                         // get fragment transition
                         FragmentTransaction ft = fm.beginTransaction();

                         // next we need to execute
                         // get fragment class
                         fragTopTen.setArguments(mArtistBundle);
                         fragTitle.setArguments(mArtistBundle);

                         ft.replace(R.id.topTenfragment, fragTopTen, "Detail");
                         ft.replace(R.id.title_frame_layout, fragTitle, "Title");
                         ft.commit();

                 } else {
                     // Use bundle to pass data to calling fragment
                     Bundle mArtistBundle = new Bundle();
                     mArtistBundle.putString("artistName", mArtistAdapter.getItem((int) id).getName());
                     mArtistBundle.putString("artistId", mArtistAdapter.getItem((int) id).getId());

                     // we need to populate fragment with visability set to gone to allow state change to landscape
                     Fragment fragTopTen = new ResultsTopTenFragment();
                     FragmentManager fm = getFragmentManager();
                     FragmentTransaction ft = fm.beginTransaction();
                     fragTopTen.setArguments(mArtistBundle);
                     ft.replace(R.id.topTenfragment, fragTopTen, "Detail");
                     ft.commit();

                     // Creating new intent activity to display results within new window
                     Intent i = new Intent();
                     i.setClass(getActivity(), TopTenActivity.class);
                     i.putExtras(mArtistBundle);
                     startActivity(i);
                 }
                 //storing position of item clicked
                 mPosition = position;
             }
         });

        // if bundle contains key for stored position then set to method varible mPosition
        if (savedInstanceState != null && savedInstanceState.containsKey(LIST_INSTANCE_STATE)){
            mPosition = savedInstanceState.getInt(LIST_INSTANCE_STATE);
        }

        FrameLayout.LayoutParams lv_params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        lv_results_artist.setLayoutParams(lv_params);
        return v;
    }

    // DRY method to update list adapter
    public static void updateAdapter() {
        Handler updateUi = new Handler(Looper.getMainLooper());
        updateUi.post(new Runnable() {
            @Override
            public void run() {
            mArtistAdapter.notifyDataSetChanged();

            }
        });
    }



}

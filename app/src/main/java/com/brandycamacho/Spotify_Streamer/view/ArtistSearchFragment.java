package com.brandycamacho.Spotify_Streamer.view;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.FetchArtistData;

/**
 * Created by brandycamacho on 6/10/15.
 */
public class ArtistSearchFragment extends Fragment {
    // Create view
    View v;
    SearchView searchView;
    int searchCount = 0;
    String searchText;
    boolean alreadySearched = false;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("created", "true");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // apply view by using inflater to inflate view with fragment data
        v = inflater.inflate(R.layout.fragment_activity_search_artist, container, false);
        LinearLayout ll = (LinearLayout) v.findViewById(R.id.ll_search_artist);
        searchView = new SearchView(getActivity());
        searchView.setIconified(false);
        searchView.setActivated(true);
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        searchView.setQueryHint("Search Artist...");
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String queryString) {
                        FetchArtistData fd = new FetchArtistData(getActivity());
                        fd.getArtistList(queryString);
                        alreadySearched = true;
                        searchView.clearFocus(); // close the keyboard on load

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        alreadySearched = false;
                        searchCount++;
                        searchText = newText;
                        SearchHandler.sendMessageDelayed(SearchHandler.obtainMessage(searchCount), 2000);
                        return false;
                    }
                });
        ll.addView(searchView);
        return v;
    }

    public static void ResultCheck(final Context context, String message) {
        // http://developer.android.com/tools/support-library/features.html#design
        String data = "Sorry " + message + " not found, try again";
        backgroundToast(context, data);
    }

    // Handler is used due to UI and background thread needing to be seperate.
    private Handler SearchHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (alreadySearched == true) {
                // do nothing as end user already searched value
            } else if (msg.what == 1) {
                // we want to do nothing at this point. By checking count to msg and the results are
                // 1=1 then the Artist name is too small and prevents creating additional
                // threads upon screen rotation. This also elevates the need to store value within saveInstance, is it wrong to do it this way?

            } else if (msg.what == searchCount) {
                if (!searchText.isEmpty() || !searchText.equals("")) {
                    Log.v("DATA", String.valueOf(searchText));
                    FetchArtistData fd = new FetchArtistData(getActivity());
                    fd.getArtistList(searchText);
                    searchView.clearFocus(); // close the keyboard on load
                }
                System.out.println("Success" + searchCount + "=" + msg.what);
            } else {
                System.out.println(msg.what + " - " + searchCount);
            }
        }
    };

    // I needed a way to communicate a toast message using fragment activities which is why I used Context with Looper for background thread
    public static void backgroundToast(final Context context,
                                       final String msg) {
        if (context != null && msg != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("Artist : ", "Paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        alreadySearched = true;
        Log.v("searchFragment", "onResumed");
    }
}
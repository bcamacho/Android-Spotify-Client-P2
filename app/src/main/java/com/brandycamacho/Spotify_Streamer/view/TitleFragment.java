package com.brandycamacho.Spotify_Streamer.view;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brandycamacho.Spotify_Streamer.R;

/**
 * Created by brandycamacho on 6/18/15.
 */
public class TitleFragment extends Fragment {
    String TAG = this.toString();
    // Create view
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // apply view by using inflater to inflate view with fragment data
        v = inflater.inflate(R.layout.fragment_activity_search_artist, container, false);
        Log.v(TAG, "onCreateView");
        LinearLayout ll_search_artist = (LinearLayout) v.findViewById(R.id.ll_search_artist);
        TextView tv_Title = new TextView(getActivity());
        Bundle mGetTitleBundle = this.getArguments();
        if (mGetTitleBundle != null) {
            tv_Title.setText(mGetTitleBundle.getString("artistName") + " Top 10");
        } else {
            tv_Title.setText(R.string.error_artist_title);
        }
        tv_Title.setTextSize(22);
        ll_search_artist.addView(tv_Title);
        return v;
    }

}

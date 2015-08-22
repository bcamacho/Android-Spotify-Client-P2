package com.brandycamacho.Spotify_Streamer.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.brandycamacho.Spotify_Streamer.model.Artist;
import com.brandycamacho.Spotify_Streamer.view.ArtistResultsFragment;
import com.brandycamacho.Spotify_Streamer.view.ArtistSearchFragment;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.client.Response;

/**
 * Created by brandycamacho on 6/16/15.
 * I desire to centralized data management which is why I separated fetch artist, this allowed ease of use with search fragment.
 */
public class FetchArtistData extends ContextWrapper {

    String TAG = this.toString();
    private ProgressDialog progressDialog;
    SpotifyApi api;
    SpotifyService spotify;

    public FetchArtistData(Context ctxBase) {
        super(ctxBase);
    }


    // separate artist data into separate class file.
    public void getArtistList(String name) {
        Log.v(TAG, "getArtistList");

        try {
            final String artistSearch = name;
            // instantiate new progress dialog
            progressDialog = new ProgressDialog(this);
            // spinner (wheel) style dialog
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // better yet - use a string resource getString(R.string.your_message)
            progressDialog.setMessage("Loading data");
            // display dialog
            progressDialog.show();
            api = new SpotifyApi();
            spotify = api.getService();

            spotify.searchArtists(name, new SpotifyCallback<ArtistsPager>() {
                @Override
                public void failure(SpotifyError spotifyError) {
                    ArtistResultsFragment.mArtistList.clear();
                    ArtistResultsFragment.mArtistList.add(new Artist("ERROR -> Check internet connection", null, null));
                    ArtistResultsFragment.updateAdapter();
                    progressDialog.dismiss();

                }

                @Override
                public void success(final ArtistsPager artistsPager, Response response) {

                    ArtistResultsFragment.mArtistList.clear();
                    Artist testArtist;
                    String name, url, id;

                    if (artistsPager.artists.items.size() == 0) {
                        ArtistResultsFragment.mArtistList.add(new Artist("Artist not found, try again", null, null));
                    } else {
                        for (int i = 0; i < artistsPager.artists.items.size(); i++) {

                            kaaes.spotify.webapi.android.models.Artist data = artistsPager.artists.items.get(i);
                            name = data.name;
                            if (data.images.size() != 0) {
                                url = data.images.get(0).url;
                            } else {
                                url = null;
                            }
                            id = data.id;
                            testArtist = new Artist(name, id, url);
                            ArtistResultsFragment.mArtistList.add(testArtist);
                        }
                    }
                    // Tell adapter to update data
                    ArtistResultsFragment.updateAdapter();
                    progressDialog.dismiss();
                    // check if search results contained data, if not display toast message to end user
                    if (artistsPager.artists.items.size() == 0)
                        ArtistSearchFragment.ResultCheck(getApplicationContext(), artistSearch);
                }
            });
        } catch (Exception e) {
            Log.e("ERROR : ", e.toString());

        }


    }
}

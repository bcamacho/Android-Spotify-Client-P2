package com.brandycamacho.Spotify_Streamer.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.model.Artist;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by brandycamacho on 6/16/15.
 */
public class ArtistTopTracksAdapter<A> extends ArrayAdapter<Artist> implements Serializable {
    // declaring our ArrayList of Artist
    private ArrayList<Artist> objects;
    String TAG = this.toString();

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
	* because it is the list of objects we want to display.
	*/
    public ArtistTopTracksAdapter(Context context, int textViewResourceId, ArrayList<Artist> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_view_artist_top_tracks, null);
        }

		/*
         * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        Artist i = objects.get(position);

        if (i != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView tv_track_name = (TextView) v.findViewById(R.id.list_view_tv_track_name);
            TextView tv_album_name = (TextView) v.findViewById(R.id.list_view_tv_album_name);
            ImageView iv_album_art = (ImageView) v.findViewById(R.id.list_item_icon);


            // check to see if each individual textview is null.
            // if not, assign some text!
            if (tv_track_name != null) {
                tv_track_name.setText(i.getTrackTitle());
            }
            if (tv_album_name != null) {
                tv_album_name.setText(i.getAlbum());
            }
            if (iv_album_art != null) {

                Picasso.with(getContext())
                        .load(i.getAlbum_art())
                        .placeholder(R.drawable.abc_ic_voice_search_api_mtrl_alpha)
                        .error(R.drawable.abc_btn_rating_star_on_mtrl_alpha)
                        .resize(40, 40)
                        .centerCrop()
                        .into(iv_album_art);

            }

        }

        // the view must be returned to our activity
        return v;

    }


}

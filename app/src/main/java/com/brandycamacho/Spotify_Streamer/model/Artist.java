package com.brandycamacho.Spotify_Streamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by brandycamacho on 6/12/15.
 * This class is used to control Artist Data which can be stored into a ListArray
 */
public class Artist implements Parcelable, Serializable {
    private String name;
    private String album;
    private String trackTitle;
    private String trackId;
    private String album_art;
    private String id;

    public Artist(String artistName, String trackTitle, String trackId, String artistId, String album_art, String album) {
        this.name = artistName;
        this.album = album;
        this.album_art = album_art;
        this.trackTitle = trackTitle;
        this.trackId = trackId;
        this.id = artistId;

    }

    public Artist(String name, String artistId, String album_art) {
        this.name = name;
        this.id = artistId;
        this.album_art = album_art;
        this.trackTitle = null;
        this.album = null;
        this.trackId = null;

    }


    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setTrackTitle(String trackTitle) {
        this.trackTitle = trackTitle;
    }

    public void setAlbum_art(String album_art) {
        this.album_art = album_art;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbum_art() {
        return album_art;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(album);
        out.writeString(trackTitle);
        out.writeString(trackId);
        out.writeString(album_art);
        out.writeString(id);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Creator<Artist>() {

        public Artist createFromParcel(Parcel in) {
            Artist artist = new Artist(null, null, null, null, null, null);
            artist.name = in.readString();
            artist.album = in.readString();
            artist.trackTitle = in.readString();
            artist.trackId = in.readString();
            artist.album_art = in.readString();
            artist.id = in.readString();
            return artist;
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };


}


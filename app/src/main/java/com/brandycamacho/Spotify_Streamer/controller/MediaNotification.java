package com.brandycamacho.Spotify_Streamer.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.view.MainStreamerActivity;
import com.squareup.picasso.Picasso;

/**
 * Created by brandycamacho on 8/21/15.
 */
public class MediaNotification {
    private Context parent;
    private NotificationManager nManager;
    private Notification notification;
    private NotificationCompat.Builder nBuilder;
    private RemoteViews remoteView;


    public MediaNotification(Context ctx, String artistName, String trackName, String albumArt) {
        this.parent = ctx;

        // Intent to return to application after selecting notification
        Intent notificationIntent = new Intent(parent, MainStreamerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(parent, 3, notificationIntent, 0);

        // Configuring remoteView for Notification builder
        remoteView = new RemoteViews(parent.getPackageName(), R.layout.notification);
        remoteView.setImageViewResource(R.id.iv_album_art, 999);
        remoteView.setTextViewText(R.id.tv_artist_name, artistName);
        remoteView.setTextViewText(R.id.tv_track_title, trackName);


        // Attaching notification to builder
        nBuilder = new NotificationCompat.Builder(parent)
                .setContentTitle("Track Notification")
                .setContentText("Playing track")
                .setSmallIcon(R.drawable.icon)
                .setOngoing(false);

        //set the button listeners
        setListeners(remoteView);
        nBuilder.setContent(remoteView);
        nBuilder.setContentIntent(intent);
        notification = nBuilder.build();
        // set big content view for newer androids
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification.bigContentView = remoteView;
        }
        nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(2, notification);
        // use picasso to load album art to remote view for notification
        Picasso.with(parent).load(albumArt).into(remoteView, R.id.iv_album_art, 2, notification);
    }

    public void setListeners(RemoteViews view) {

        //listener 1
        Intent pause = new Intent(parent, NotificationReturn.class);
        pause.putExtra("DO", "pause");
        PendingIntent pend_pause = PendingIntent.getActivity(parent, 0, pause, 0);
        view.setOnClickPendingIntent(R.id.btn_pause, pend_pause);

        //listener 2
        Intent next = new Intent(parent, NotificationReturn.class);
        next.putExtra("DO", "next");
        PendingIntent pend_next = PendingIntent.getActivity(parent, 1, next, 0);
        view.setOnClickPendingIntent(R.id.btn_next, pend_next);

        //listener 3
        Intent previous = new Intent(parent, NotificationReturn.class);
        previous.putExtra("DO", "previous");
        PendingIntent pend_previous = PendingIntent.getActivity(parent, 2, previous, 0);
        view.setOnClickPendingIntent(R.id.btn_previous, pend_previous);

    }

    public static void cancel(Context ctx) {
        Context parent = ctx;
        NotificationManager nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancelAll();
    }
}
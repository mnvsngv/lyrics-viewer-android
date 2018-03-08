package com.lyricsviewer.receiver;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lyricsviewer.R;
import com.lyricsviewer.Utils;
import com.lyricsviewer.song.SongContent;

import static android.support.v4.app.NotificationManagerCompat.from;
import static com.lyricsviewer.SongListActivity.nowPlaying;

public class MusicBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final long longId = intent.getLongExtra("id", 0);
        final String id = String.valueOf(longId);
        SongContent.Song song = SongContent.ITEM_MAP.get(id);

        ImageView imageView;
        TextView title;
        TextView artist;

        if(nowPlaying != null && song != null) {
            imageView = (ImageView) nowPlaying.findViewById(R.id.imageView);
            title = (TextView) nowPlaying.findViewById(R.id.title);
            artist = (TextView) nowPlaying.findViewById(R.id.artist);

            imageView.setImageBitmap(Utils.getSongThumbnail(song, 300));
            title.setText(song.title);
            artist.setText(song.artist);

            if(nowPlaying != null) {
                nowPlaying.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.fireLyricsActivityIntent(v.getContext(), id);
                    }
                });
            }
        }

        if(song != null) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(song.title);
            builder.setContentText(song.artist);
            builder.setSubText("Open MP3 Lyrics!");
            builder.setLargeIcon(BitmapFactory.decodeFile(song.albumArtPath));
            builder.setTicker("Lyrics Finder");
            builder.setSmallIcon(R.drawable.ic_stat_icon);
            builder.setPriority(Notification.PRIORITY_MIN);

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(context, 0,
                            Utils.createLyricsActivityIntent(context, id),
                            PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);
//                builder.addAction(new android.support.v4.app.NotificationCompat.Action.Builder(
//                        0, "TEST", pendingIntent).build());


            Notification notification = builder.build();
            NotificationManagerCompat notificationManger = from(context);
            notificationManger.notify(0, notification);
        }
    }
}

package com.lyricsviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lyricsviewer.song.SongContent;

public class Utils {
    public static Bitmap getSongThumbnail(SongContent.Song song, int size) {
        if(song.albumArtPath != null) {
            return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(song.albumArtPath), size, size, false);
        }
        return null;
    }

    public static void fireLyricsActivityIntent(Context context, String id) {
        context.startActivity(createLyricsActivityIntent(context, id));
    }

    public static Intent createLyricsActivityIntent(Context context, String id) {
        Intent intent = new Intent(context, SongDetailActivity.class);
        intent.putExtra(SongDetailFragment.ARG_ITEM_ID, id);
        return intent;
    }
}

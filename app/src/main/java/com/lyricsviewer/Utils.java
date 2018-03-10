package com.lyricsviewer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.lyricsviewer.song.SongContent;

import java.util.ArrayList;
import java.util.List;

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

    public static SongContent.Song queryDatabaseForSong(Context context, long id) {
        return getMusicFromDatabase(context, String.valueOf(id)).get(0);
    }

    public static List<SongContent.Song> getMusicFromDatabase(Context context, String id) {
        List<SongContent.Song> songs = new ArrayList<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        ContentResolver musicResolver = context.getContentResolver();

        Cursor musicCursor = null;
        if(id != null) {
            musicCursor = musicResolver.query(musicUri,
                    null,
                    MediaStore.Audio.Media._ID + "=?",
                    new String[] { id },
                    MediaStore.Audio.Media.ALBUM_ID);
        } else {
            musicCursor = musicResolver.query(musicUri,
                    null,
                    null,
                    null,
                    MediaStore.Audio.Media.ALBUM_ID);
        }

        int titleColumn = musicCursor.getColumnIndex
                (MediaStore.Audio.Media.TITLE);
        int idColumn = musicCursor.getColumnIndex
                (MediaStore.Audio.Media._ID);
        int artistColumn = musicCursor.getColumnIndex
                (MediaStore.Audio.Media.ARTIST);
        int dataColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);


        if(id != null) {
            if(!musicCursor.isAfterLast()) {
                musicCursor.moveToFirst();
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String filePath = musicCursor.getString(dataColumn);
                long albumId = musicCursor.getLong(albumIdColumn);
                String albumArtPath = null;
                SongContent.Song song = null;

                Cursor singleAlbumCursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        null,
                        MediaStore.Audio.Albums._ID + "=?",
                        new String[]{String.valueOf(albumId)},
                        MediaStore.Audio.Albums._ID);
                if(!singleAlbumCursor.isAfterLast()) {
                    singleAlbumCursor.moveToFirst();
                    int albumArtColumn = singleAlbumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                    albumArtPath = singleAlbumCursor.getString(albumArtColumn);
                }

                song = new SongContent.Song(String.valueOf(thisId), thisTitle, thisArtist, filePath, albumId, albumArtPath);

                songs.add(song);
            }
        } else {
            Cursor albumCursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    MediaStore.Audio.Albums._ID);
            int albumArtColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);

            CursorJoiner joiner = new CursorJoiner(musicCursor, new String[] {MediaStore.Audio.Media.ALBUM_ID},
                    albumCursor, new String[] {MediaStore.Audio.Albums._ID});

            long previousAlbumId = -1;
            String previousAlbumArtPath = null;

            musicCursor.moveToFirst();
            albumCursor.moveToFirst();

            for (CursorJoiner.Result result : joiner) {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String filePath = musicCursor.getString(dataColumn);
                long albumId = musicCursor.getLong(albumIdColumn);
                String albumArtPath = null;
                SongContent.Song song = null;

                switch (result) {
                    case LEFT:
                        if (albumId == previousAlbumId) {
                            albumArtPath = previousAlbumArtPath;
                        }
                        break;

                    case BOTH:
                        albumArtPath = albumCursor.getString(albumArtColumn);
                        previousAlbumId = albumId;
                        previousAlbumArtPath = albumArtPath;
                        break;

                    default:
                        break;
                }

                song = new SongContent.Song(String.valueOf(thisId),
                        thisTitle, thisArtist, filePath, albumId, albumArtPath);

                songs.add(song);
            }
        }

        return songs;
    }
}

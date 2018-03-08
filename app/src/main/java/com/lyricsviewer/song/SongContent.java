package com.lyricsviewer.song;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongContent {

    public static final List<Song> ITEMS = new ArrayList<Song>();
    public static final Map<String, Song> ITEM_MAP = new HashMap<String, Song>();

    public static void addItem(Song item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class Song implements Comparable<SongContent.Song> {
        public final String id;
        public final String title;
        public final String artist;
        public final String filePath;
        public final long albumId;
        public final String albumArtPath;

        public Song(String id, String title, String artist, String filePath, long albumId, String albumArtPath) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.filePath = filePath;
            this.albumId = albumId;
            this.albumArtPath = albumArtPath;
        }

        @Override
        public String toString() {
            return title;
        }

        @Override
        public int compareTo(@NonNull Song o) {
            return this.title.compareTo(o.title);
        }
    }
}

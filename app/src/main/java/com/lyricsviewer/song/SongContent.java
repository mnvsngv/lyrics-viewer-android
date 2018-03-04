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
        public final String titleArtist;
        public final String filePath;
        public final long albumId;
        public Bitmap thumbnail;

        public Song(String id, String titleArtist, String filePath, long albumId) {
            this.id = id;
            this.titleArtist = titleArtist;
            this.filePath = filePath;
            this.albumId = albumId;
        }

        @Override
        public String toString() {
            return titleArtist;
        }

        @Override
        public int compareTo(@NonNull Song o) {
            return this.titleArtist.compareTo(o.titleArtist);
        }
    }
}

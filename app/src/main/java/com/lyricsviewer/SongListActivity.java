package com.lyricsviewer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.lyricsviewer.codecomputerlove.fastrecyclerviewdemo.FastScrollRecyclerViewInterface;
import com.lyricsviewer.codecomputerlove.fastrecyclerviewdemo.FastScrollRecyclerViewItemDecoration;
import com.lyricsviewer.song.SongContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * An activity representing a list of Songs. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SongDetailActivity} representing
 * item filePath. On tablets, the activity presents the list of items and
 * item filePath side-by-side using two vertical panes.
 */
public class SongListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static RecyclerView recyclerView;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        View recyclerView = findViewById(R.id.song_list);
        assert recyclerView != null;
        this.recyclerView = (RecyclerView) recyclerView;
        setupRecyclerView(this.recyclerView);

        if (findViewById(R.id.song_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        if (SongContent.ITEMS.size() == 0) {
            Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            ContentResolver musicResolver = getContentResolver();

            Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int dataColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            Cursor albumCursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    null,
                    null,
                    null);
            int albumArtColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);

            CursorJoiner joiner = new CursorJoiner(musicCursor, new String[] {MediaStore.Audio.Media.ALBUM_ID},
                    albumCursor, new String[] {MediaStore.Audio.Albums._ID});

            for(CursorJoiner.Result result : joiner) {
                switch(result) {
                    case BOTH:
                        long thisId = musicCursor.getLong(idColumn);
                        String thisTitle = musicCursor.getString(titleColumn);
                        String thisArtist = musicCursor.getString(artistColumn);
                        String filePath = musicCursor.getString(dataColumn);
                        long albumId = musicCursor.getLong(albumIdColumn);
                        String albumArtPath = albumCursor.getString(albumArtColumn);

                        SongContent.Song song = new SongContent.Song(String.valueOf(thisId),
                                thisArtist + " - " + thisTitle, filePath, albumId, albumArtPath);
//                        if(albumArtPath != null)
//                            song.thumbnail = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(albumArtPath), 200, 200, false);

                        SongContent.addItem(song);
                        break;

                    case LEFT:

                        break;

                    default:
                        break;
                }
            }

            Collections.sort(SongContent.ITEMS);

            new ThumbnailsUpdateTask().execute(SongContent.ITEMS.toArray(new SongContent.Song[0]));
        }
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(SongContent.ITEMS, calculateIndexesForName(SongContent.ITEMS)));
        FastScrollRecyclerViewItemDecoration decoration = new FastScrollRecyclerViewItemDecoration(this);
        recyclerView.addItemDecoration(decoration);
    }

    public void getThumbnails() {
        for(SongContent.Song song : SongContent.ITEMS) {
            if(song.albumArtPath != null) {
                song.thumbnail = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(song.albumArtPath), 200, 200, false);
            }
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if(adapter != null && !recyclerView.isComputingLayout()) adapter.notifyDataSetChanged();
        }
    }

    private HashMap<String, Integer> calculateIndexesForName(List<SongContent.Song> items) {
        HashMap<String, Integer> mapIndex = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < items.size(); i++) {
            String name = items.get(i).titleArtist;
            String index = name.substring(0, 1);
            index = index.toUpperCase();

            // TODO Put all non alphabetical indices into a single index of "#"
            if (!mapIndex.containsKey(index)) {
                mapIndex.put(index, i);
            }
        }
        return mapIndex;
    }

    private Bitmap getThumbnail(long albumId) {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID+ "=?",
                new String[] {String.valueOf(albumId)},
                null);

        if (cursor.moveToFirst()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            return BitmapFactory.decodeFile(path);
        } else return null;
    }

    private static class ThumbnailsUpdateTask extends AsyncTask<SongContent.Song, Integer, Integer> {

        @Override
        protected Integer doInBackground(SongContent.Song... songs) {
            for(SongContent.Song song : SongContent.ITEMS) {
                if(song.albumArtPath != null) {
                    song.thumbnail = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(song.albumArtPath), 200, 200, false);
                }
                publishProgress(5);
            }
            return SongContent.ITEMS.size();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if(adapter != null && !recyclerView.isComputingLayout()) adapter.notifyDataSetChanged();
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> implements FastScrollRecyclerViewInterface {

        private final List<SongContent.Song> mValues;
        private HashMap<String, Integer> mMapIndex;

        public SimpleItemRecyclerViewAdapter(List<SongContent.Song> items, HashMap<String, Integer> index) {
            mValues = items;
            this.mMapIndex = index;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.song_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mContentView.setText(mValues.get(position).titleArtist);

//            GlideApp.with(this).load(mValues.get(position).thumbnail).into(imageView);

            holder.mImageView.setImageBitmap(mValues.get(position).thumbnail);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(SongDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        SongDetailFragment fragment = new SongDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.song_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, SongDetailActivity.class);
                        intent.putExtra(SongDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
            isBound = true;
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        @Override
        public HashMap<String, Integer> getMapIndex() {
            return this.mMapIndex;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImageView;
            public final TextView mContentView;
            public SongContent.Song mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = (TextView) view.findViewById(R.id.titleArtist);
                mImageView = (ImageView) view.findViewById(R.id.imageView);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}

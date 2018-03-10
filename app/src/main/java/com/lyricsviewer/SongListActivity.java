package com.lyricsviewer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.lyricsviewer.codecomputerlove.fastrecyclerviewdemo.FastScrollRecyclerViewInterface;
import com.lyricsviewer.codecomputerlove.fastrecyclerviewdemo.FastScrollRecyclerViewItemDecoration;
import com.lyricsviewer.decoration.SimpleDividerItemDecoration;
import com.lyricsviewer.song.SongContent;
import com.squareup.picasso.Picasso;

import java.io.File;
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
    public static View nowPlaying;
    private Service mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        nowPlaying = findViewById(R.id.nowPlaying);
        ImageView imageView = (ImageView) nowPlaying.findViewById(R.id.imageView);
        TextView title = (TextView) nowPlaying.findViewById(R.id.title);
        TextView artist = (TextView) nowPlaying.findViewById(R.id.artist);

        imageView.setImageBitmap(null);
        title.setText("<no song>");
        artist.setText("<unknown>");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.song_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

        if (findViewById(R.id.song_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        Intent serviceIntent = new Intent(this, MusicReceiverService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        if (SongContent.ITEMS.size() == 0) {
            SongContent.ITEMS.addAll(Utils.getMusicFromDatabase(this, null));

            Collections.sort(SongContent.ITEMS);
        }
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(SongContent.ITEMS, calculateIndexesForName(SongContent.ITEMS)));
        FastScrollRecyclerViewItemDecoration decoration = new FastScrollRecyclerViewItemDecoration(this);

        recyclerView.addItemDecoration(decoration);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
    }

    private HashMap<String, Integer> calculateIndexesForName(List<SongContent.Song> items) {
        HashMap<String, Integer> mapIndex = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < items.size(); i++) {
            String name = items.get(i).title;
            String index = name.substring(0, 1);
            index = index.toUpperCase();

            // TODO Put all non alphabetical indices into a single index of "#"
            if (!mapIndex.containsKey(index)) {
                mapIndex.put(index, i);
            }
        }
        return mapIndex;
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
            SongContent.Song song = mValues.get(position);
            holder.mItem = song;

            holder.mTitleView.setText(song.title);
            holder.mTitleView.setSelected(true);
            holder.mArtistView.setText(song.artist);
            holder.mArtistView.setSelected(true);

            if(song.albumArtPath!= null) {
                Picasso.with(getApplicationContext())
                        .load(new File(song.albumArtPath))
                        .resize(150, 150)
                        .into(holder.mImageView);
            }

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
                        Utils.fireLyricsActivityIntent(v.getContext(), holder.mItem.id);
                    }
                }
            });
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
            public final TextView mTitleView;
            public final TextView mArtistView;
            public SongContent.Song mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.title);
                mArtistView = (TextView) view.findViewById(R.id.artist);
                mImageView = (ImageView) view.findViewById(R.id.imageView);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((MusicReceiverService.MusicBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

}

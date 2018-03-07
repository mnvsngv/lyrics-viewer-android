package com.lyricsviewer;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lyricsviewer.song.SongContent;

import java.io.IOException;

/**
 * A fragment representing a single Song detail screen.
 * This fragment is either contained in a {@link SongListActivity}
 * in two-pane mode (on tablets) or a {@link SongDetailActivity}
 * on handsets.
 */
public class SongDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy title this fragment is presenting.
     */
    private SongContent.Song mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy title specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load title from a title provider.
            mItem = SongContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.title);
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int height = (int) (metrics.widthPixels / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                appBarLayout.setMinimumHeight(height);

                Bitmap albumArt = BitmapFactory.decodeFile(mItem.albumArtPath);
                appBarLayout.setContentScrim(new BitmapDrawable(getResources(), albumArt));
                appBarLayout.setScrimVisibleHeightTrigger(Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.song_detail, container, false);

        // Show the dummy title as text in a TextView.
        if (mItem != null) {
            try {
                ((TextView) rootView.findViewById(R.id.song_detail)).setText(LyricsFinder.findLyrics(mItem.filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return rootView;
    }
}

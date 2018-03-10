package com.lyricsviewer;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.os.Binder;
import android.os.IBinder;
import android.media.session.MediaController;
import android.widget.Toast;

import com.lyricsviewer.receiver.MusicBroadcastReceiver;

public class MusicReceiverService extends Service {
    public MusicReceiverService() {
    }

    /*@Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        // If we get killed, after returning from here, restart
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.music.metachanged");
        intentFilter.addAction("com.android.music.playstatechanged");
        intentFilter.addAction("com.android.music.playbackcomplete");
        intentFilter.addAction("com.android.music.queuechanged");

        registerReceiver(new MusicBroadcastReceiver(), intentFilter);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MusicBinder extends Binder {
        public MusicReceiverService getService() {
            return MusicReceiverService.this;
        }
    }
}

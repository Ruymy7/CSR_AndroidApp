package com.google.sample.cast.refplayer.liveradio;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;

import static com.android.volley.VolleyLog.TAG;

public class LiveRadioPlayer extends Thread implements MediaPlayer.OnPreparedListener {
    public static final String ACTION_PLAY = "com.liveRadioPlayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.liveRadioPlayer.action.PAUSE";
    MediaPlayer mediaPlayer = null;
    private String radio_url = "http://server10.emitironline.com:10288/stream";

    public LiveRadioPlayer() {

    }

    public void run() {
        try {
            Log.d(TAG, "onStartCommand: STARTING");
            mediaPlayer = new MediaPlayer(); // initialize it here
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(radio_url);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync(); // prepare async to not block main thread
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
    }
}

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

public class LiveRadioPlayer extends Service implements MediaPlayer.OnPreparedListener {
    public static final String ACTION_PLAY = "com.liveRadioPlayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.liveRadioPlayer.action.PAUSE";
    MediaPlayer mediaPlayer = null;
    private String radio_url = "http://server10.emitironline.com:10288/stream";

    public LiveRadioPlayer() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equals(ACTION_PLAY)){
            try {
                Toast.makeText(this, "PLAY", Toast.LENGTH_SHORT).show();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(radio_url);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(intent.getAction().equals(ACTION_PAUSE)){
            Toast.makeText(this, "PAUSE", Toast.LENGTH_SHORT).show();
            pause();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED);
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }
}

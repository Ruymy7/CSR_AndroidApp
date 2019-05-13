package com.google.sample.cast.refplayer.liveradio;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.VideoBrowserActivity;
import com.google.sample.cast.refplayer.queue.ui.QueueListViewActivity;
import com.google.sample.cast.refplayer.settings.CastPreference;

import java.io.IOException;

public class LiveRadio extends AppCompatActivity {

    private static final String TAG = "LiveRadio";
    private ImageButton playButton;
    private SeekBar volumeBar;
    private ImageButton pauseButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_radio);
        getButtons();
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
    }

    private void getButtons() {
        playButton = findViewById(R.id.play_button);
        pauseButton = findViewById(R.id.pause_button);
        volumeBar = findViewById(R.id.volume_bar);

        if(VideoBrowserActivity.mLiveState == VideoBrowserActivity.LiveState.PAUSE){
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        } else {
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    private void play() {
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        Log.d(TAG, "play: ");
        if(VideoBrowserActivity.mLiveState == VideoBrowserActivity.LiveState.PAUSE) {
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
            VideoBrowserActivity.liveRadioPlayer.start();
            VideoBrowserActivity.mLiveState = VideoBrowserActivity.LiveState.PLAYING;
        } else {
            Toast.makeText(this, "Is Alive", Toast.LENGTH_SHORT).show();
        }
    }

    private void pause() {
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        VideoBrowserActivity.mLiveState = VideoBrowserActivity.LiveState.PAUSE;
        VideoBrowserActivity.liveRadioPlayer.stop();
    }

}

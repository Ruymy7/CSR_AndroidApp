package com.google.sample.cast.refplayer.liveradio;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
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
import com.google.sample.cast.refplayer.browser.VideoListAdapter;
import com.google.sample.cast.refplayer.queue.ui.QueueListViewActivity;
import com.google.sample.cast.refplayer.settings.CastPreference;

import java.io.IOException;

public class LiveRadio extends Fragment {

    private static final String TAG = "LiveRadio";
    private ImageButton playButton;
    private SeekBar volumeBar;
    private ImageButton pauseButton;
    private AudioManager audioManager;
    private TextView textView;
    private ImageView image_mute;
    private ImageView image_volume;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.live_radio_fragment, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getButtons();
        setHasOptionsMenu(true);
    }


   /* @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = null;
        /*getButtons();
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(intent);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        seekBar.getProgress(), 0);
                if(seekBar.getProgress() == 0){
                    image_volume.setVisibility(View.GONE);
                    image_mute.setVisibility(View.VISIBLE);
                } else {
                    image_volume.setVisibility(View.VISIBLE);
                    image_mute.setVisibility(View.GONE);
                }
            }
        });

        getContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return false;
            }

            @Override
            public void onChange(boolean selfChange) {

                volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

                if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
                    image_volume.setVisibility(View.GONE);
                    image_mute.setVisibility(View.VISIBLE);
                } else {
                    image_volume.setVisibility(View.VISIBLE);
                    image_mute.setVisibility(View.GONE);
                }
            }
        });
    }*/


    private void getButtons() {
        playButton = getView().findViewById(R.id.play_button);
        pauseButton = getView().findViewById(R.id.pause_button);
        volumeBar = getView().findViewById(R.id.volume_bar);
        textView = getView().findViewById(R.id.text_liveRadio);
        image_mute = getView().findViewById(R.id.image_mute);
        image_volume = getView().findViewById(R.id.image_volume);
        //audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        /*volumeBar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
            image_volume.setVisibility(View.GONE);
            image_mute.setVisibility(View.VISIBLE);
        } else {
            image_volume.setVisibility(View.VISIBLE);
            image_mute.setVisibility(View.GONE);
        }*/

        if(VideoBrowserActivity.mLiveState == VideoBrowserActivity.LiveState.PAUSE){
            textView.setTextColor(getResources().getColor(R.color.red));
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        } else {
            textView.setTextColor(getResources().getColor(R.color.red));
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    /*private void play(Intent intent) {
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        textView.setTextColor(getResources().getColor(R.color.red));
        Log.d(TAG, "play: ");
        if(VideoBrowserActivity.mLiveState == VideoBrowserActivity.LiveState.PAUSE) {
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
            intent = new Intent(this, LiveRadioPlayer.class);
            intent.setAction(LiveRadioPlayer.ACTION_PLAY);
            startService(intent);
            //VideoBrowserActivity.liveRadioPlayer;
            VideoBrowserActivity.mLiveState = VideoBrowserActivity.LiveState.PLAYING;
        } else {
            Toast.makeText(this, "Is Alive", Toast.LENGTH_SHORT).show();
        }
    }

    private void pause() {
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        textView.setTextColor(getResources().getColor(R.color.black));
        VideoBrowserActivity.mLiveState = VideoBrowserActivity.LiveState.PAUSE;
        //VideoBrowserActivity.liveRadioPlayer.stop();
    }*/
}

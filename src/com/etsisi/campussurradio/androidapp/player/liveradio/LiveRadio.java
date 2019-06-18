/*
 * Copyright 2019 Padrón Castañeda, Ruymán
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.etsisi.campussurradio.androidapp.player.liveradio;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.etsisi.campussurradio.androidapp.player.expandedcontrols.ExpandedControlsActivity;
import com.etsisi.campussurradio.androidapp.player.queue.QueueDataProvider;
import com.etsisi.campussurradio.androidapp.player.utils.Utils;
import com.google.android.gms.cast.MediaInfo;
import com.etsisi.campussurradio.androidapp.player.R;
import com.etsisi.campussurradio.androidapp.player.browser.VideoProvider;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import java.util.List;

public class LiveRadio extends Fragment {

    private static final String TAG = "LiveRadio";
    private ImageButton playButton;
    private SeekBar volumeBar;
    private ImageButton pauseButton;
    private AudioManager audioManager;
    private TextView textView;
    private ImageView image_mute;
    private ImageView image_volume;
    private String media = "http://server10.emitironline.com:10288/stream";
    private MediaInfo item;
    private ContentObserver contentObserver;
    private int lastVolume = 0;
    private LiveRadioPlayer player = null;
    private LiveRadioPlayer.LocalBinder binder = null;
    private Intent playerIntent;
    private QueueDataProvider provider;
    private boolean isPlayingRemotely = false;
    private int itemid = -1;
    private CastSession castSession;
    private RemoteMediaClient remoteMediaClient;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.live_radio_fragment, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCast();
        playerIntent = new Intent(getContext(), LiveRadioPlayer.class);
        getContext().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        setHasOptionsMenu(true);
        createMediaInfo();
    }

    private void getCast() {
        castSession = CastContext.getSharedInstance(getContext()).getSessionManager().getCurrentCastSession();
        if(castSession != null)
            remoteMediaClient = castSession.getRemoteMediaClient();
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (LiveRadioPlayer.LocalBinder) service;
            player = binder.getService();
            getButtons();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            player.hideNotification();
            binder = null;
            player = null;
        }
    };

    private void getButtons() {
        provider = QueueDataProvider.getInstance(getContext());
        List<MediaQueueItem> queueList =  provider.getItems();
        for(int i = 0; i < queueList.size(); i++) {
            MediaQueueItem item = queueList.get(i);
            if(item != null && item.getMedia().getMetadata().getString(VideoProvider.TAG_VIDEO_URL).equals(media)) {
                itemid = item.getItemId();
                if(remoteMediaClient.isPlaying() && remoteMediaClient.getCurrentItem().getMedia().getMetadata().getString(VideoProvider.TAG_VIDEO_URL).equals(media))
                    isPlayingRemotely = true;
            }
        }
        playButton = getView().findViewById(R.id.play_button);
        pauseButton = getView().findViewById(R.id.pause_button);
        volumeBar = getView().findViewById(R.id.volume_bar);
        textView = getView().findViewById(R.id.text_liveRadio);
        image_mute = getView().findViewById(R.id.image_mute);
        image_volume = getView().findViewById(R.id.image_volume);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        volumeBar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
            image_volume.setVisibility(View.GONE);
            image_mute.setVisibility(View.VISIBLE);
        } else {
            image_volume.setVisibility(View.VISIBLE);
            image_mute.setVisibility(View.GONE);
        }

        if(!player.isPlaying() && !isPlayingRemotely){
            textView.setTextColor(getResources().getColor(R.color.black));
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        } else {
            textView.setTextColor(getResources().getColor(R.color.red));
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }

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
        image_volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastVolume = volumeBar.getProgress();
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                image_volume.setVisibility(View.GONE);
                image_mute.setVisibility(View.VISIBLE);
                volumeBar.setProgress(0);
            }
        });
        image_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0);
                image_volume.setVisibility(View.VISIBLE);
                image_mute.setVisibility(View.GONE);
                volumeBar.setProgress(lastVolume);
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

       contentObserver = new ContentObserver(new Handler()) {
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
        };

        getContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, contentObserver);
    }

    private void play() {
        Log.d(TAG, "play: ");
        getCast();
        if(itemid != -1 ) { // Si el streaming está en la cola pero no se esta reproduciendo, salta a él
            if(remoteMediaClient != null) {
                remoteMediaClient.queueJumpToItem(itemid, null);
                remoteMediaClient.play();
                setPlay();
            }
        } else if (!player.isPlaying()) {
            if(remoteMediaClient != null) {
                isPlayingRemotely = true;
                MediaQueueItem queueItem = new MediaQueueItem.Builder(item).setAutoplay(true).setPreloadTime(5).build();
                MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
                if (provider.isQueueDetached() && provider.getCount() > 0) {
                    MediaQueueItem[] items = Utils.rebuildQueueAndAppend(provider.getItems(), queueItem);
                    remoteMediaClient.queueLoad(items, provider.getCount(), MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
                } else if (provider.getCount() == 0) {
                    remoteMediaClient.queueLoad(newItemArray, 0, MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
                } else {
                    int currentId = provider.getCurrentItemId();
                    remoteMediaClient.queueInsertAndPlayItem(queueItem, currentId, null);
                }
                Intent intent = new Intent(getContext(), ExpandedControlsActivity.class);
                getContext().startActivity(intent);
                setPlay();
            } else {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                textView.setTextColor(getResources().getColor(R.color.red));
                playerIntent.putExtra("media", media);
                playerIntent.setAction(LiveRadioPlayer.STARTPLAYING);
                getContext().startService(playerIntent);
                Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "play: Player is already running");
        }
    }

    private void pause() {
        if(isPlayingRemotely) {
            if(remoteMediaClient != null) {
                remoteMediaClient.pause();
                itemid = provider.getCurrentItemId();
            } else {
                Toast.makeText(player, "No se ha podido pausar", Toast.LENGTH_SHORT).show();
            }
        } else {
            player.onDestroy();
        }
        setPause();
    }

    void setPause() {
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        textView.setTextColor(getResources().getColor(R.color.black));
    }

    void setPlay() {
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        textView.setTextColor(getResources().getColor(R.color.red));
    }

    private void createMediaInfo() {
        item = VideoProvider.buildMediaInfo("Campus Sur Radio", "Campus Sur Radio", "En directo",
                0, media, "audio/mpeg", "http://iaas92-43.cesvima.upm.es:2019/api/thumbnails/smallIcon.png",
                "http://iaas92-43.cesvima.upm.es:2019/api/thumbnails/icon.png", null, "", "");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: " + player + getContext());
        if(serviceConnection != null && player != null) {
            getContext().unbindService(serviceConnection);
            player = null;
            serviceConnection = null;
        }
        getContext().getContentResolver().unregisterContentObserver(contentObserver);
        super.onDestroy();
    }
}

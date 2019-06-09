package com.etsisi.campussurradio.androidapp.player.liveradio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.etsisi.campussurradio.androidapp.player.VideoBrowserActivity;
import com.etsisi.campussurradio.androidapp.player.R;

import java.io.IOException;

import static com.android.volley.VolleyLog.TAG;


public class LiveRadioPlayer extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private String URL;
    private AudioManager audioManager;

    public static String CHANNEL_ID = "csr_channel_01";
    public static int notification_id = 1;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder notification;

    public final static String STARTPLAYING = "com.campussurradio.liveradioplayer.startplaying";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            URL = intent.getExtras().getString("media");
        } catch (NullPointerException e) {
            stopSelf();
        }

        String action = intent.getAction();
        if(action.equals(STARTPLAYING)){
            if (URL != null && !URL.equals(""))
                initMediaPlayer();
        }

        if (!requestAudioFocus()) {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        hideNotification();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        removeAudioFocus();
        super.onDestroy();
    }

    public void showNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_ID,
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("CAMPUS_SUR_RADIO_NOTIFICATION");
            mNotificationManager.createNotificationChannel(channel);
        }
        Intent openIntent = new Intent(this, VideoBrowserActivity.class);
        openIntent.setAction("liveradio");
        PendingIntent pendingOpenIntent = PendingIntent.getActivity(this, 0, openIntent, 0);

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(false)
                .setContentIntent(pendingOpenIntent)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.on_live));

        notificationManagerCompat.notify(notification_id, notification.build());
    }

    public void hideNotification() {
        notificationManagerCompat.cancel(notification_id);
    }

    private void initMediaPlayer() {
        Log.d(TAG, "initMediaPlayer: ");
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(URL);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            showNotification();
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        hideNotification();
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }


    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus() {
        if(audioManager != null)
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
        return false;
    }

    class LocalBinder extends Binder {
        LiveRadioPlayer getService() {
            return LiveRadioPlayer.this;
        }
    }

    boolean isPlaying() {
        if(mediaPlayer != null)
            return mediaPlayer.isPlaying();
        return false;
    }

    public void onTaskRemoved(Intent rootIntent) {
        hideNotification();
        stopSelf();
    }
}

package com.example.mp3player;

import android.app.NotificationChannel;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.annotation.Nullable;
import androidx.media.session.MediaButtonReceiver;

public class Services<mediaButtonReceiver> extends Service {

    private boolean running = false;
    public static MP3Player player = new MP3Player();
    private String music_path;
    private String music_title;
    public static int duration;
    private String channel_id = "channel_id";
    private int notification_id = 100;
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        player = new MP3Player();
       createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String action = intent.getAction();

        if (action.equals(ACTION_PLAY)) {
            if (player.getState()== MP3Player.MP3PlayerState.STOPPED){
                player.load(music_path);
            }
            player.play();
        } else if (action.equals(ACTION_PAUSE)) {
            player.pause();
        } else if (action.equals(ACTION_STOP)) {
            player.stop();
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        } else {
            music_path = intent.getStringExtra("selected music");
            music_title = intent.getStringExtra("music title");
            player.load(music_path);
            CreateNotification();
            duration = player.getDuration();
        }

        return Service.START_STICKY;
    }

    public void onDestroy()
    {
        player.stop();
        running = false;
        super.onDestroy();
    }

    public void CreateNotification()
    {
        // intent for tapping the notification
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("selected music",  music_path);
        i.putExtra("music title", music_title);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // intent for play button in notification
        Intent play_intent = new Intent(this, Services.class);
        play_intent.putExtra("selected music",  music_path);
        play_intent.putExtra("music title", music_title);
        play_intent.setAction(ACTION_PLAY);

        // intent for pause button in notification
        Intent pause_intent = new Intent(this, Services.class);
        pause_intent.putExtra("selected music",  music_path);
        pause_intent.putExtra("music title", music_title);
        pause_intent.setAction(ACTION_PAUSE);

        // intent for stop button in notification
        Intent stop_intent = new Intent(this, Services.class);
        stop_intent.putExtra("selected music",  music_path);
        stop_intent.putExtra("music title", music_title);
        stop_intent.setAction(ACTION_STOP);

        // create pending intent based on the intent above
        PendingIntent pending_intent = PendingIntent.getActivity(this, 0, i, 0);
        PendingIntent pending_intent_play = PendingIntent.getService(this, 0, play_intent, 0);
        PendingIntent pending_intent_pause = PendingIntent.getService(this, 0, pause_intent, 0);
        PendingIntent pending_intent_stop = PendingIntent.getService(this, 0, stop_intent, 0);

        // create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);
        builder.setContentTitle("Currently Playing")
                .setContentText(music_path)
                .setContentIntent(pending_intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .addAction(android.R.drawable.ic_media_play, "Play", pending_intent_play)
                .addAction(android.R.drawable.ic_media_pause, "Pause", pending_intent_pause)
                .addAction(android.R.drawable.checkbox_off_background , "Stop", pending_intent_stop)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)))
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP));

        // notification manager
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(notification_id, builder.build());
        // start foreground service
        startForeground(notification_id, builder.build());
    }

    public void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = "MP3Player";
            String description = "MP3Player";
            NotificationChannel channel = new NotificationChannel(channel_id, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

}

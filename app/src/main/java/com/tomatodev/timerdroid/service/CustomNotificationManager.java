package com.tomatodev.timerdroid.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.activities.HomeActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomNotificationManager {

    private static final int SOUND_STREAM = AudioManager.STREAM_ALARM;

    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_ID_SOUND = 2;

    public static final String NOTIFICATION_CHANNEL_ID_FINISHED = "timerdroid_channel_finished";

    public static final String NOTIFICATION_CHANNEL_ID_RUNNING = "timerdroid_channel_running";

    private NotificationManager mNotificationManager;

    private Context mContext;

    public CustomNotificationManager(Context context) {
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) context.getSystemService(ns);
        mContext = context;

        this.createNotificationChannelAlarm();
        this.createNotificationChannelRunning();
    }

    private void createNotificationChannelAlarm(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        mNotificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID_RUNNING);
        mNotificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID_FINISHED);

        CharSequence name = "Timer Finished"; // TODO
        String description = "Timer finished";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_FINISHED, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setShowBadge(true);
//        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
        mChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void createNotificationChannelRunning(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        CharSequence name = "Timer Running"; // TODO
        String description = "Timer running";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_RUNNING, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(false);
        mChannel.enableVibration(false);
        mChannel.setShowBadge(false);
        mChannel.setSound(null, null);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private String getRunningTimersText(Collection<TimerService.CountDown> runningTimers){

        if (runningTimers.isEmpty()) {
            return mContext.getString(R.string.service_no_running_timers);
        }

        List<String> timerList = new ArrayList<>();
        for (TimerService.CountDown timer : runningTimers) {
            timerList.add(timer.getName());
        }

        return mContext.getString(R.string.service_running_timers)
                + ": "
                + TextUtils.join(", ", timerList);
    }

    private Notification createRunningTimersNotification(Collection<TimerService.CountDown> timers){

        Collection<TimerService.CountDown> runningTimers = new ArrayList<>();
        for (TimerService.CountDown timer : timers) {
            if (timer.isStarted()) {
                runningTimers.add(timer);
            }
        }

        long minimumTimeLeft = Long.MAX_VALUE;
        for (TimerService.CountDown countdown : runningTimers) {
            if (countdown.getTimeLeft() < minimumTimeLeft) {
                minimumTimeLeft = countdown.getTimeLeft();
            }
        }

        NotificationCompat.Builder notificationBuilder = getNotificationBuilder(NOTIFICATION_CHANNEL_ID_RUNNING);

        notificationBuilder.setNumber(runningTimers.size())
                .setContentText(getRunningTimersText(runningTimers))
                .setWhen(System.currentTimeMillis() + minimumTimeLeft)
                .setOngoing(true);

        return notificationBuilder.build();
    }

    private NotificationCompat.Builder getNotificationBuilder(String channelId){
        PendingIntent contentIntent = getIntentStartingApp();

        Bitmap iconTimer = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.timer);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, channelId)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_stat_action_schedule) // Needed to not get generic Android notification
                .setLargeIcon(iconTimer)
                .setFullScreenIntent(contentIntent, false)
                .setContentIntent(contentIntent);

        return notificationBuilder;
    }

    public Notification createTimerFinishedNotification(String timerName){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        String ringtone = prefs.getString("ringtone", "");
        Uri soundUri = ringtone.equals("") ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) : Uri.parse(ringtone);

        NotificationCompat.Builder notificationBuilder = getNotificationBuilder(NOTIFICATION_CHANNEL_ID_FINISHED)
                .setContentText(mContext.getText(R.string.service_finished_label) + ": " + timerName)
                .setSound(soundUri, SOUND_STREAM);

        Notification notificationSound = notificationBuilder.build();

        // TODO what about this insistent preference, can it be done another way?
        if (prefs.getBoolean("insistent_alarm", true)) {
            notificationSound.flags |= Notification.FLAG_INSISTENT;
        }

        // If device is set to silent mode by user, overwrite it
//        AudioManager audio = (AudioManager) mContext.getSystemService(
//                Context.AUDIO_SERVICE);
//        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
//        int currentRingerMode = audio.getRingerMode();
//        int max = audio.getStreamMaxVolume(SOUND_STREAM);
//
//        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//        int volume = (int) Math.round(prefs.getInt("alarm_volume", 5) * 0.1 * max);
//        audio.setStreamVolume(SOUND_STREAM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

//        mNotificationManager.notify(NOTIFICATION_ID_SOUND, notificationSound);

        // reset volume and ringer mode
//        audio.setRingerMode(currentRingerMode);
//        audio.setStreamVolume(AudioManager.STREAM_RING, currentVolume, 0);

//        startMainActivity();

        return notificationSound;
    }

    private PendingIntent getIntentStartingApp() {
        Intent resultIntent = new Intent(mContext, HomeActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        return resultPendingIntent;
    }

    public Notification getNotificationForService(Collection<TimerService.CountDown> timers){
        return createRunningTimersNotification(timers);
    }

    public void startNotificationForStartedTimer(String timerName){
        NotificationCompat.Builder builder = getNotificationBuilder(NOTIFICATION_CHANNEL_ID_RUNNING).setContentText(mContext.getText(R.string.service_timer_label) + ": " +
                timerName + " " +
                mContext.getString(R.string.service_started_label));

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void startNotificationForCancelledTimer(String timerName){
        NotificationCompat.Builder builder = getNotificationBuilder(NOTIFICATION_CHANNEL_ID_RUNNING).setContentText(mContext.getText(R.string.service_timer_label) + ": " +
                timerName + " " +
                mContext.getString(R.string.service_stopped_label));

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void startNotificationForFinishedTimer(String timerName){
        mNotificationManager.notify(NOTIFICATION_ID_SOUND, createTimerFinishedNotification(timerName));
    }

    public void cancelSoundNotification(){
        mNotificationManager.cancel(NOTIFICATION_ID_SOUND);
    }

    public void cancelTextNotification(){
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}
package com.tomatodev.timerdroid.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.tomatodev.timerdroid.MyApplication;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.activities.MainActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimerService extends Service {

	private static final int SOUND_STREAM = AudioManager.STREAM_ALARM;
	private Map<Integer, CountDown> timers = new HashMap<Integer, CountDown>();
	private static Integer lastId = 0;
	private static final int NOTIFICATION_ID = 1;
	private static final int NOTIFICATION_ID_SOUND = 2;
	private static final int NOTIFICATION_ID_FOREGROUND = 3;

	private static final int NOTIFICATION_TYPE_STARTED = 0;
	private static final int NOTIFICATION_TYPE_STOPPED = 1;
	private static final int NOTIFICATION_TYPE_CANCELLED = 2;

	private NotificationManager mNotificationManager;
	private PowerManager.WakeLock wl;

	public class LocalBinder extends Binder {
		public TimerService getService() {
			return TimerService.this;
		}

	}

	private final LocalBinder binder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		setupNotification();
		return binder;
	}

	private void setupNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
	}

	private void createNotification(String tickerText, int type) {
		Context context = getApplicationContext();
		StringBuffer text = new StringBuffer("");
		text.append(getString(R.string.service_running_timers) + ": ");
		boolean timerRunning = false;
        int numberOfRunningTimers = 0;
        long minimumTimeleft = Long.MAX_VALUE;

		for (CountDown countdown : timers.values()) {
			if (countdown.isStarted()) {
				text.append(countdown.getName() + ", ");
				timerRunning = true;
                numberOfRunningTimers++;
                if (countdown.getTimeLeft() < minimumTimeleft) {
                    minimumTimeleft = countdown.getTimeLeft();
                }
			}
		}
		if (!timerRunning) {
			text = new StringBuffer(getString(R.string.service_no_running_timers));
		} else {
			// delete the unnecessary ',' at the end
			text.delete(text.length() - 2, text.length() - 1);
		}
		CharSequence contentTitle = getString(R.string.app_name);
        PendingIntent contentIntent = getIntentStartingApp();

        Bitmap iconTimer = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.timer);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_action_schedule) // Needed to not get generic Android notification
                .setLargeIcon(iconTimer)
                .setFullScreenIntent(contentIntent, false)
                .setContentIntent(contentIntent)
                .setNumber(numberOfRunningTimers)
                .setWhen(System.currentTimeMillis() + minimumTimeleft)
                .setOngoing(true);

        Notification notification = notificationBuilder.build();//getNotification();// build() only working with API level >= 16

        if (type == NOTIFICATION_TYPE_STOPPED) {
			startSoundNotification(tickerText);
		}

		if (type == NOTIFICATION_TYPE_STARTED || timerRunning) {
			startForeground(NOTIFICATION_ID_FOREGROUND, notification);
		} else {
			stopForeground(true);
		}

	}

    private void startSoundNotification(String text) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Bitmap iconTimer = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.timer);

        String ringtone = prefs.getString("ringtone", "");
        Uri soundUri = ringtone.equals("") ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) : Uri.parse(ringtone);

        PendingIntent contentIntent = getIntentStartingApp();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_action_schedule) // Needed to not get generic Android notification (at least for service startForeground
                .setLargeIcon(iconTimer)
                .setLights(Color.RED, 200, 600)
                .setFullScreenIntent(contentIntent, false)
                .setContentIntent(contentIntent)
                .setSound(soundUri, SOUND_STREAM);

        // Currently not needed since sound stops when pulling down the notification twice
//        NotificationCompat.Action action = new NotificationCompat.Action.Builder(android.R.drawable.ic_delete, "Delete", contentIntent).build();
//        notificationBuilder.addAction(action);

        Notification notificationSound = notificationBuilder.build();

        // TODO what about this insistent preference, can it be done another way?
        if (prefs.getBoolean("insistent_alarm", true)) {
            notificationSound.flags |= Notification.FLAG_INSISTENT;
        }

        // If device is set to silent mode by user, overwrite it
        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
        int currentRingerMode = audio.getRingerMode();
        int max = audio.getStreamMaxVolume(SOUND_STREAM);

        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        int volume = (int) Math.round(prefs.getInt("alarm_volume", 5) * 0.1 * max);
        audio.setStreamVolume(SOUND_STREAM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        mNotificationManager.notify(NOTIFICATION_ID_SOUND, notificationSound);

        // reset volume and ringer mode
        audio.setRingerMode(currentRingerMode);
        audio.setStreamVolume(AudioManager.STREAM_RING, currentVolume, 0);

//        startMainActivity();
    }

    private PendingIntent getIntentStartingApp() {
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        return resultPendingIntent;
    }

    private void startMainActivity() {
        // Show application
        MyApplication.showRunningTimers = true;
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplication().startActivity(i);
    }

    public void stopSound() {
		mNotificationManager.cancel(NOTIFICATION_ID_SOUND);
	}

	public long getTimeLeft(Integer id) {
		return timers.get(id).getTimeLeft();
	}

	public Integer startTimer(String name, long timeLeft) {
		return startTimer(name, timeLeft, null);
	}

	public Integer startTimer(String name, long timeLeft, List<TimerDescription> queue) {

		if (name == null || name.equalsIgnoreCase("")) {
			name = getApplicationContext().getResources().getString(R.string.cr_timer_custom);
		}
		
		if (timers.isEmpty()) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			if (prefs.getBoolean("screen_unlocked", false)) {
				wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
			} else {
				wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
			}
			wl.acquire();

		}

		synchronized (lastId) {
			CountDown counter = new CountDown(timeLeft, 1000, name, queue);
			counter.start();
			counter.setStarted(true);
			timers.put(lastId, counter);
			lastId++;

			createNotification(getString(R.string.service_timer_label) + " " + name + " "
					+ getString(R.string.service_started_label), NOTIFICATION_TYPE_STARTED);
		}

		return lastId - 1;

	}	

	public void deleteTimer(Integer id) {
		if (timers.get(id).isStarted()) {
			stopTimer(id);
		}
		timers.remove(id);

		if (timers.isEmpty()) {
			wl.release();
			mNotificationManager.cancel(NOTIFICATION_ID);
			this.stopSelf();
		}
	}
	
	private void stopTimer(Integer id) {
		timers.get(id).setStarted(false);
		timers.get(id).cancel();
		createNotification(getString(R.string.service_timer_label) + " " + timers.get(id).getName()
				+ " " + getString(R.string.service_stopped_label), NOTIFICATION_TYPE_CANCELLED);

	}

	public void pauseTimer(Integer id) {
		// TODO
	}

	public boolean isStarted(Integer id) {
		return timers.get(id).isStarted();
	}

	public void setTimeLeft(Integer id, long timeLeft) {
		timers.get(id).setTimeLeft(timeLeft);
	}

	public void resumeTimer(Integer id) {
		// TODO
	}

	public int getNumberOfTimers() {
		return timers.size();
	}

	public Map<Integer, CountDown> getTimers() {
		return timers;
	}

	public class CountDown extends AbstractCountDown implements Comparable<CountDown> {

		public CountDown(long millisInFuture, long countDownInterval, String name) {
			super(millisInFuture, countDownInterval, name);
		}
		
		

		public CountDown(long millisInFuture, long countDownInterval, String name, List<TimerDescription> queue) {
			super(millisInFuture, countDownInterval, name, queue);
		}



		public void onFinish() {
			started = false;
			
			if (queue != null && queue.size() > 0){
				String nameNewTimer = queue.get(0).getName();
				long timeNewTimer = queue.get(0).getTime();
				queue.remove(0);
				startTimer(nameNewTimer, timeNewTimer, queue);
			}
			
			createNotification(getString(R.string.service_timer_label) + " " + name + " "
					+ getString(R.string.service_finished_label), NOTIFICATION_TYPE_STOPPED);
			
		}

		public void onTick(long millisUntilFinished) {
			timeLeft = millisUntilFinished;
			// Log.v(Constants.LOG_TAG, "TimerService: time left: " + timeLeft);
		}

		@Override
		public int compareTo(CountDown another) {
			return (int) (this.timeLeft - another.timeLeft);
		}
	}

}

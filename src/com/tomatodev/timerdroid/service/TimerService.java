package com.tomatodev.timerdroid.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.tomatodev.timerdroid.MyApplication;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.activities.MainActivity;

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
		int icon = R.drawable.notification;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		StringBuffer text = new StringBuffer("");
		text.append(getString(R.string.service_running_timers) + ": ");
		boolean timerRunning = false;
		for (CountDown countdown : timers.values()) {
			if (countdown.isStarted()) {
				text.append(countdown.getName() + ", ");
				timerRunning = true;
			}
		}
		if (!timerRunning) {
			text = new StringBuffer(getString(R.string.service_no_running_timers));
		} else {
			// delete the unncessary ',' at the end
			text.delete(text.length() - 2, text.length() - 1);
		}
		CharSequence contentTitle = getString(R.string.app_name);
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, text, contentIntent);

		if (type == NOTIFICATION_TYPE_STOPPED) {

			startSoundNotification(tickerText);
		}

		if (type == NOTIFICATION_TYPE_STARTED || timerRunning) {
			startForeground(NOTIFICATION_ID_FOREGROUND, notification);
		} else {
			stopForeground(true);
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		}

	}

	private void startSoundNotification(String text) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// set ringtone
		Notification notificationSound = new Notification();

		String ringtone = prefs.getString("ringtone", "");
		if (ringtone.equals("")) {
			Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			notificationSound.sound = alert;
			notificationSound.audioStreamType = SOUND_STREAM;
		} else {
			notificationSound.sound = Uri.parse(ringtone);
			notificationSound.audioStreamType = SOUND_STREAM;
		}

		// If device is set to silent mode by user, overwrite it
		AudioManager audio = (AudioManager) getApplicationContext().getSystemService(
				Context.AUDIO_SERVICE);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
		int currentRingerMode = audio.getRingerMode();
		int max = audio.getStreamMaxVolume(SOUND_STREAM);
		
//		audio.setStreamSolo(SOUND_STREAM, false);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		int volume = (int) Math.round(prefs.getInt("alarm_volume", 5) * 0.1 * max);
		audio.setStreamVolume(SOUND_STREAM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

		if (prefs.getBoolean("insistent_alarm", true)) {
			notificationSound.flags |= Notification.FLAG_INSISTENT;
		}
		notificationSound.icon = R.drawable.notification;
		
		// TODO change text and icon if needed at all (2 icons in status bar...)
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);
		contentView.setImageViewResource(R.id.notification_image, R.drawable.timer);
		contentView.setTextViewText(R.id.notification_text, text);
		notificationSound.contentView = contentView;
//		contentView.setOnClickPendingIntent(R.id.notification_button, getDialogPendingIntent("Tapped the 'dialog' button in the notification."));

		mNotificationManager.notify(NOTIFICATION_ID_SOUND, notificationSound);

		// reset volume and ringer mode
//		audio.setStreamSolo(SOUND_STREAM, false);
		audio.setRingerMode(currentRingerMode);
		audio.setStreamVolume(AudioManager.STREAM_RING, currentVolume, 0);

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

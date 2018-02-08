package com.tomatodev.timerdroid.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.tomatodev.timerdroid.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimerService extends Service {

	private Map<Integer, CountDown> timers = new HashMap<>();
	private static Integer lastId = 0;

	private ArrayList<ITimerUpdatedHandler> listeners = new ArrayList<>();

	private CustomNotificationManager mNotificationManager;
	private PowerManager.WakeLock wl;

	public class LocalBinder extends Binder {
		public TimerService getService() {
			return TimerService.this;
		}
	}

	private final LocalBinder binder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		this.mNotificationManager = new CustomNotificationManager(this);
		return binder;
	}

    public void registerListener(ITimerUpdatedHandler handler){
	    if (!this.listeners.contains(handler)) {
            this.listeners.add(handler);
        }
    }

    public void deregisterListener(ITimerUpdatedHandler handler){
        this.listeners.remove(handler);
    }

    public void stopSound() {
		mNotificationManager.cancelSoundNotification();
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

			this.notifyListeners();

//            mNotificationManager.startNotificationForStartedTimer(name);
			startForeground(100, mNotificationManager.getNotificationForService(timers.values()));
		}

		return lastId - 1;
	}

	private void notifyListeners(){
        for (ITimerUpdatedHandler listener : this.listeners){
            listener.onTimersChanged();
        }
    }

	public void deleteTimer(Integer id) {
		if (timers.get(id).isStarted()) {
			stopTimer(id);
		}
		timers.remove(id);

		if (timers.isEmpty()) {
			wl.release();
			mNotificationManager.cancelTextNotification();
            if (!isTimerRunning()){
                stopForeground(true);
            }
		}
	}
	
	private void stopTimer(Integer id) {
		timers.get(id).setStarted(false);
		timers.get(id).cancel();
		mNotificationManager.startNotificationForCancelledTimer(timers.get(id).getName());
		if (!isTimerRunning()){
		    stopForeground(true);
        }
	}

	private boolean isTimerRunning(){
        for (TimerService.CountDown countdown : timers.values()) {
            if (countdown.isStarted()) {
                return true;
            }
        }
        return false;
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

			mNotificationManager.startNotificationForFinishedTimer(name);
            if (!isTimerRunning()){
                stopForeground(true);
            }
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

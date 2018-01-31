package com.tomatodev.timerdroid.service;

import android.os.CountDownTimer;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCountDown extends CountDownTimer {

	protected String name;
	protected boolean started;
	protected long timeLeft = 0;
	
	protected List<TimerDescription> queue = new ArrayList<TimerDescription>();

	public AbstractCountDown(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		this.name = "";
		this.started = false;
		this.timeLeft = millisInFuture;
	}

	public AbstractCountDown(long millisInFuture, long countDownInterval, String name) {
		super(millisInFuture, countDownInterval);
		this.name = name;
		this.started = false;
		this.timeLeft = millisInFuture;
	}
	
	

	public AbstractCountDown(long millisInFuture, long countDownInterval, String name,
			List<TimerDescription> queue) {
		super(millisInFuture, countDownInterval);
		this.name = name;
		this.started = false;
		this.timeLeft = millisInFuture;
		this.queue = queue;
	}

	public long getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(long timeLeft) {
		this.timeLeft = timeLeft;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public String getName() {
		return name;
	}

}

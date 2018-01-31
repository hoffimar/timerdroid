package com.tomatodev.timerdroid.service;

public class TimerDescription {

	private String name = "";
	private long time = 0;
	
	public TimerDescription(String name, long time) {
		this.name = name;
		this.time = time;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		return name + " - " + time;
	}
	
	
	
}

package com.tomatodev.timerdroid;

import android.app.Application;

public class MyApplication extends Application {
	public static boolean mainVisible = false;
	private static int counter;
	
	public static boolean showRunningTimers = false;
	
	public static synchronized int getId() {
		return counter++;
	}
}

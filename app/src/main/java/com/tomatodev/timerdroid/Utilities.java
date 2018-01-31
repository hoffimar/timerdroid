package com.tomatodev.timerdroid;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class Utilities {

	public static String formatTime(long millis) {
		String output = "00:00:00";
		long seconds = Math.round(millis / 1000.0);
		long minutes = seconds / 60;
		long hours = minutes / 60;

		seconds = seconds % 60;
		minutes = minutes % 60;

		String secondsD = String.valueOf(seconds);
		String minutesD = String.valueOf(minutes);
		String hoursD = String.valueOf(hours);

		if (seconds < 10)
			secondsD = "0" + seconds;
		if (minutes < 10)
			minutesD = "0" + minutes;

        if (hours > 0) {
            output = hoursD + " : " + minutesD + " : " + secondsD;
        } else {
            output = minutesD + " : " + secondsD;
        }

		return output;
	}
	
	public static String formatTimeNoBlanks(long millis) {
		long seconds = Math.round(millis / 1000.0);
		long minutes = seconds / 60;
		long hours = minutes / 60;

		seconds = seconds % 60;
		minutes = minutes % 60;

		String secondsD = String.valueOf(seconds);
		String minutesD = String.valueOf(minutes);
		String hoursD = String.valueOf(hours);

		if (seconds < 10)
			secondsD = "0" + seconds;
		if (minutes < 10)
			minutesD = "0" + minutes;
		if (hours < 10)
			hoursD = "0" + hours;

		StringBuffer output = new StringBuffer();
		if (hours > 0)
			output.append(hoursD + "h");
		output.append(minutesD + "m" + secondsD + "s");
		return output.toString();
	}
	
	public static String formatTimeNoBlanksNoLeadingZeros(long millis) {
		long seconds = Math.round(millis / 1000.0);
		long minutes = seconds / 60;
		long hours = minutes / 60;

		seconds = seconds % 60;
		minutes = minutes % 60;

		String secondsD = String.valueOf(seconds);
		String minutesD = String.valueOf(minutes);
		String hoursD = String.valueOf(hours);

		if (seconds < 10)
			secondsD = "0" + seconds;
		if (minutes < 10 && hours > 0) 
			minutesD = "0" + minutes;

		StringBuffer output = new StringBuffer();
		if (hours > 0)
			output.append(hoursD + "h");
		output.append(minutesD);
		
		if (hours == 0 && seconds == 0)
			output.append("min");
		else
			output.append("m");
		
		if (seconds > 0) 
			output.append(secondsD + "s");
		return output.toString();
	}
	
	public static int[] lengthToTime(long millis) {
		int [] times = new int [3];
		long seconds = Math.round(millis / 1000.0);
		long minutes = seconds / 60L;
		long hours = minutes / 60L;

		times [0] = (int) (seconds % 60L);
		times [1] = (int) (minutes % 60L);
		times [2] = (int) (hours);

		return times;
	}
	
	public static long computeLength(int hour, int minute, int second) {
		long counter;
		long cHour = hour * 3600000L;
		long cMinute = minute * 60000L;
		long cSecond = second * 1000L;
		counter = cHour + cMinute + cSecond;
        return counter;
	}
	
	// decodes image and scales it to reduce memory consumption
	// see http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue
	public static Bitmap decodeFile(Context context, String imageUri){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
//	        Log.v(Constants.LOG_TAG, "Uri: " + imageUri);
	        InputStream is = context.getContentResolver().openInputStream(Uri.parse(imageUri));
	        BitmapFactory.decodeStream(is, null, o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=45;

	        //Find the correct scale value. It should be the power of 2.
	        int width_tmp=o.outWidth, height_tmp=o.outHeight;
	        int scale=1;
	        while(true){
	            if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
	                break;
	            width_tmp/=2;
	            height_tmp/=2;
	            scale*=2;
	        }

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(imageUri)), null, o2);
	    } catch (FileNotFoundException e) {}
	    
	    return null;
	}
	
}

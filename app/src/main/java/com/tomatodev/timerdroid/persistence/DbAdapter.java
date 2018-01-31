package com.tomatodev.timerdroid.persistence;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tomatodev.timerdroid.Constants;
import com.tomatodev.timerdroid.R;

public class DbAdapter {

	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 1;

	public static final String TIMER_TABLE_NAME = "timers";
	public static final String TIMER_KEY_ID = "_id";
	public static final String TIMER_KEY_NAME = "name";
	public static final String TIMER_KEY_TIME = "time";
	public static final String TIMER_KEY_FAVORITE = "favorite";
	public static final String TIMER_KEY_CATEGORY = "category";
	public static final String TIMER_KEY_USERCREATED = "usercreated";

	public static final String CATEGORIES_TABLE_NAME = "categories";
	public static final String CATEGORIES_KEY_ID = "_id";
	public static final String CATEGORIES_KEY_NAME = "name";
	public static final String CATEGORIES_KEY_PARENT_CATEGORY = "parent";
	public static final String CATEGORIES_KEY_IMAGE = "image";
	
	private static Context mCtx;

	/**
	 * Helper to open or create the database
	 * 
	 */
	static class DbOpenHelper extends SQLiteOpenHelper {

		private static final String TIMER_TABLE_CREATE = "CREATE TABLE " + TIMER_TABLE_NAME + " ("
				+ TIMER_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ TIMER_KEY_NAME + " TEXT, "
				+ TIMER_KEY_TIME + " TEXT, " 
				+ TIMER_KEY_CATEGORY + " INTEGER, " 
				+ TIMER_KEY_FAVORITE + " INTEGER, "
				+ TIMER_KEY_USERCREATED + " INTEGER, "
				+ "FOREIGN KEY(" + TIMER_KEY_CATEGORY + ") REFERENCES " + CATEGORIES_TABLE_NAME + "(" + CATEGORIES_KEY_ID + ") );";
		
		private static final String CATEGORIES_TABLE_CREATE = "CREATE TABLE " + CATEGORIES_TABLE_NAME + " ("
		+ CATEGORIES_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
		+ CATEGORIES_KEY_NAME + " TEXT, " 
		+ CATEGORIES_KEY_PARENT_CATEGORY + " INTEGER, " 
		+ CATEGORIES_KEY_IMAGE + " TEXT, "
		+ "FOREIGN KEY(" + CATEGORIES_KEY_PARENT_CATEGORY + ") REFERENCES " + CATEGORIES_TABLE_NAME + "(" + CATEGORIES_KEY_ID + ") );";

		private static final String WIDGET_TABLE_CREATE = "CREATE TABLE " + WidgetProvider.WidgetTable.TABLE_NAME + " (" + WidgetProvider.WidgetTable.WIDGET_KEY_ID
		+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + WidgetProvider.WidgetTable.WIDGET_KEY_NAME + " TEXT, " + WidgetProvider.WidgetTable.WIDGET_KEY_WIDGET_ID
		+ " INTEGER, " + WidgetProvider.WidgetTable.WIDGET_KEY_TIME + " TEXT );";
		
		public DbOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mCtx = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CATEGORIES_TABLE_CREATE);
			db.execSQL(TIMER_TABLE_CREATE);
			db.execSQL(WIDGET_TABLE_CREATE);
			importFromCsv(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			Log.w(Constants.LOG_TAG, "Upgrading database from version " + oldVersion + " to "
//					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TIMER_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + WidgetProvider.WidgetTable.TABLE_NAME);
			onCreate(db);

		}
		
		/**
		 * Loads categories and timers from a csv file
		 * @param db
		 */
		private void importFromCsv(SQLiteDatabase db){
			Locale locale = Locale.getDefault();
//			Log.v(Constants.LOG_TAG, "Locale: " + locale.toString());
			
			InputStream isTimers;
			InputStream isCategories;
			
			if (locale.getLanguage().equalsIgnoreCase("de")){
				isTimers = mCtx.getResources().openRawResource(R.raw.timers_de);
				isCategories = mCtx.getResources().openRawResource(R.raw.categories_de);
			} else {
				if (locale.getLanguage().equalsIgnoreCase("es")){
					isTimers = mCtx.getResources().openRawResource(R.raw.timers_es);
					isCategories = mCtx.getResources().openRawResource(R.raw.categories_es);
				} else {
					isTimers = mCtx.getResources().openRawResource(R.raw.timers);
					isCategories = mCtx.getResources().openRawResource(R.raw.categories);
				}
			}
			
			// insert categories into db
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(isCategories), "utf8"),8192);

				String strLine = null;

				while ((strLine = br.readLine()) != null) {
					
					String[] tokens = strLine.split(";");
					if (tokens.length == 2){
					db.execSQL("INSERT INTO " + CATEGORIES_TABLE_NAME + "(" 
							+ CATEGORIES_KEY_NAME + ", " 
							+ CATEGORIES_KEY_PARENT_CATEGORY + ")" 
							+ " VALUES('" + tokens[0] + "', '" + tokens[1] + "')");
					} else if (tokens.length == 3){
						db.execSQL("INSERT INTO " + CATEGORIES_TABLE_NAME
								+ "(" + CATEGORIES_KEY_NAME + ", " 
								+ CATEGORIES_KEY_PARENT_CATEGORY + ", " 
								+ CATEGORIES_KEY_IMAGE + ")" 
								+ " VALUES('" + tokens[0] + "', '" + tokens[1] + "', " + "'" + tokens[2] + "')");
					}
					
				}
				br.close();
			} catch  (Exception e) {
				Log.e(Constants.LOG_TAG, e.getMessage());
			}
			
			
			
			
			// Insert timers into db
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(isTimers), "utf8"),8192);
				String strLine = null;

				while ((strLine = br.readLine()) != null) {
					
					String[] tokens = strLine.split(";");
					
					if (tokens.length == 4){
						db.execSQL("INSERT INTO " 
								+ TIMER_TABLE_NAME + "(" 
								+ TIMER_KEY_NAME + ", " 
								+ TIMER_KEY_TIME + ", " 
								+ TIMER_KEY_CATEGORY + ", " 
								+ TIMER_KEY_FAVORITE + ", " 
								+ TIMER_KEY_USERCREATED 
								+ ") VALUES('" + tokens[0] + "', " + tokens[1] + ", " + tokens[2] + ", " + tokens[3] + ", 0)");
					}
					
				}
				br.close();
			} catch  (Exception e) {
				Log.e(Constants.LOG_TAG, e.getMessage());
			}

		}

	}




}

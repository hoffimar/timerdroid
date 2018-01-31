/**
 * 
 */
package com.tomatodev.timerdroid.persistence;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.tomatodev.timerdroid.persistence.DbAdapter.DbOpenHelper;

public class TimersProvider extends ContentProvider {
	
	/**
	     * Definition of the contract for the timer table of our provider.
	     */
	    public static final class TimerTable implements BaseColumns {
	
	        // This class cannot be instantiated
	        private TimerTable() {}
	
	        /**
	         * The table name offered by this provider
	         */
	        public static final String TABLE_NAME = "timers";
	
	        /**
	         * The content:// style URL for this table
	         */
	        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY_TIMERS + "/timers");
	
	        /**
	         * The content URI base for a single row of data. Callers must
	         * append a numeric row id to this Uri to retrieve a row
	         */
	        public static final Uri CONTENT_ID_URI_BASE
	                = Uri.parse("content://" + AUTHORITY_TIMERS + "/timers/");
	
	        /**
	         * The MIME type of {@link #CONTENT_URI}.
	         */
	        public static final String CONTENT_TYPE
	                = "vnd.android.cursor.dir/vnd.com.tomatodev.persistence.timerdroid";
	
	        /**
	         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
	         */
	        public static final String CONTENT_ITEM_TYPE
	                = "vnd.android.cursor.item/vnd.com.tomatodev.persistence.timerdroid";
	        /**
	         * The default sort order for this table
	         */
	        public static final String DEFAULT_SORT_ORDER = "name COLLATE LOCALIZED ASC";
	
	        /**
	         * Column name for the single column holding our data.
	         * <P>Type: TEXT</P>
	         */
	//        public static final String COLUMN_NAME_DATA = "data";
	        
	        public static final String TIMER_KEY_ID = "_id";
	    	public static final String TIMER_KEY_NAME = "name";
	    	public static final String TIMER_KEY_TIME = "time";
	    	public static final String TIMER_KEY_FAVORITE = "favorite";
	    	public static final String TIMER_KEY_CATEGORY = "category";
	    	public static final String TIMER_KEY_USERCREATED = "usercreated";
	    }

	// A projection map used to select columns from the database
    private final HashMap<String, String> mNotesProjectionMap;
    // Uri matcher to decode incoming URIs.
    private final UriMatcher mUriMatcher;

    // The incoming URI matches the main table URI pattern
    private static final int TIMER = 1;
    // The incoming URI matches the main table row ID URI pattern
    private static final int TIMER_ID = 2;

	
	private DbOpenHelper mOpenHelper;
	/**
	 * The authority we use to get to our sample provider.
	 */
	public static final String AUTHORITY_TIMERS = "com.tomatodev.timerdroid.persistence.TimersProvider";

	public TimersProvider() {
		// Create and initialize URI matcher.
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(TimersProvider.AUTHORITY_TIMERS, TimersProvider.TimerTable.TABLE_NAME, TIMER);
        mUriMatcher.addURI(TimersProvider.AUTHORITY_TIMERS, TimersProvider.TimerTable.TABLE_NAME + "/#", TIMER_ID);

        // Create and initialize projection map for all columns.  This is
        // simply an identity mapping.
        mNotesProjectionMap = new HashMap<String, String>();
        mNotesProjectionMap.put(TimersProvider.TimerTable._ID, TimersProvider.TimerTable._ID);
        mNotesProjectionMap.put(TimersProvider.TimerTable.TIMER_KEY_NAME, TimersProvider.TimerTable.TIMER_KEY_NAME);
        mNotesProjectionMap.put(TimersProvider.TimerTable.TIMER_KEY_TIME, TimersProvider.TimerTable.TIMER_KEY_TIME);
        mNotesProjectionMap.put(TimersProvider.TimerTable.TIMER_KEY_CATEGORY, TimersProvider.TimerTable.TIMER_KEY_CATEGORY);
        mNotesProjectionMap.put(TimersProvider.TimerTable.TIMER_KEY_FAVORITE, TimersProvider.TimerTable.TIMER_KEY_FAVORITE);

	}
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DbOpenHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalWhere;

        int count;

        switch (mUriMatcher.match(uri)) {
            case TIMER:
                // If URI is main table, delete uses incoming where clause and args.
                count = db.delete(TimerTable.TABLE_NAME, selection, selectionArgs);
                break;

                // If the incoming URI matches a single note ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular note ID.
            case TIMER_ID:
                // If URI is for a particular row ID, delete is based on incoming
                // data but modified to restrict to the given ID.
                finalWhere = DatabaseUtils.concatenateWhere(
                		TimerTable._ID + " = " + ContentUris.parseId(uri), selection);
                count = db.delete(TimerTable.TABLE_NAME, finalWhere, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case TIMER:
			return TimersProvider.TimerTable.CONTENT_TYPE;
		case TIMER_ID:
			return TimersProvider.TimerTable.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (mUriMatcher.match(uri) != TIMER) {
            // Can only insert into to main URI.
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insert(TimerTable.TABLE_NAME, null, values);

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            Uri timerUri = ContentUris.withAppendedId(TimerTable.CONTENT_ID_URI_BASE, rowId);
            getContext().getContentResolver().notifyChange(timerUri, null);
            return timerUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TimersProvider.TimerTable.TABLE_NAME);

        switch (mUriMatcher.match(uri)) {
            case TIMER:
                // If the incoming URI is for main table.
                qb.setProjectionMap(mNotesProjectionMap);
                break;

            case TIMER_ID:
                // The incoming URI is for a single row.
                qb.setProjectionMap(mNotesProjectionMap);
                qb.appendWhere(TimersProvider.TimerTable._ID + "=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[] { uri.getLastPathSegment() });
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }


        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = TimersProvider.TimerTable.DEFAULT_SORT_ORDER;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor c = qb.query(db, projection, selection, selectionArgs,
                null /* no group */, null /* no filter */, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;

        switch (mUriMatcher.match(uri)) {
            case TIMER:
            	count = 0;
                break;

            case TIMER_ID:
                // If URI is for a particular row ID, update is based on incoming
                // data but modified to restrict to the given ID.
                finalWhere = DatabaseUtils.concatenateWhere(
                        TimerTable._ID + " = " + ContentUris.parseId(uri), selection);
                count = db.update(TimerTable.TABLE_NAME, values, finalWhere, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
	}
	
}
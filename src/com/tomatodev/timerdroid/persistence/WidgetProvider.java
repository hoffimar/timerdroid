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

public class WidgetProvider extends ContentProvider {
	
	/**
	     * Definition of the contract for the widget table of our provider.
	     */
	    public static final class WidgetTable implements BaseColumns {
	
	        // This class cannot be instantiated
	        private WidgetTable() {}
	
	        /**
	         * The table name offered by this provider
	         */
	        public static final String TABLE_NAME = "widget";
	
	        /**
	         * The content:// style URL for this table
	         */
	        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY_TIMERS + "/widget");
	
	        /**
	         * The content URI base for a single row of data. Callers must
	         * append a numeric row id to this Uri to retrieve a row
	         */
	        public static final Uri CONTENT_ID_URI_BASE
	                = Uri.parse("content://" + AUTHORITY_TIMERS + "/widget/");
	
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
	
	    	public static final String WIDGET_KEY_ID = "_id";
	    	public static final String WIDGET_KEY_NAME = "name";
	    	public static final String WIDGET_KEY_TIME = "time";
	    	public static final String WIDGET_KEY_WIDGET_ID = "widgetid";
	    }

	// A projection map used to select columns from the database
    private final HashMap<String, String> mNotesProjectionMap;
    // Uri matcher to decode incoming URIs.
    private final UriMatcher mUriMatcher;

    // The incoming URI matches the main table URI pattern
    private static final int WIDGET = 1;
    // The incoming URI matches the main table row ID URI pattern
    private static final int WIDGET_ID = 2;

	
	private DbOpenHelper mOpenHelper;
	/**
	 * The authority we use to get to our sample provider.
	 */
	public static final String AUTHORITY_TIMERS = "com.tomatodev.timerdroid.persistence.WidgetProvider";

	public WidgetProvider() {
		// Create and initialize URI matcher.
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(WidgetProvider.AUTHORITY_TIMERS, WidgetProvider.WidgetTable.TABLE_NAME, WIDGET);
        mUriMatcher.addURI(WidgetProvider.AUTHORITY_TIMERS, WidgetProvider.WidgetTable.TABLE_NAME + "/#", WIDGET_ID);

        // Create and initialize projection map for all columns.  This is
        // simply an identity mapping.
        mNotesProjectionMap = new HashMap<String, String>();
        mNotesProjectionMap.put(WidgetProvider.WidgetTable._ID, WidgetProvider.WidgetTable._ID);
        mNotesProjectionMap.put(WidgetProvider.WidgetTable.WIDGET_KEY_NAME, WidgetProvider.WidgetTable.WIDGET_KEY_NAME);
        mNotesProjectionMap.put(WidgetProvider.WidgetTable.WIDGET_KEY_TIME, WidgetProvider.WidgetTable.WIDGET_KEY_TIME);
        mNotesProjectionMap.put(WidgetProvider.WidgetTable.WIDGET_KEY_WIDGET_ID, WidgetProvider.WidgetTable.WIDGET_KEY_WIDGET_ID);

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
            case WIDGET:
                // If URI is main table, delete uses incoming where clause and args.
                count = db.delete(WidgetTable.TABLE_NAME, selection, selectionArgs);
                break;

                // If the incoming URI matches a single note ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular note ID.
            case WIDGET_ID:
                // If URI is for a particular row ID, delete is based on incoming
                // data but modified to restrict to the given ID.
                finalWhere = DatabaseUtils.concatenateWhere(
                		WidgetTable._ID + " = " + ContentUris.parseId(uri), selection);
                count = db.delete(WidgetTable.TABLE_NAME, finalWhere, selectionArgs);
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
		case WIDGET:
			return WidgetProvider.WidgetTable.CONTENT_TYPE;
		case WIDGET_ID:
			return WidgetProvider.WidgetTable.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (mUriMatcher.match(uri) != WIDGET) {
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

        long rowId = db.insert(WidgetTable.TABLE_NAME, null, values);

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            Uri widgetUri = ContentUris.withAppendedId(WidgetTable.CONTENT_ID_URI_BASE, rowId);
            getContext().getContentResolver().notifyChange(widgetUri, null);
            return widgetUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(WidgetProvider.WidgetTable.TABLE_NAME);

        switch (mUriMatcher.match(uri)) {
            case WIDGET:
                // If the incoming URI is for main table.
                qb.setProjectionMap(mNotesProjectionMap);
                break;

            case WIDGET_ID:
                // The incoming URI is for a single row.
                qb.setProjectionMap(mNotesProjectionMap);
                qb.appendWhere(WidgetProvider.WidgetTable._ID + "=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[] { uri.getLastPathSegment() });
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }


        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = WidgetProvider.WidgetTable.DEFAULT_SORT_ORDER;
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
            case WIDGET:
            	count = 0;
                break;

            case WIDGET_ID:
                // If URI is for a particular row ID, update is based on incoming
                // data but modified to restrict to the given ID.
                finalWhere = DatabaseUtils.concatenateWhere(
                        WidgetTable._ID + " = " + ContentUris.parseId(uri), selection);
                count = db.update(WidgetTable.TABLE_NAME, values, finalWhere, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
	}
	
}
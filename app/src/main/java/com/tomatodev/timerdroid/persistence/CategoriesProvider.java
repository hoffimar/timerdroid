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

public class CategoriesProvider extends ContentProvider {
	
	/**
	     * Definition of the contract for the categories table of our provider.
	     */
	    public static final class CategoriesTable implements BaseColumns {
	
	        // This class cannot be instantiated
	        private CategoriesTable() {}
	
	        /**
	         * The table name offered by this provider
	         */
	        public static final String TABLE_NAME = "categories";
	
	        /**
	         * The content:// style URL for this table
	         */
	        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY_CATEGORIES + "/categories");
	
	        /**
	         * The content URI base for a single row of data. Callers must
	         * append a numeric row id to this Uri to retrieve a row
	         */
	        public static final Uri CONTENT_ID_URI_BASE
	                = Uri.parse("content://" + AUTHORITY_CATEGORIES + "/categories/");
	
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
	        
	        public static final String CATEGORIES_TABLE_NAME = "categories";
	    	public static final String CATEGORIES_KEY_ID = "_id";
	    	public static final String CATEGORIES_KEY_NAME = "name";
	    	public static final String CATEGORIES_KEY_PARENT_CATEGORY = "parent";
	    	public static final String CATEGORIES_KEY_IMAGE = "image";
	    }

	// A projection map used to select columns from the database
    private final HashMap<String, String> mNotesProjectionMap;
    // Uri matcher to decode incoming URIs.
    private final UriMatcher mUriMatcher;

    // The incoming URI matches the main table URI pattern
    private static final int CATEGORY = 1;
    // The incoming URI matches the main table row ID URI pattern
    private static final int CATEGORY_ID = 2;

	
	private DbOpenHelper mOpenHelper;
	public static final String AUTHORITY_CATEGORIES = "com.tomatodev.timerdroid.persistence.CategoriesProvider";

	public CategoriesProvider() {
		// Create and initialize URI matcher.
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(CategoriesProvider.AUTHORITY_CATEGORIES, CategoriesProvider.CategoriesTable.TABLE_NAME, CATEGORY);
        mUriMatcher.addURI(CategoriesProvider.AUTHORITY_CATEGORIES, CategoriesProvider.CategoriesTable.TABLE_NAME + "/#", CATEGORY_ID);

        // Create and initialize projection map for all columns.  This is
        // simply an identity mapping.
        mNotesProjectionMap = new HashMap<String, String>();
        mNotesProjectionMap.put(CategoriesProvider.CategoriesTable._ID, CategoriesProvider.CategoriesTable._ID);
        mNotesProjectionMap.put(CategoriesProvider.CategoriesTable.CATEGORIES_KEY_NAME, CategoriesProvider.CategoriesTable.CATEGORIES_KEY_NAME);
        mNotesProjectionMap.put(CategoriesProvider.CategoriesTable.CATEGORIES_KEY_PARENT_CATEGORY, CategoriesProvider.CategoriesTable.CATEGORIES_KEY_PARENT_CATEGORY);
        mNotesProjectionMap.put(CategoriesProvider.CategoriesTable.CATEGORIES_KEY_IMAGE, CategoriesProvider.CategoriesTable.CATEGORIES_KEY_IMAGE);

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
            case CATEGORY:
                // If URI is main table, delete uses incoming where clause and args.
                count = db.delete(CategoriesTable.TABLE_NAME, selection, selectionArgs);
                break;

                // If the incoming URI matches a single note ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular note ID.
            case CATEGORY_ID:
                // If URI is for a particular row ID, delete is based on incoming
                // data but modified to restrict to the given ID.
                finalWhere = DatabaseUtils.concatenateWhere(
                		CategoriesTable._ID + " = " + ContentUris.parseId(uri), selection);
                count = db.delete(CategoriesTable.TABLE_NAME, finalWhere, selectionArgs);
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
		case CATEGORY:
			return CategoriesProvider.CategoriesTable.CONTENT_TYPE;
		case CATEGORY_ID:
			return CategoriesProvider.CategoriesTable.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (mUriMatcher.match(uri) != CATEGORY) {
            // Can only insert into to main URI.
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        if (!values.containsKey(CategoriesTable.CATEGORIES_KEY_NAME)) {
            values.put(CategoriesTable.CATEGORIES_KEY_NAME, "Timer");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insert(CategoriesTable.TABLE_NAME, null, values);

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            Uri categoriesUri = ContentUris.withAppendedId(CategoriesTable.CONTENT_ID_URI_BASE, rowId);
            getContext().getContentResolver().notifyChange(categoriesUri, null);
            return categoriesUri;
        }

        throw new SQLException("Failed to insert row into " + uri);

	}

	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CategoriesProvider.CategoriesTable.TABLE_NAME);

        switch (mUriMatcher.match(uri)) {
            case CATEGORY:
                // If the incoming URI is for main table.
                qb.setProjectionMap(mNotesProjectionMap);
                break;

            case CATEGORY_ID:
                // The incoming URI is for a single row.
                qb.setProjectionMap(mNotesProjectionMap);
                qb.appendWhere(CategoriesProvider.CategoriesTable._ID + "=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[] { uri.getLastPathSegment() });
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }


        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = CategoriesProvider.CategoriesTable.DEFAULT_SORT_ORDER;
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
            case CATEGORY:
                // If URI is main table, update uses incoming where clause and args.
//                count = db.update(MainTable.TABLE_NAME, values, where, whereArgs);
            	count = 0;
                break;

            case CATEGORY_ID:
                // If URI is for a particular row ID, update is based on incoming
                // data but modified to restrict to the given ID.
                finalWhere = DatabaseUtils.concatenateWhere(
                        CategoriesTable._ID + " = " + ContentUris.parseId(uri), selection);
                count = db.update(CategoriesTable.TABLE_NAME, values, finalWhere, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;

	}
	
}
package com.tomatodev.timerdroid.widget;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.tomatodev.timerdroid.MyApplication;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.Utilities;
import com.tomatodev.timerdroid.persistence.DbAdapter;
import com.tomatodev.timerdroid.persistence.TimersProvider;
import com.tomatodev.timerdroid.persistence.WidgetProvider.WidgetTable;

public class AppWidgetConfigure extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private int mAppWidgetId = 0;
	
	private TimerWidgetSmallCursorAdapter items;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		setResult(RESULT_CANCELED);
	}

	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}
	
	private void fillData() {
		
		getLoaderManager().initLoader(0, null, this);        
		items = new TimerWidgetSmallCursorAdapter(this, null);
		setListAdapter(items);
		
		this.setTitle(R.string.widget_choose_timer);

		setContentView(R.layout.widget_config_list);

		ListView lv = getListView();
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String timerName = items.getCursor().getString(items.getCursor().getColumnIndex(DbAdapter.TIMER_KEY_NAME));
				long timerTime = items.getCursor().getLong(items.getCursor().getColumnIndex(DbAdapter.TIMER_KEY_TIME));

				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

				Intent intent = new Intent(getApplicationContext(), WidgetProviderSmall.class);
				intent.setClass(getApplicationContext(), WidgetProviderSmall.class);
				intent.setAction(WidgetProviderSmall.ACTION_WIDGET_RECEIVER_START_TIMER);
				intent.putExtra("timerTime", timerTime);
				intent.putExtra("timerName", timerName);
				intent.putExtra("widgetId", mAppWidgetId);
				
				PendingIntent timerPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), MyApplication.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

				RemoteViews updateViews = new RemoteViews(getApplicationContext().getPackageName(),
						R.layout.widget_small);
				updateViews.setTextViewText(R.id.widget_listcounters_name, timerName);
				updateViews.setTextViewText(R.id.widget_listcounters_time, Utilities.formatTimeNoBlanksNoLeadingZeros(timerTime));
				updateViews.setOnClickPendingIntent(R.id.widget_listcounters_name, timerPendingIntent);
				updateViews.setOnClickPendingIntent(R.id.widget_listcounters_time, timerPendingIntent);
				updateViews.setOnClickPendingIntent(R.id.widget, timerPendingIntent);
				
				appWidgetManager.updateAppWidget(mAppWidgetId, updateViews);
				
				ContentValues values = new ContentValues();
				values.put(WidgetTable.WIDGET_KEY_NAME, timerName);
				values.put(WidgetTable.WIDGET_KEY_TIME, Long.toString(timerTime));
				values.put(WidgetTable.WIDGET_KEY_WIDGET_ID, mAppWidgetId);
				getApplicationContext().getContentResolver().insert(WidgetTable.CONTENT_URI, values);

				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();

			}
		});

	}
	
	static final String[] PROJECTION = new String[] {
        TimersProvider.TimerTable._ID,
        TimersProvider.TimerTable.TIMER_KEY_NAME,
        TimersProvider.TimerTable.TIMER_KEY_TIME,
        TimersProvider.TimerTable.TIMER_KEY_CATEGORY,
        TimersProvider.TimerTable.TIMER_KEY_FAVORITE,
    };


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cl = new CursorLoader(this, TimersProvider.TimerTable.CONTENT_URI,
                PROJECTION, null, null, null);
        return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		items.swapCursor(arg1);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		items.swapCursor(null);
		
	}

}

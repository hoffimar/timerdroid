package com.tomatodev.timerdroid.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.tomatodev.timerdroid.MyApplication;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.Utilities;
import com.tomatodev.timerdroid.persistence.DbAdapter;
import com.tomatodev.timerdroid.persistence.WidgetProvider.WidgetTable;
import com.tomatodev.timerdroid.service.TimerService;
import com.tomatodev.timerdroid.service.TimerService.LocalBinder;

public class WidgetProviderSmall extends AppWidgetProvider {

	public static String ACTION_WIDGET_RECEIVER_MAIN = "ActionReceiverWidgetMain";
	public static String ACTION_WIDGET_RECEIVER_CONFIGURE = "ActionReceiverWidgetConfigure";
	public static String ACTION_WIDGET_RECEIVER_START_TIMER = "ActionReceiverWidgetTimerStart";

	static DbAdapter mDbHelper;
	static Cursor mCursor;
	private static LocalBinder localBinder;
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			context.getContentResolver().delete(WidgetTable.CONTENT_URI,
					WidgetTable.WIDGET_KEY_WIDGET_ID + " = ?", new String[] { Integer.toString(appWidgetId) });
		}
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		connectService(context);
	}

	private void connectService(Context context) {
		Intent iTimerService = new Intent(context, TimerService.class);
		ServiceConnection serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				localBinder = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				localBinder = (LocalBinder) service;

			}
		};
		boolean success = context.getApplicationContext().bindService(iTimerService, serviceConnection,
				Service.BIND_AUTO_CREATE);
		if (!success) {
			// TODO: do something
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			Uri uri = WidgetTable.CONTENT_URI;
			Cursor cursor = context.getContentResolver().query(uri, null, WidgetTable.WIDGET_KEY_WIDGET_ID + " = ?", 
					new String[]{Integer.toString(appWidgetId)}, null);

			cursor.moveToFirst();
			if (cursor != null && cursor.getCount() > 0) {
				String name = cursor.getString(cursor.getColumnIndex(WidgetTable.WIDGET_KEY_NAME));
				long time = cursor.getLong(cursor.getColumnIndex(WidgetTable.WIDGET_KEY_TIME));
//				Log.v(Constants.LOG_TAG, "widget: " + name + "; " + time);

				// Update widgets
				Intent intent = new Intent(context, WidgetProviderSmall.class);
				intent.setClass(context, WidgetProviderSmall.class);
				intent.setAction(WidgetProviderSmall.ACTION_WIDGET_RECEIVER_START_TIMER);
				intent.putExtra("timerTime", time);
				intent.putExtra("timerName", name);
				intent.putExtra("widgetId", appWidgetId);

				PendingIntent timerPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),
						MyApplication.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

				RemoteViews updateViews = new RemoteViews(context.getApplicationContext().getPackageName(),
						R.layout.widget_small);
				updateViews.setTextViewText(R.id.widget_listcounters_name, name);
				updateViews.setTextViewText(R.id.widget_listcounters_time, Utilities.formatTimeNoBlanksNoLeadingZeros(time));
				updateViews.setOnClickPendingIntent(R.id.widget_listcounters_name, timerPendingIntent);
				updateViews.setOnClickPendingIntent(R.id.widget_listcounters_time, timerPendingIntent);
				updateViews.setOnClickPendingIntent(R.id.widget, timerPendingIntent);

				appWidgetManager.updateAppWidget(appWidgetId, updateViews);
				
				cursor.close();
			}
		}
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		String intentAction = intent.getAction();

		if (intentAction.equals(ACTION_WIDGET_RECEIVER_START_TIMER)) {

			final String timerName = intent.getExtras().getString("timerName");
			final long timerTime = intent.getExtras().getLong("timerTime");

			if (localBinder == null) {

				Intent iTimerService = new Intent(context, TimerService.class);
				ServiceConnection serviceConnection = new ServiceConnection() {

					@Override
					public void onServiceDisconnected(ComponentName name) {
						localBinder = null;
					}

					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						localBinder = (LocalBinder) service;
						localBinder.getService().startTimer(timerName, timerTime);

					}
				};
				boolean success = context.getApplicationContext().bindService(iTimerService, serviceConnection,
						Service.BIND_AUTO_CREATE);
				if (!success) {
					// TODO: do something
				}

			} else {

				localBinder.getService().startTimer(timerName, timerTime);

				// updateViews.setTextColor(R.id.widget_listcounters_name,
				// Color.RED);

				// ComponentName thisWidget = new ComponentName(context,
				// WidgetProviderSmall.class);
				// AppWidgetManager manager =
				// AppWidgetManager.getInstance(context);
				// manager.updateAppWidget(thisWidget, updateViews);

				// for debugging only
//				int widgetId = intent.getExtras().getInt("widgetId");
//				String msg = timerName + ", ID: " + widgetId + " started";
//				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			}
		}

	}
}

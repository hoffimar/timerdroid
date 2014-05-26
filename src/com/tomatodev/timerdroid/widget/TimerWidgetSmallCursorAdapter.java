package com.tomatodev.timerdroid.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.Utilities;
import com.tomatodev.timerdroid.persistence.TimersProvider;

public class TimerWidgetSmallCursorAdapter extends CursorAdapter {

	public TimerWidgetSmallCursorAdapter(Context context, Cursor c) {
		super(context, c);
		
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView time = (TextView) view.findViewById(R.id.listcounters_time);
		time.setText(Utilities.formatTime(cursor.getLong(cursor.getColumnIndex(TimersProvider.TimerTable.TIMER_KEY_TIME))));
		
		TextView name = (TextView) view.findViewById(R.id.listcounters_name);
		name.setText(cursor.getString(cursor.getColumnIndex(TimersProvider.TimerTable.TIMER_KEY_NAME)));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.widget_listcounters2, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}

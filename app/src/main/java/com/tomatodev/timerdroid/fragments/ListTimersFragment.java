package com.tomatodev.timerdroid.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tomatodev.timerdroid.MyApplication;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.TimerCursorAdapter;
import com.tomatodev.timerdroid.activities.HomeActivity;
import com.tomatodev.timerdroid.persistence.TimersProvider;
import com.tomatodev.timerdroid.service.TimerDescription;
import com.tomatodev.timerdroid.service.TimerService;
import com.tomatodev.timerdroid.service.TimerService.LocalBinder;

import java.util.ArrayList;
import java.util.List;

public class ListTimersFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private int categoryId;
	private TimerCursorAdapter items;
	
	private LocalBinder localBinder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setEmptyText(getActivity().getString(R.string.no_timers_in_this_category));
		fillData();
	}
	
	private void fillData() {
		Intent intent = new Intent(this.getActivity(), TimerService.class);
		ServiceConnection serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				localBinder = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				localBinder = (LocalBinder) service;
				items.setLocalBinder(localBinder);

			}
		};
		boolean success = getActivity().getApplicationContext().bindService(intent,
				serviceConnection, AppCompatActivity.BIND_AUTO_CREATE);
		
		categoryId = getActivity().getIntent().getIntExtra("category_id", 1);
		
        getLoaderManager().initLoader(0, null, this);        
		items = new TimerCursorAdapter(this.getActivity(), null, getFragmentManager(), localBinder);
		setListAdapter(items);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_bar_list_timers, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_run_category:
			List<TimerDescription> queue = new ArrayList<TimerDescription>();
			Cursor cursor = items.getCursor();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String timerName = cursor.getString(cursor.getColumnIndex(TimersProvider.TimerTable.TIMER_KEY_NAME));
				long timerTime = cursor.getLong(cursor.getColumnIndex(TimersProvider.TimerTable.TIMER_KEY_TIME));
				queue.add(new TimerDescription(timerName, timerTime));
				cursor.moveToNext();
			}
			try {
				TimerDescription firstTimer = queue.remove(0);
				localBinder.getService().startTimer(firstTimer.getName(),
						firstTimer.getTime(), queue);
				Intent intent = new Intent(getActivity().getApplicationContext(), HomeActivity.class);
				MyApplication.showRunningTimers = true;
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getActivity().getApplication().startActivity(intent);
			} catch (IndexOutOfBoundsException e) {
				// TODO
			} finally {
				cursor.close();
			}
			
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
		CursorLoader cl = new CursorLoader(getActivity(), TimersProvider.TimerTable.CONTENT_URI,
                PROJECTION, TimersProvider.TimerTable.TIMER_KEY_CATEGORY + "=" + categoryId, null, null);
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

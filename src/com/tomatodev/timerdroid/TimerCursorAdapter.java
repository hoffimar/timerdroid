package com.tomatodev.timerdroid;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.tomatodev.timerdroid.activities.TimerActivity;
import com.tomatodev.timerdroid.fragments.DeleteTimerDialogFragment;
import com.tomatodev.timerdroid.fragments.RepeatTimerFragment;
import com.tomatodev.timerdroid.fragments.RunningTimersFragment;
import com.tomatodev.timerdroid.persistence.DbAdapter;
import com.tomatodev.timerdroid.persistence.TimersProvider.TimerTable;
import com.tomatodev.timerdroid.service.TimerService.LocalBinder;

public class TimerCursorAdapter extends CursorAdapter {

	private LocalBinder localBinder;

	private FragmentManager fm;
	
	public TimerCursorAdapter(Context context, Cursor c, FragmentManager fm, LocalBinder binder) {
		super(context, c, true);
		this.fm = fm;
		this.localBinder = binder;
	}
	
	public void setLocalBinder(LocalBinder binder) {
		this.localBinder = binder;
	}

	public void bindView(final View view, final Context context, Cursor cursor) {
		
		final String timerName = cursor.getString(cursor.getColumnIndex(DbAdapter.TIMER_KEY_NAME));
		final int timerId = cursor.getInt(cursor.getColumnIndex(DbAdapter.TIMER_KEY_ID));
		final long timerTime = cursor.getLong(cursor.getColumnIndex(TimerTable.TIMER_KEY_TIME));
		
		TextView time = (TextView) view.findViewById(R.id.listcounters_time);
		time.setText(Utilities.formatTime(cursor.getLong(cursor.getColumnIndex(DbAdapter.TIMER_KEY_TIME))));
		
		
		
		TextView name = (TextView) view.findViewById(R.id.listcounters_name);
		name.setText(cursor.getString(cursor.getColumnIndex(DbAdapter.TIMER_KEY_NAME)));
		View.OnClickListener timerOnClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				
				    PopupMenu popup = new PopupMenu(context, v);
				    popup.inflate(R.menu.popup_timer);
				    popup.show();

				    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getItemId()) {
							case R.id.popup_timer_edit:
								Intent i = new Intent(context, TimerActivity.class);
								i.putExtra("timerId", timerId);
								context.startActivity(i);
								return true;
							case R.id.popup_timer_delete:
								DialogFragment newFragment = DeleteTimerDialogFragment.newInstance(context.getResources().getString(R.string.list_timers_delete_title), timerId);
							    newFragment.show(fm, "dialog");
								return true;
							case R.id.popup_timer_run_repeated:
								FragmentTransaction ft = fm.beginTransaction();
							    ft.addToBackStack(null);
							    RepeatTimerFragment newCategoryFragment = new RepeatTimerFragment(localBinder, timerName, timerTime);
							    newCategoryFragment.show(ft, "dialog");
								return true;
							default:
								break;
							}
							return false;
						}
					});
				
			}
		};
		name.setOnClickListener(timerOnClickListener);
		
		
		ImageButton startButton = (ImageButton) view.findViewById(R.id.counter_start_button);
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				localBinder.getService().startTimer(timerName, timerTime);
				Fragment timerListFragment = fm.findFragmentById(R.id.fragment_timer_list);
				if (timerListFragment != null) {
					MyApplication.showRunningTimers = true;
					timerListFragment.getActivity().finish();
//					Intent i = new Intent(context, MainActivity.class);
//					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
//							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				} else {
					RunningTimersFragment runningFragment = (RunningTimersFragment) fm.findFragmentByTag("running");
					if (runningFragment != null) {
						runningFragment.refreshTimerList();
					}
				}
			}
		});
		
		ImageButton popupMenuButton = (ImageButton) view.findViewById(R.id.timer_dropdown_button);
		popupMenuButton.setOnClickListener(timerOnClickListener);
		
//		view.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
////				CreateTimerFragment fragment = (CreateTimerFragment) fm.findFragmentById(R.id.fragment_create_timer);
////				fragment.showTimer(timerId);
//				
//			}
//		});
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.listcounters, parent, false);
		bindView(v, context, cursor);
		return v;

	}
	
	
	
}

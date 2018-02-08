package com.tomatodev.timerdroid.fragments;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tomatodev.timerdroid.Constants;
import com.tomatodev.timerdroid.MyApplication;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.TimerCursorAdapter;
import com.tomatodev.timerdroid.Utilities;
import com.tomatodev.timerdroid.activities.TimerActivity;
import com.tomatodev.timerdroid.persistence.TimersProvider;
import com.tomatodev.timerdroid.service.AbstractCountDown;
import com.tomatodev.timerdroid.service.ITimerUpdatedHandler;
import com.tomatodev.timerdroid.service.TimerService;
import com.tomatodev.timerdroid.service.TimerService.CountDown;
import com.tomatodev.timerdroid.service.TimerService.LocalBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class RunningTimersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ITimerUpdatedHandler{

	private LocalBinder mLocalBinder;

	private TimerCursorAdapter items;

	private TableLayout timersTable;

	private Map<Integer, TextView> views = new ConcurrentHashMap<Integer, TextView>();

	private Map<Integer, MyCount> counters = new ConcurrentHashMap<Integer, MyCount>();

	private List<Integer> mappingCountersToRows = new ArrayList<Integer>();

	private TextView tvNoRunningTimers;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.setHasOptionsMenu(true);

		tvNoRunningTimers = getActivity().findViewById(R.id.main_textview_running);

		MyApplication.mainVisible = true;
	}

    @Override
    public void onPause() {
        super.onPause();
        if (mLocalBinder != null){
            mLocalBinder.getService().deregisterListener(this);
        }
    }

    @Override
	public void onResume() {
		super.onResume();

		this.bindTimerService();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_bar_running_timers, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_timer:
			Intent i = new Intent(getActivity(), TimerActivity.class);
			startActivity(i);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.running_timers, container, false);
	}

	private void bindTimerService(){
        Intent intent = new Intent(getActivity(), TimerService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mLocalBinder = null;
                mLocalBinder.getService().deregisterListener(RunningTimersFragment.this);
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mLocalBinder = (LocalBinder) service;
                mLocalBinder.getService().registerListener(RunningTimersFragment.this);

                refreshRunningTimersList();
                fillFavorites();
            }
        };
        boolean success = getActivity().getApplicationContext().bindService(intent, serviceConnection,
                Service.BIND_AUTO_CREATE);
        if (!success) {
            // TODO: do something
        }
    }

	public void fillRunningTimersTable() {
		Map<Integer, CountDown> timersUnsorted = mLocalBinder.getService().getTimers();

		// not yet sorted :-(
		TreeMap<Integer, CountDown> timers = new TreeMap<Integer, CountDown>(timersUnsorted);

		if (timers.keySet().size() > 0)
			tvNoRunningTimers.setVisibility(View.GONE);
		else
			tvNoRunningTimers.setVisibility(View.VISIBLE);

		int i = 0;
		for (Integer timerNumber : timers.keySet()) {

			i++;
			final int number = timerNumber;
			final int rowNumber = i;// TODO: remove hack

			// use Math.round or the timers will not count down at
			// the same
			// time on the UI
			long timeLeft = Math.round(timers.get(timerNumber).getTimeLeft() / 1000.0) * 1000;
			MyCount countdown;

			// final long originalTime =
			// timers.get(timerNumber).getOriginalTime();
			final String currentTimerName = timers.get(timerNumber).getName();

			if (timers.get(timerNumber).isStarted()) {
				countdown = new MyCount(timeLeft, 500, currentTimerName, i);
				countdown.start();
			} else {
				countdown = new MyCount(0, 500, currentTimerName, i);
			}
			counters.put(timerNumber, countdown);

			// Fill tablelayout with timers
			TableRow row = new TableRow(getActivity());
			row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			row.setGravity(Gravity.CENTER_VERTICAL);

			row.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mLocalBinder.getService().stopSound();
					// numberRunning = number;
					// rowNumberRunning = rowNumber;
					// length = originalTime;
					// timerName = currentTimerName;
					// mBarRunningTimer.show(v);

				}
			});

			timersTable.addView(row);
			mappingCountersToRows.add(number);

			TextView tvName = new TextView(getActivity());
			tvName.setText(timers.get(timerNumber).getName());
			row.addView(tvName);
			// tvName.setTextColor(Color.argb(255, 102, 153, 255));
			// tvName.setTextColor(Color.rgb(168, 194, 78));

			tvName.setPadding(3, 3, 3, 3);
			// tvName.setTextSize(20);
			tvName.setTextSize(19);
			// tvName.setTextAppearance(getApplicationContext(),
			// android.R.style.TextAppearance_Medium);
			tvName.setSingleLine(true);
			tvName.setLayoutParams(new TableRow.LayoutParams(android.widget.TableLayout.LayoutParams.WRAP_CONTENT,
					android.widget.TableLayout.LayoutParams.FILL_PARENT));
			tvName.setGravity(Gravity.LEFT);
			
			View.OnClickListener timerOnClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					    PopupMenu popup = new PopupMenu(getActivity(), v);
					    popup.inflate(R.menu.popup_running_timer);
					    popup.show();
					    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								switch (item.getItemId()) {
								case R.id.popup_timer_delete:

									mLocalBinder.getService().stopSound();

									// Show confirmation dialog if timer is running
									if (mLocalBinder.getService().isStarted(number)) {
										AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
										builder.setMessage(
												getString(R.string.main_deletedialog_title_1) + " " + counters.get(number).getName()
														+ " " + getString(R.string.main_deletedialog_title_2))
												.setCancelable(false)
												.setPositiveButton(getString(R.string.main_deletedialog_yes),
														new DialogInterface.OnClickListener() {
															public void onClick(DialogInterface dialog, int id) {
																mLocalBinder.getService().deleteTimer(number);

																counters.get(number).cancel();
																counters.remove(number);

																views.remove(rowNumber);
																int rowIndexToDelete = mappingCountersToRows.indexOf(number);
																timersTable.removeViewAt(rowIndexToDelete);
																mappingCountersToRows.remove(rowIndexToDelete);
																
																if (counters.size() == 0) {
																	tvNoRunningTimers.setVisibility(View.VISIBLE);
																}
															}
														})
												.setNegativeButton(getString(R.string.main_deletedialog_no),
														new DialogInterface.OnClickListener() {
															public void onClick(DialogInterface dialog, int id) {
																dialog.cancel();
															}
														});
										AlertDialog alert = builder.create();
										alert.show();
									} else {
										mLocalBinder.getService().deleteTimer(number);
										counters.get(number).cancel();
										counters.remove(number);

										views.remove(rowNumber);
										timersTable.removeViewAt(mappingCountersToRows.indexOf(number));
										mappingCountersToRows.remove(mappingCountersToRows.indexOf(number));
										
										if (counters.size() == 0) {
											tvNoRunningTimers.setVisibility(View.VISIBLE);
										}
									}

								
									return true;
								default:
									break;
								}
								return false;
							}
						});
				}
			};
			tvName.setOnClickListener(timerOnClickListener);

			TextView tvTime = new TextView(getActivity());
			if (timers.get(timerNumber).isStarted()) {
				tvTime.setText(Utilities.formatTime(timers.get(timerNumber).getTimeLeft()));
				//tvTime.setTextColor(0xffffffff);
				//tvName.setTextColor(Color.rgb(255, 255, 255));
			} else {
				tvTime.setText(Utilities.formatTime(0));
				tvTime.setTextColor(0xffff0000);
				tvName.setTextColor(0xffff0000);
			}
			// tvTime.setTextSize(20);
			tvTime.setTextSize(16);
			row.addView(tvTime);
			tvTime.setPadding(3, 3, 3, 3);
			tvTime.setGravity(Gravity.RIGHT);
			tvTime.setOnClickListener(timerOnClickListener);

			views.put(i, tvTime);

			// --------------- Stop button -------------------
			ImageView stopButton = new ImageView(getActivity().getApplicationContext());
			//stopButton.setImageDrawable(getResources().getDrawable((android.R.drawable.ic_menu_delete)));
			stopButton.setImageDrawable(getResources().getDrawable((R.drawable.discard)));
			stopButton.setAdjustViewBounds(true);
			stopButton.setPadding(3, 3, 3, 3);
//			stopButton.setLayoutParams(new TableRow.LayoutParams(40, 40));
			stopButton.setScaleType(ScaleType.FIT_CENTER);
			stopButton.setScaleX(1.3f);
			stopButton.setScaleY(1.3f);

			row.addView(stopButton);
			stopButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					mLocalBinder.getService().stopSound();

					// Show confirmation dialog if timer is running
					if (mLocalBinder.getService().isStarted(number)) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(
								getString(R.string.main_deletedialog_title_1) + " " + counters.get(number).getName()
										+ " " + getString(R.string.main_deletedialog_title_2))
								.setCancelable(false)
								.setPositiveButton(getString(R.string.main_deletedialog_yes),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												mLocalBinder.getService().deleteTimer(number);

												counters.get(number).cancel();
												counters.remove(number);

												views.remove(rowNumber);
												int rowIndexToDelete = mappingCountersToRows.indexOf(number);
												timersTable.removeViewAt(rowIndexToDelete);
												mappingCountersToRows.remove(rowIndexToDelete);
												
												if (counters.size() == 0) {
													tvNoRunningTimers.setVisibility(View.VISIBLE);
												}
											}
										})
								.setNegativeButton(getString(R.string.main_deletedialog_no),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												dialog.cancel();
											}
										});
						AlertDialog alert = builder.create();
						alert.show();
					} else {
						mLocalBinder.getService().deleteTimer(number);
						counters.get(number).cancel();
						counters.remove(number);

						views.remove(rowNumber);
						timersTable.removeViewAt(mappingCountersToRows.indexOf(number));
						mappingCountersToRows.remove(mappingCountersToRows.indexOf(number));
						
						if (counters.size() == 0) {
							tvNoRunningTimers.setVisibility(View.VISIBLE);
						}
					}

				}
			});
		}
	}

	private void fillFavorites() {
		getLoaderManager().restartLoader(0, null, this);
		items = new TimerCursorAdapter(this.getActivity(), null, getFragmentManager(), mLocalBinder);

		ListView lv = getActivity().findViewById(R.id.main_list_favorites);
		lv.setAdapter(items);
	}

	private void clearRunningTimers() {
		timersTable = (TableLayout) getActivity().findViewById(R.id.main_timers_table);
		if (timersTable != null) {
			timersTable.removeAllViews();
		}
		mappingCountersToRows.clear();

		for (MyCount counter : counters.values()) {
			counter.cancel();
		}
		counters.clear();
		views.clear();
	}

	public void refreshRunningTimersList() {
		clearRunningTimers();
		fillRunningTimersTable();
	}

	static final String[] PROJECTION = new String[] { TimersProvider.TimerTable._ID,
			TimersProvider.TimerTable.TIMER_KEY_NAME, TimersProvider.TimerTable.TIMER_KEY_TIME,
			TimersProvider.TimerTable.TIMER_KEY_CATEGORY, TimersProvider.TimerTable.TIMER_KEY_FAVORITE, };

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cl = new CursorLoader(getActivity(), TimersProvider.TimerTable.CONTENT_URI, PROJECTION,
				TimersProvider.TimerTable.TIMER_KEY_FAVORITE + "= 1", null, null);
		return cl;
	}

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
		items.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
		items.swapCursor(null);
    }

    @Override
    public void onTimersChanged() {
        this.refreshRunningTimersList();
    }

    public class MyCount extends AbstractCountDown {

		private int textViewId;

		public MyCount(long millisInFuture, long countDownInterval, String name, int textViewId) {
			super(millisInFuture, countDownInterval, name);
			this.textViewId = textViewId;
		}

		public void onFinish() {
			TextView textView = views.get(textViewId);

			if (textView != null) {
				textView.setText(Utilities.formatTime(0));
				((TextView) textView).setTextColor(0xffff0000);

				// TODO remove if not needed anymore
//				timersTable.removeAllViews();
//				mappingCountersToRows.clear();

//				for (MyCount counter : counters.values()) {
//					counter.cancel();
//				}
//				counters.clear();
//				views.clear();

			} else {
				Log.e(Constants.LOG_TAG, "onFinish: textview is null");
			}

		}

		public void onTick(long millisUntilFinished) {
			TextView textView = views.get(textViewId);
			if (textView != null) {
				textView.setText(Utilities.formatTime(millisUntilFinished));
			} else {
				Log.e(Constants.LOG_TAG, "onTick: textview is null");
			}
		}
	}
}

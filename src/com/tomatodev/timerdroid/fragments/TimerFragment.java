package com.tomatodev.timerdroid.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.tomatodev.timerdroid.MyApplication;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.Utilities;
import com.tomatodev.timerdroid.activities.MainActivity;
import com.tomatodev.timerdroid.persistence.CategoriesProvider;
import com.tomatodev.timerdroid.persistence.CategoriesProvider.CategoriesTable;
import com.tomatodev.timerdroid.persistence.DbAdapter;
import com.tomatodev.timerdroid.persistence.TimersProvider;
import com.tomatodev.timerdroid.persistence.TimersProvider.TimerTable;
import com.tomatodev.timerdroid.service.TimerService;
import com.tomatodev.timerdroid.service.TimerService.LocalBinder;

public class TimerFragment extends Fragment {

	private NumberPicker pickerHrs;
	private NumberPicker pickerMins;
	private NumberPicker pickerSecs;

	private EditText timerNameEditText;

	private LocalBinder localBinder;
	private Spinner categorySpinner;
	private CheckBox favoriteCheckBox;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		ActionBar actionBar = getActivity().getActionBar();
//	    actionBar.setDisplayShowHomeEnabled(true);
	    actionBar.setHomeButtonEnabled(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		pickerHrs = (NumberPicker) getActivity().findViewById(R.id.timer_picker_hrs);
		pickerMins = (NumberPicker) getActivity().findViewById(R.id.timer_picker_mins);
		pickerSecs = (NumberPicker) getActivity().findViewById(R.id.timer_picker_secs);

		pickerHrs.setMinValue(0);
		pickerHrs.setMaxValue(1000);
		pickerHrs.setValue(0);
		pickerHrs.setOnLongPressUpdateInterval(200);

		pickerMins.setMinValue(0);
		pickerMins.setMaxValue(59);
		pickerMins.setValue(0);
		pickerMins.setOnLongPressUpdateInterval(200);

		pickerSecs.setMinValue(0);
		pickerSecs.setMaxValue(59);
		pickerSecs.setValue(0);
		pickerSecs.setOnLongPressUpdateInterval(200);

		timerNameEditText = (EditText) getActivity().findViewById(R.id.timerName);
		
		favoriteCheckBox = (CheckBox) getActivity().findViewById(R.id.checkBoxFavorite);

		// Start Timer
		Intent intent = new Intent(this.getActivity(), TimerService.class);
		// intent.putExtra("time", length);
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
		boolean success = getActivity().getApplicationContext().bindService(intent, serviceConnection,
				Activity.BIND_AUTO_CREATE);
		// if (!success) {
		// // TODO: do something
		// }

		categorySpinner = (Spinner) getActivity().findViewById(R.id.spinner_category);
		Cursor cursor = getCategoriesCursor();
		getActivity().startManagingCursor(cursor);
		String[] columns = new String[] { DbAdapter.CATEGORIES_KEY_NAME };
		int[] to = new int[] { android.R.id.text1 };
		SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this.getActivity(),
				android.R.layout.simple_spinner_item, cursor, columns, to);

		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(mAdapter);
		
		
		int timerId = getActivity().getIntent().getIntExtra("timerId", -1);
		if (timerId != -1) {
			showTimer(timerId);
		} else {
			int categoryId = getActivity().getIntent().getIntExtra("categoryId", -1);
			if (categoryId != -1) {
				adjustCategorySpinner(categoryId);
			}
		}

	}

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.timer, container, false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_bar_timer_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_start_timer:
			startTimer();
			return true;
		case R.id.menu_save_timer:
			saveTimer();
			return true;
		case android.R.id.home:
			Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
			MyApplication.showRunningTimers = true;
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getActivity().getApplication().startActivity(intent);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startTimer() {
		long length = Utilities.computeLength(pickerHrs.getValue(), pickerMins.getValue(), pickerSecs.getValue());
		if (localBinder != null) {
			String timerName = timerNameEditText.getText().toString();

			localBinder.getService().startTimer(timerName, length);
		}
		MyApplication.showRunningTimers = true;
		getActivity().finish();
		Intent i = new Intent(getActivity().getApplicationContext(), MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	private void saveTimer() {
		long length = Utilities.computeLength(pickerHrs.getValue(), pickerMins.getValue(), pickerSecs.getValue());
		String timerName = timerNameEditText.getText().toString();
		int isFavorite = 0;
		if (favoriteCheckBox.isChecked()) {
			isFavorite = 1;
		}

		if (length == 0 || timerName.length() == 0) {
			if (length == 0) {
				Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.create_timer_no_time_set,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 40);
				toast.show();
			} else {
				Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.create_timer_no_name_set,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 40);
				toast.show();
			}

		} else {
			long category = categorySpinner.getSelectedItemId();

			final String[] projection = new String[] { TimersProvider.TimerTable._ID,
					TimersProvider.TimerTable.TIMER_KEY_NAME, TimersProvider.TimerTable.TIMER_KEY_TIME,
					TimersProvider.TimerTable.TIMER_KEY_CATEGORY, TimersProvider.TimerTable.TIMER_KEY_FAVORITE, };
			Cursor cursor = getActivity().getContentResolver().query(TimerTable.CONTENT_URI, projection,
					TimersProvider.TimerTable.TIMER_KEY_NAME + "=" + DatabaseUtils.sqlEscapeString(timerName) + "", null, TimerTable.TIMER_KEY_ID);

			// Cursor cursor =
			// mDbHelper.fetchTimer(enterName.getText().toString());
			cursor.moveToFirst();
			int id = -1;
			if (!cursor.isAfterLast()) { // TODO always updates the
				// first timer with this
				// name
				id = cursor.getInt(0);
				cursor.moveToNext();
			}

			if (id > -1) {

				ContentValues values = new ContentValues();
				values.put(TimerTable.TIMER_KEY_NAME, timerName);
				values.put(TimerTable.TIMER_KEY_TIME, String.valueOf(length));
				values.put(TimerTable.TIMER_KEY_CATEGORY, (int) category);
				values.put(TimerTable.TIMER_KEY_FAVORITE, isFavorite);
				getActivity().getContentResolver().update(
						Uri.parse(TimerTable.CONTENT_ID_URI_BASE + "/" + Integer.toString(id)), values, null, null);

				Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.create_timer_timer_saved,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 40);
				toast.show();
			} else {
				ContentValues values = new ContentValues();
				values.put(TimerTable.TIMER_KEY_NAME, timerName);
				values.put(TimerTable.TIMER_KEY_TIME, String.valueOf(length));
				values.put(TimerTable.TIMER_KEY_CATEGORY, (int) category);
				values.put(TimerTable.TIMER_KEY_FAVORITE, isFavorite);
				getActivity().getContentResolver().insert(TimerTable.CONTENT_URI, values);

				Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.create_timer_timer_saved,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 40);
				toast.show();
			}

		}

	}
	
	public void showTimer(int id) {

		final String[] projection = new String[] { TimersProvider.TimerTable._ID,
				TimersProvider.TimerTable.TIMER_KEY_NAME, TimersProvider.TimerTable.TIMER_KEY_TIME,
				TimersProvider.TimerTable.TIMER_KEY_CATEGORY,
				TimersProvider.TimerTable.TIMER_KEY_FAVORITE, };
		Cursor cursor = getActivity().getContentResolver().query(
				Uri.parse(TimerTable.CONTENT_ID_URI_BASE + "/" + id), projection, null, null,
				TimerTable.TIMER_KEY_ID);

		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			// Update text field
			timerNameEditText.setText(cursor.getString(cursor.getColumnIndex(TimerTable.TIMER_KEY_NAME)));
			
			// Set favorite field
			if (cursor.getInt(cursor.getColumnIndex(TimerTable.TIMER_KEY_FAVORITE)) == 1) {
				favoriteCheckBox.setChecked(true);
			} else {
				favoriteCheckBox.setChecked(false);
			}

			// Update time
			long updateValue = cursor.getLong(cursor.getColumnIndex(TimerTable.TIMER_KEY_TIME));

			int times[] = Utilities.lengthToTime(updateValue);
			pickerSecs.setValue(times[0]);
			pickerMins.setValue(times[1]);
			pickerHrs.setValue(times[2]);

			// Update Category
			int catID = cursor.getInt(cursor.getColumnIndex(TimerTable.TIMER_KEY_CATEGORY));

			adjustCategorySpinner(catID);
		}

	}

	private void adjustCategorySpinner(int catID) {
		// Cursor cursorAllCat = mDbHelper.fetchAllCategories();
		Cursor cursorAllCat = getCategoriesCursor();

		// loop over all categories to find the position for the current
		// category id
		cursorAllCat.moveToFirst();
		int position = 1;
		do {
			if (cursorAllCat.getInt(0) == catID)
				break;

			cursorAllCat.moveToNext();
			position++;
		} while (!cursorAllCat.isLast());

		String[] columns = new String[] { CategoriesTable.CATEGORIES_KEY_NAME };
		int[] to = new int[] { android.R.id.text1 };

		SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this.getActivity(),
				android.R.layout.simple_spinner_item, cursorAllCat, columns, to);

		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(mAdapter);

		categorySpinner.setSelection(position - 1);
	}

	private Cursor getCategoriesCursor() {
		final String[] categoryProjection = new String[] { CategoriesProvider.CategoriesTable._ID,
				CategoriesProvider.CategoriesTable.CATEGORIES_KEY_NAME,
				CategoriesProvider.CategoriesTable.CATEGORIES_KEY_PARENT_CATEGORY,
				CategoriesProvider.CategoriesTable.CATEGORIES_KEY_IMAGE, };
		Cursor cursorAllCat = getActivity().getContentResolver().query(CategoriesTable.CONTENT_URI, categoryProjection,
				null, null, CategoriesTable.CATEGORIES_KEY_NAME);
		return cursorAllCat;
	}

}

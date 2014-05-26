/**
 * 
 */
package com.tomatodev.timerdroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.tomatodev.timerdroid.MyApplication;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.activities.MainActivity;

/**
 * @author martin
 * 
 */
public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		setHasOptionsMenu(true);
		getActivity().getActionBar().setHomeButtonEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
			MyApplication.showRunningTimers = true;
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getActivity().getApplication().startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

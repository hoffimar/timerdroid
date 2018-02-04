package com.tomatodev.timerdroid.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomatodev.timerdroid.R;

public class AboutDialogFragment extends DialogFragment {

	public static AboutDialogFragment newInstance() {
		AboutDialogFragment fragment = new AboutDialogFragment();
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		Context mContext = getActivity();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about_dialog, (ViewGroup) getActivity().findViewById(R.id.main_layout));

		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);
		builder.setPositiveButton(R.string.menu_about_button_text, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		alertDialog = builder.create();
		alertDialog.setTitle(R.string.menu_about);
		return alertDialog;
	}

}

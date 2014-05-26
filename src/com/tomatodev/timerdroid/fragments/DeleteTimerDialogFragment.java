package com.tomatodev.timerdroid.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.persistence.TimersProvider.TimerTable;

public class DeleteTimerDialogFragment extends DialogFragment {

	public static DeleteTimerDialogFragment newInstance(String title, int timerId) {
		DeleteTimerDialogFragment frag = new DeleteTimerDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", timerId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        final int id = getArguments().getInt("id");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.main_deletedialog_yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            doPositiveClick(id);
                        }
                    }
                )
                .setNegativeButton(R.string.main_deletedialog_no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            doNegativeClick();
                        }
                    }
                )
                .create();
    }
    
    public void doPositiveClick(int timerId) {
    	getActivity().getContentResolver().delete(Uri.parse(TimerTable.CONTENT_ID_URI_BASE	+ "/" + 
    			Integer.toString(timerId)),
				null, null);
    }

    public void doNegativeClick() {
    	
    }

	
}

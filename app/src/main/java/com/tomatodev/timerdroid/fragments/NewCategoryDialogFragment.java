package com.tomatodev.timerdroid.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.persistence.CategoriesProvider.CategoriesTable;

public class NewCategoryDialogFragment extends DialogFragment {

	private static final int SELECT_IMAGE = 0;
	private Uri imageUri;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.categories_add_category);

		View v = inflater.inflate(R.layout.rename_category_dialog, container, false);

		final EditText categoryName = (EditText) v.findViewById(R.id.rename_category_inputfield);

		Button imagePickerButton = (Button) v.findViewById(R.id.rename_category_chooseimage_button);
		imagePickerButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
						SELECT_IMAGE);
			}

		});

		Button okButton = (Button) v.findViewById(R.id.rename_category_okbutton);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (imageUri != null) {
					ContentValues values = new ContentValues();
					values.put(CategoriesTable.CATEGORIES_KEY_NAME, categoryName.getText()
							.toString());
					values.put(CategoriesTable.CATEGORIES_KEY_PARENT_CATEGORY, 0);
					values.put(CategoriesTable.CATEGORIES_KEY_IMAGE, imageUri.toString());
					getActivity().getContentResolver().insert(CategoriesTable.CONTENT_URI, values);
				} else {
					ContentValues values = new ContentValues();
					values.put(CategoriesTable.CATEGORIES_KEY_NAME, categoryName.getText()
							.toString());
					values.put(CategoriesTable.CATEGORIES_KEY_PARENT_CATEGORY, 0);
					getActivity().getContentResolver().insert(CategoriesTable.CONTENT_URI, values);
				}
				NewCategoryDialogFragment.this.dismiss();
			}
		});

		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_IMAGE) {
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				imageUri = selectedImage;
			}
		}
	}

}

package com.tomatodev.timerdroid;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomatodev.timerdroid.persistence.DbAdapter;

public class CategoriesCursorAdapter extends CursorAdapter {

	public CategoriesCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		ImageView imageView = (ImageView) view.findViewById(R.id.categories_image);
		String imageUri = cursor.getString(cursor.getColumnIndex(DbAdapter.CATEGORIES_KEY_IMAGE));
		if (imageUri != null){
			if (imageUri.contains("://")) {
				imageView.setImageBitmap(Utilities.decodeFile(context, imageUri));
			} else {
				imageUri = "android.resource://com.tomatodev.timerdroid/" + context.getResources().getIdentifier(imageUri, "drawable", "com.tomatodev.timerdroid");
				imageView.setImageBitmap(Utilities.decodeFile(context, imageUri));
			}
		} else {
			//imageView.setImageResource(android.R.drawable.ic_menu_gallery);
			imageView.setImageResource(R.drawable.collection);
		}
		
		
		TextView name = (TextView) view.findViewById(R.id.categories_name);
		name.setText(cursor.getString(cursor.getColumnIndex(DbAdapter.CATEGORIES_KEY_NAME)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.categories, parent, false);
//		bindView(v, context, cursor);
		return v;
	}
	
	
	


}

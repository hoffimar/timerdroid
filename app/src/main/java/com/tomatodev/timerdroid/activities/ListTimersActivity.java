package com.tomatodev.timerdroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tomatodev.timerdroid.R;

public class ListTimersActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_timers_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final int categoryId = getIntent().getIntExtra("category_id", 1);

        FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(v -> {
            Intent i = new Intent(this, TimerActivity.class);
            i.putExtra("categoryId", categoryId);
            startActivity(i);
		});
	}
	
}

package com.tomatodev.timerdroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.fragments.AboutDialogFragment;
import com.tomatodev.timerdroid.fragments.CategoriesFragment;
import com.tomatodev.timerdroid.fragments.NewCategoryDialogFragment;
import com.tomatodev.timerdroid.fragments.RunningTimersFragment;

public class HomeActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            switch (mViewPager.getCurrentItem()){
                case 0:
                    Intent i = new Intent(this, TimerActivity.class);
                    startActivity(i);
                    break;
                case 1:
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.addToBackStack(null);
                    NewCategoryDialogFragment newCategoryFragment = new NewCategoryDialogFragment();
                    newCategoryFragment.show(ft, "dialog");
                    break;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, UserSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_about:
                DialogFragment newFragment = AboutDialogFragment.newInstance();
                newFragment.show(this.getSupportFragmentManager(), "about_dialog");
                return true;
            case R.id.menu_feedback:
                Intent myIntent = new Intent(android.content.Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                myIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.menu_feedback_subject));
                myIntent.putExtra(Intent.EXTRA_EMAIL, new String [] { getString(R.string.menu_feedback_address) });
                startActivity(Intent.createChooser(myIntent, getResources().getString(R.string.feedback_chooser_title)));
                return true;
            case R.id.menu_share:
                onInviteClicked();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.menu_share))
                .setMessage(getString(R.string.menu_share_subject))
                .build();
        startActivityForResult(intent, 100);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position){
                case 0: return new RunningTimersFragment();
                case 1: return new CategoriesFragment();
            }
            throw new IllegalArgumentException("position for tab not valid: " + position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}

package com.danielkim.soundrecorder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.fragments.FileViewerFragment;
import com.danielkim.soundrecorder.fragments.LicensesFragment;
import com.danielkim.soundrecorder.fragments.RecordFragment;
import com.danielkim.soundrecorder.settings.SettingsActivity;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.Delete);
        item.setVisible(false);
        MenuItem item2 = menu.findItem(R.id.cancel);
        item2.setVisible(false);
        MenuItem item3 = menu.findItem(R.id.a);
        item3.setVisible(false);
        MenuItem item4 = menu.findItem(R.id.aa);
        item4.setVisible(false);
        MenuItem item5 = menu.findItem(R.id.aaa);
        item5.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_licenses:
                openLicenses();
                return true;
            case R.id.Settings:
                openSettings();
                return true;
            case R.id.Delete:    //disable menu
                return false;
            case R.id.a:
                return false;
            case R.id.aa:
                return false;
            case R.id.aaa:
                return false;
            case R.id.cancel:
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openLicenses() {
        LicensesFragment licensesFragment = new LicensesFragment();
        licensesFragment.show(getSupportFragmentManager().beginTransaction(), "dialog_licenses");
    }

    public void openSettings() {
        Intent modifySettings = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(modifySettings);
    }

    public class MyAdapter extends FragmentPagerAdapter {
        private String[] titles = {getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings)};

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return RecordFragment.newInstance(position);
                }
                case 1: {
                    return FileViewerFragment.newInstance(position);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    public MainActivity() {
    }

    //hide toolbar title
    public void disableToolbar() {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
    //hide toolbar title
    public void enableToolbar() {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }
}

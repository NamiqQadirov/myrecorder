package com.danielkim.soundrecorder.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.danielkim.soundrecorder.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

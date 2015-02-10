/*
 * Copyright (C) 2012 The CyanogenMod Project (DvTonder)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.lockclock.preference;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.cyanogenmod.lockclock.ClockWidgetProvider;
import com.cyanogenmod.lockclock.R;
import com.cyanogenmod.lockclock.misc.Constants;
import com.cyanogenmod.lockclock.misc.Preferences;
import com.cyanogenmod.lockclock.weather.WeatherUpdateService;

public class WeatherWidgetPreferences extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "WeatherWidgetPreferences";

    private static final String WEATHER_REFRESH_KEYS =
        Constants.SHOW_WEATHER;

    private ListPreference mFontColor;
    private ListPreference mTimestampFontColor;
    private IconSelectionPreference mIconSet;

    private Context mContext;
    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.preferences_widget_weather);
        mContext = getActivity();
        mResolver = mContext.getContentResolver();

        getActivity().getActionBar().setTitle(getResources().getString(R.string.weather_category));

        // Load items that need custom summaries etc.
        mFontColor = (ListPreference) findPreference(Constants.WEATHER_FONT_COLOR);
        mTimestampFontColor = (ListPreference) findPreference(Constants.WEATHER_TIMESTAMP_FONT_COLOR);
        mIconSet = (IconSelectionPreference) findPreference(Constants.WEATHER_ICONS);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateFontColorsSummary();
        updateIconSetSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }

        boolean needWeatherUpdate = false;

        if (pref == mIconSet) {
            updateIconSetSummary();
        }

        if (key.equals(Constants.SHOW_WEATHER)) {
            needWeatherUpdate = true;
        }

        if (Constants.DEBUG) {
            Log.v(TAG, "Preference " + key + " changed, need update "  + needWeatherUpdate);
        }

        if (Preferences.showWeather(mContext) && needWeatherUpdate) {
            Intent updateIntent = new Intent(mContext, WeatherUpdateService.class);
            mContext.startService(updateIntent);
        }

        Intent updateIntent = new Intent(mContext, ClockWidgetProvider.class);
        mContext.sendBroadcast(updateIntent);
    }

    //===============================================================================================
    // Utility classes and supporting methods
    //===============================================================================================

    private void updateFontColorsSummary() {
        if (mFontColor != null) {
            mFontColor.setSummary(mFontColor.getEntry());
        }
        if (mTimestampFontColor != null) {
            mTimestampFontColor.setSummary(mTimestampFontColor.getEntry());
        }
    }

    private void updateIconSetSummary() {
        if (mIconSet != null) {
            mIconSet.setSummary(mIconSet.getEntry());
        }
    }
}

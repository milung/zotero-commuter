package sk.mung.sentience.zoterosentience;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import sk.mung.sentience.zoterocommuter.R;

public  class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String KEY_SYNC_FREQUENCY = "sync_frequency";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(KEY_SYNC_FREQUENCY)) {
            BootSchedulerReceiver.scheduleSynchronizing(getActivity(),true);
        }
    }
}

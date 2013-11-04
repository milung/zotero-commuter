package sk.mung.sentience.zoterosentience;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
        {
            //noinspection deprecation
            addPreferencesFromResource(R.xml.preferences);
        }
        else
        {
            getFragmentManager().beginTransaction()
                    .replace(
                            android.R.id.content,
                            new SettingsFragment())
                    .commit();
            ActionBar actionBar = getActionBar();
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

}
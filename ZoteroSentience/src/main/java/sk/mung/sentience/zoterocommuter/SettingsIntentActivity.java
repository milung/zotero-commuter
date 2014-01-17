package sk.mung.sentience.zoterocommuter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import sk.mung.zoteroapi.ZoteroSync;

public class SettingsIntentActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        if("sk.mung.sentience.zoterocommuter.clear_library".equals(action))
        {
            Intent serviceStartIntent;
            serviceStartIntent = new Intent(this, SynchronizingService.class);
            serviceStartIntent.putExtra(
                    SynchronizingService.SYNCHRONIZATION_TYPE,
                    SynchronizingService.MSG_FULL_SYNCHRONIZE_MANUAL);
            this.startService(serviceStartIntent);
            finish();
        }
        if("sk.mung.sentience.zoterocommuter.delete_attachments".equals(action))
        {
            ZoteroSync sync = ((GlobalState)getApplication()).getZoteroSync();
            sync.deleteAllAttachments();
            finish();
        }
    }
}
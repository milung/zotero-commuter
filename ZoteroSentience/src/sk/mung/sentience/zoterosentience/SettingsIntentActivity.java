package sk.mung.sentience.zoterosentience;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

public class SettingsIntentActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        if("sk.mung.sentience.zoterosentience.clear_library".equals(action))
        {
            Intent serviceStartIntent;
            serviceStartIntent = new Intent(this, SynchronizingService.class);
            serviceStartIntent.putExtra(
                    SynchronizingService.SYNCHRONIZATION_TYPE,
                    SynchronizingService.MSG_FULL_SYNCHRONIZE_MANUAL);
            this.startService(serviceStartIntent);
            finish();
        }
        if("sk.mung.sentience.zoterosentience.delete_attachments".equals(action))
        {
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS );
            assert dir != null;
            //noinspection ResultOfMethodCallIgnored
            deleteDirectory(dir);
            finish();
        }
    }


    private boolean deleteDirectory(File directory)
    {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for (File file : files)
                {
                    if (file.isDirectory())
                    {
                        deleteDirectory(file);
                    } else
                    {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

}
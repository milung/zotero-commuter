package sk.mung.sentience.zoterosentience;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class SyncEventsReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        Intent serviceStartIntent;
        serviceStartIntent = new Intent(context, SynchronizingService.class);
        serviceStartIntent.putExtra(SynchronizingService.SYNCHRONIZATION_TYPE, SynchronizingService.MSG_SYNCHRONIZE);
        context.startService(serviceStartIntent);
    }
}

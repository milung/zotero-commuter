package sk.mung.sentience.zoterosentience;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;


public class BootSchedulerReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        scheduleSynchronizing(context,false);
    }

    static void scheduleSynchronizing(Context context, boolean reschedule)
    {
        GlobalState globalState;
        globalState = ((GlobalState)context.getApplicationContext());
        assert globalState != null;
        SharedPreferences preferences = globalState.getPreferences();
        assert preferences!=null;
        int period = getDownloadPeriod(preferences);

        Intent schedulerIntent = new Intent(context, SyncEventsReceiver.class);
        schedulerIntent.putExtra(SynchronizingService.SYNCHRONIZATION_TYPE, SynchronizingService.MSG_SYNCHRONIZE);

        PendingIntent pendingIntent
                = PendingIntent.getBroadcast(globalState, 0, schedulerIntent, PendingIntent.FLAG_NO_CREATE);
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if(period == 0 || reschedule && pendingIntent != null) // reschedule usually after period change
        {
            alarm.cancel(pendingIntent);
            pendingIntent =null;
        }
        if(period > 0 && pendingIntent == null)
        {
            pendingIntent = PendingIntent.getBroadcast(context, 0, schedulerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarm.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + period,
                    period,
                    pendingIntent);
        }
    }

    private static int getDownloadPeriod( SharedPreferences preferences )
    {
        String frequency = preferences.getString("sync_frequency", "sync_one_hour");

        if(SynchronizingService.SYNC_FREQUENCY_NEVER.equals(frequency))
        {
            return 0;
        }
        if(SynchronizingService.SYNC_FREQUENCY_CHARGING.equals(frequency) ||
                SynchronizingService.SYNC_FREQUENCY_ONE_HOUR.equals(frequency) )
        {
            return 60;
        }

        if(SynchronizingService.SYNC_FREQUENCY_FIFTEEN_MINUTES.equals(frequency)) return 15;
        if(SynchronizingService.SYNC_FREQUENCY_SIX_HOURS.equals(frequency)) return 360;
        if(SynchronizingService.SYNC_FREQUENCY_ONE_DAY.equals(frequency)) return 24*60;
        return 60;
    }
}

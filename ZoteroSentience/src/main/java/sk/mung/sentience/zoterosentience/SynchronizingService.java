package sk.mung.sentience.zoterosentience;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;

import sk.mung.sentience.zoterocommuter.R;

public class SynchronizingService extends IntentService
{
    public static final int MSG_SYNCHRONIZE = 1;
    public static final int MSG_FULL_SYNCHRONIZE_MANUAL = 2;
    public static final int MSG_SYNCHRONIZE_MANUAL = 3;

    public static final String SYNCHRONIZATION_TYPE = "sync_type";
    private static final long PERIOD_TOLERANCE = 10000;
    public static final String SYNC_FREQUENCY_NEVER = "sync_never";
    public static final String SYNC_FREQUENCY_CHARGING = "sync_charging";
    public static final String SYNC_FREQUENCY_FIFTEEN_MINUTES = "sync_fifteen_minutes";
    public static final String SYNC_FREQUENCY_ONE_HOUR = "sync_one_hour";
    public static final String SYNC_FREQUENCY_SIX_HOURS = "sync_six_hours";
    public static final String SYNC_FREQUENCY_ONE_DAY = "sync_one_day";
    private static final String TAG = "SynchronizingService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
    public SynchronizingService()
    {
        super("SynchronizingService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        GlobalState globalState = ((GlobalState)getApplication());
        assert globalState != null;
        if(globalState.isSyncRunning()) return;
        try
        {
            Log.d(TAG, "--> processing the intent");
            int mode = intent.getIntExtra(SYNCHRONIZATION_TYPE, MSG_SYNCHRONIZE);

            globalState.setSyncRunning(true);
            SharedPreferences preferences = globalState.getPreferences();
            long lastUpdate = preferences.getLong("last_update", 0L);
            int period = getDownloadPeriod(this, preferences );

            if(     globalState.isUserLogged() &&
                    (   mode != MSG_SYNCHRONIZE ||
                        period > 0 &&
                        (System.currentTimeMillis() - PERIOD_TOLERANCE - lastUpdate) > period*60*1000))
            {
                preferences.edit().putLong( "last_update", System.currentTimeMillis() );

                switch (mode)
                {
                    case MSG_SYNCHRONIZE:
                    case MSG_SYNCHRONIZE_MANUAL:
                        Log.d(TAG, "--> Synchronization started");
                        globalState.getZoteroSync().fullSync();
                        Log.d(TAG, "<-- Synchronization ended");
                        break;

                    case MSG_FULL_SYNCHRONIZE_MANUAL:
                        Log.d(TAG, "--> Full synchronization started");
                        globalState.getStorage().deleteData();
                        globalState.getZoteroSync().fullSync();
                        Log.d(TAG, "<-- Full synchronization ended");
                        break;
                }
            }
            Log.d(TAG, "<-- processing the intent stopped");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            globalState.setSyncRunning(false);
        }
    }

    private int getDownloadPeriod(Context context, SharedPreferences preferences )
    {
        String frequency = preferences.getString("sync_frequency", "sync_one_hour");

        if(SYNC_FREQUENCY_NEVER.equals(frequency))
        {
            return 0;
        }

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if(!isConnected) return 0;

        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if(!isWiFi)
        {
            boolean mobileDataAllowed = preferences.getBoolean("mobile_sync",false);
            if(!mobileDataAllowed) return 0;
        }

        if(SYNC_FREQUENCY_CHARGING.equals(frequency))
        {
            IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, batteryFilter);
            assert batteryStatus != null;
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

            if(!isCharging) return 0;
            else return 60;
        }

        if(SYNC_FREQUENCY_FIFTEEN_MINUTES.equals(frequency)) return 15;
        if(SYNC_FREQUENCY_ONE_HOUR.equals(frequency)) return 60;
        if(SYNC_FREQUENCY_SIX_HOURS.equals(frequency)) return 360;
        if(SYNC_FREQUENCY_ONE_DAY.equals(frequency)) return 24*60;
        return 60;
    }

    static  void ExecuteInBackground(@NotNull final Context context)
    {
        new AsyncTask<Void, Void, Integer>(){

            @Override
            protected Integer doInBackground(Void... arg0)
            {
                Thread.currentThread().setName("Synchronizing Service");
                Intent serviceStartIntent;
                serviceStartIntent = new Intent(context, SynchronizingService.class);
                serviceStartIntent.putExtra(
                        SynchronizingService.SYNCHRONIZATION_TYPE,
                        SynchronizingService.MSG_SYNCHRONIZE_MANUAL);
                context.startService(serviceStartIntent);
                return 0;
            }

            @Override
            protected void onPostExecute(Integer result)
            {
                if( !result.equals( 0))
                {
                    Toast.makeText(
                            context,
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}

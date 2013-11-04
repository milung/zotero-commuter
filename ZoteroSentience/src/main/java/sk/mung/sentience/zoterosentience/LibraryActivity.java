package sk.mung.sentience.zoterosentience;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import sk.mung.sentience.zoterosentience.navigation.ActivityWithDrawer;
import sk.mung.sentience.zoterosentience.navigation.DrawerFragment;
import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;

/**
 * An activity representing a list of LibraryItems. This activity has different
 * presentations for handset and tablet-size devices.  On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link sk.mung.sentience.zoterosentience.navigation.DrawerFragment} and the item details (if present) is a
 * {@link ItemListFragment}.
 * <p>
 * This activity also implements the required
 * {@link sk.mung.sentience.zoterosentience.navigation.DrawerFragment.Callbacks} interface to listen for item
 * selections.
 */
public class LibraryActivity extends ActivityWithDrawer
        implements DrawerFragment.Callbacks, ItemListFragment.Callback
{
    public static final int SYNC_CHECK_PERIOD = 150;
    private MenuItem refreshActionImage;
    private Timer timer = new Timer();

    @Override
    protected int getContentLayoutId()
    {
        return R.layout.activity_library;
    }

    @Override
    protected Fragment createInitialFragment()
    {
        return createCollectionFragment(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
        assert rotation != null;
        rotation.setRepeatCount(Animation.INFINITE);
        BootSchedulerReceiver.scheduleSynchronizing(this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(timer != null)
        {
            timer.purge();
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new SpinnerTask(this), 0, SYNC_CHECK_PERIOD);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.purge();
        timer.cancel();
        timer = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        refreshActionImage = menu.findItem(R.id.refresh);
        return true;
    }

    @Override
    public void onAllItemsSelected()
    {
        navigateTo(createCollectionFragment(0), false);
    }

    @Override
    public void onLoginToZotero()
    {
        navigateTo(new LoginFragment(),true);
    }

    @Override
    public void onNavigateTo(Fragment fragment, boolean putBackState) {
        navigateTo( fragment, putBackState);
    }

    @Override
    public void onSettingsSelected()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private Fragment createCollectionFragment(long collectionKey)
    {
        Bundle arguments = new Bundle();
        arguments.putLong(ItemListFragment.ARG_COLLECTION_KEY, collectionKey);
        Fragment fragment = new ItemListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onItemSelected(int position, Item item, long collectionKey)
    {
        Bundle arguments = new Bundle();
        arguments.putLong(ItemPager.ARG_COLLECTION_ID, collectionKey);
        arguments.putInt(ItemPager.ARG_CURRENT_POSITION, position);

        CollectionEntity entity = ((GlobalState)getApplication()).getStorage().findCollectionById(collectionKey);
        arguments.putString(ItemPager.ARG_COLLECTION_NAME, entity.getName());
        arguments.putInt(ItemPager.ARG_ITEMS_COUNT, entity.getItemsCount());

        ItemPager pager = new ItemPager();
        pager.setArguments(arguments);
        navigateTo(pager,true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.refresh:
                onRefreshOptionSelected(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onRefreshOptionSelected(@SuppressWarnings("UnusedParameters") MenuItem menuItem)
    {
        final Context context = this;
        new AsyncTask<Void, Void, Integer>(){

            @Override
            protected Integer doInBackground(Void... arg0)
            {
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

    private class SpinnerTask extends TimerTask
    {
        private final Activity activity;

        SpinnerTask(Activity activity)
        {
            this.activity = activity;
        }
        boolean isSpinning = false;
        @Override
        public void run()
        {
            GlobalState state = (GlobalState)getApplication();
            if(refreshActionImage != null )
            {
                if( state.isSyncRunning())
                {
                    // spin
                    if(!isSpinning)
                    {
                        isSpinning = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshActionImage.setIcon(R.drawable.progress_medium_holo);
                                LayerDrawable layerDrawable
                                        = (LayerDrawable)refreshActionImage.getIcon();
                                assert layerDrawable != null;
                                ((Animatable)layerDrawable.getDrawable(0)).start();
                                ((Animatable)layerDrawable.getDrawable(1)).start();
                            }
                        });
                    }
                }
                else
                {
                    // stop
                    if(isSpinning)
                    {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshActionImage.setIcon(R.drawable.ic_action_refresh);
                            }
                        });
                    }
                }
            }
        }
    }
}

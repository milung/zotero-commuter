package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.ZoteroSync;

/**
 * An activity representing a list of LibraryItems. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemListlActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link LibraryFragment} and the item details (if present) is a
 * {@link ItemListFragment}.
 * <p>
 * This activity also implements the required
 * {@link LibraryFragment.Callbacks} interface to listen for item
 * selections.
 */
public class LibraryActivity extends FragmentActivity
        implements LibraryFragment.Callbacks, ItemListFragment.Callback
{

    public static final String LIBRARY_ACTIVITY_DETAILS_MODE = "libraryActivity.detailsMode";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private long collectionId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        BootSchedulerReceiver.scheduleSynchronizing(this,false);
        setContentView(R.layout.activity_library);

        if (findViewById(R.id.library_itemlist_container) != null)
        {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            FragmentManager fragmentManager = getSupportFragmentManager();
            ((LibraryFragment) fragmentManager
                    .findFragmentById(R.id.library_collection_list))
                    .setActivateOnItemClick(true);


            boolean isDetailMode = false;
            if(savedInstanceState != null)
            {
                isDetailMode = savedInstanceState.getBoolean(LIBRARY_ACTIVITY_DETAILS_MODE,false);
            }
            else
            {
                onCollectionSelected(0);
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if(isDetailMode)
            {
                transaction.hide(getSupportFragmentManager().findFragmentById(R.id.library_collection_list));
            }
            else
            {
                transaction.hide(getSupportFragmentManager().findFragmentById(R.id.library_itemviewer));
            }
            transaction.commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }


    /**
     * Callback method from {@link LibraryFragment.Callbacks} indicating
     * that the item with the given ID was selected.
     */
    @Override
    public void onCollectionSelected(long id)
    {
        collectionId=id;
        if (mTwoPane)
        {
            Bundle arguments = new Bundle();
            arguments.putLong(ItemListFragment.ARG_COLLECTION_KEY, id);
            ItemListFragment fragment = new ItemListFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.library_itemlist_container, fragment)
                    .commit();
        }
        else
        {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ItemListlActivity.class);
            detailIntent.putExtra(ItemListFragment.ARG_COLLECTION_KEY, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onItemSelected(int position, Item item)
    {
        Intent detailIntent = new Intent(this, ItemPagerActivity.class);
        detailIntent.putExtra(ItemPager.ARG_COLLECTION_ID, collectionId);
        detailIntent.putExtra(ItemPager.ARG_CURRENT_POSITION, position);

        CollectionEntity entity = ((GlobalState)getApplication()).getStorage().findCollectionById(collectionId);
        detailIntent.putExtra(ItemPager.ARG_COLLECTION_NAME, entity.getName());
        detailIntent.putExtra(ItemPager.ARG_ITEMS_COUNT, entity.getItemsCount());
        startActivity(detailIntent);
    }

    public void onLoginOptionSelected( MenuItem menuItem)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void onSettingsOptionSelected( MenuItem menuItem)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onWipeDownloadsOptionSelected(MenuItem menuItem)
    {
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS );
        assert dir != null;
        //noinspection ResultOfMethodCallIgnored
        deleteDirectory(dir);
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

    public void onRefreshOptionSelected(MenuItem menuItem)
    {
        final Context context = this;
        new AsyncTask<Void, Void, Integer>(){

            @Override
            protected Integer doInBackground(Void... arg0)
            {
                try
                {
                    ZoteroSync zoteroSync = ((GlobalState) getApplication()).getZoteroSync();
                    zoteroSync.fullSync();
                    return 0;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return 1;
                }
                catch (XmlPullParserException e)
                {
                    e.printStackTrace();
                    return 2;
                } catch (URISyntaxException e)
                {
                    e.printStackTrace();
                    return 3;
                }
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

    public void onResetVersionsOptionSelected(final MenuItem menuItem)
    {
        final Context context = this;
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... arg0)
            {
                ((GlobalState)getApplication()).getStorage().deleteData();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                onRefreshOptionSelected(menuItem);
            }
        }.execute();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }
}

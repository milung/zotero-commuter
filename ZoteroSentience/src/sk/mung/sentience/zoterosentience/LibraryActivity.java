package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoterosentience.storage.ZoteroSync;

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

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
            ((LibraryFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.library_collection_list))
                    .setActivateOnItemClick(true);

            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(getSupportFragmentManager().findFragmentById(R.id.library_itemviewer))
                    .commit();
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
        if (mTwoPane)
        {
            Bundle arguments = new Bundle();
            arguments.putLong(ItemListFragment.ARG_COLLECTION_KEY, id);
            ItemListFragment fragment = new ItemListFragment();
            fragment.setArguments(arguments);

            ItemPager pager = (ItemPager) getSupportFragmentManager().findFragmentById(R.id.library_itemviewer);
            if(pager != null)
            {
                pager.setCollectionId(id);
            }

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
    public void onItemSelected(Item item)
    {
        if (mTwoPane)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            ((ItemPager)fragmentManager.findFragmentById(R.id.library_itemviewer)).setPosition(item);
            fragmentManager.beginTransaction()
                .hide(fragmentManager.findFragmentById(R.id.library_collection_list))
                .show(fragmentManager.findFragmentById(R.id.library_itemviewer))
                .addToBackStack(null)
            .commit();
        }
        else
        {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            // Intent detailIntent = new Intent(this, ItemListlActivity.class);
            // detailIntent.putExtra(ItemListFragment.ARG_COLLECTION_KEY, id);
            // startActivity(detailIntent);
        }
    }

    public void onLoginOptionSelected( MenuItem menuItem)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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
                    zoteroSync.syncCollections();
                    zoteroSync.syncDeletions();
                    zoteroSync.syncItems();
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
}

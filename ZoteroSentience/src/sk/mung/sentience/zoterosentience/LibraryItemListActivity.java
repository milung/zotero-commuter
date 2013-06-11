package sk.mung.sentience.zoterosentience;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import sk.mung.sentience.zoterosentience.storage.ZoteroSync;

/**
 * An activity representing a list of LibraryItems. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link LibraryItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link LibraryItemListFragment} and the item details (if present) is a
 * {@link LibraryItemDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link LibraryItemListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class LibraryItemListActivity extends FragmentActivity
        implements LibraryItemListFragment.Callbacks
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
        setContentView(R.layout.activity_libraryitem_list);

        if (findViewById(R.id.libraryitem_detail_container) != null)
        {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((LibraryItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.libraryitem_list))
                    .setActivateOnItemClick(true);
        }

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    /**
     * Callback method from {@link LibraryItemListFragment.Callbacks} indicating
     * that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(long id)
    {
        if (mTwoPane)
        {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(LibraryItemDetailFragment.ARG_COLLECTION_KEY, id);
            LibraryItemDetailFragment fragment = new LibraryItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.libraryitem_detail_container, fragment)
                    .commit();

        }
        else
        {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, LibraryItemDetailActivity.class);
            detailIntent.putExtra(LibraryItemDetailFragment.ARG_COLLECTION_KEY, id);
            startActivity(detailIntent);
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

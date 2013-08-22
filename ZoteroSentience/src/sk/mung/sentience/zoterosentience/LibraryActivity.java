package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.File;

import sk.mung.sentience.zoterosentience.navigation.ActivityWithDrawer;
import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;

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
public class LibraryActivity extends ActivityWithDrawer
        implements LibraryFragment.Callbacks, ItemListFragment.Callback
{

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
        BootSchedulerReceiver.scheduleSynchronizing(this, false);
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
    public void onCollectionSelected(final long id)
    {
        navigateTo(createCollectionFragment(id), false);
    }

    @Override
    public void onAllItemsSelected()
    {
        navigateTo(createCollectionFragment(0), false);
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

}

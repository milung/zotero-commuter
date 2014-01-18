package sk.mung.sentience.zoterocommuter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoterocommuter.provider.ZoteroContract;
import sk.mung.sentience.zoterocommuter.storage.ItemsLoader;
import sk.mung.sentience.zoterocommuter.storage.ReadingQueueLoader;
import sk.mung.sentience.zoterocommuter.storage.ZoteroStorageImpl;
import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;

/**
 * A fragment representing a single LibraryItem detail screen. This fragment is
 * either contained in a {@link sk.mung.sentience.zoterocommuter.MainActivity} in two-pane mode (on
 * tablets)
 */
public class ItemListFragment
        extends ItemListFragmentBase
{

    private int itemsCount = -1;
    private boolean isReadingQueue = false;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle bundle;
        if(savedInstanceState != null)  bundle = savedInstanceState; // 1
        else if(getArguments() != null) bundle = getArguments();     // 2
        else                            bundle = getActivity().getIntent().getExtras(); // 3
        isReadingQueue = bundle != null && bundle.getBoolean(ARG_IS_READING_QUEUE, false);
    }

    @Override
    protected String getActionTitle()
    {
        if(isReadingQueue)
        {
            return getString(R.string.navigation_reading_queue);
        }
        else return super.getActionTitle();
    }

    @Override
    protected String getActionSubtitle()
    {
        if(itemsCount < 0) return "";
        else return getResources().getQuantityString(R.plurals.number_of_items, itemsCount, itemsCount);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if(isReadingQueue)
        {
            ReadingQueueLoader loader
                    = new ReadingQueueLoader(this.getActivity(), getGlobalState().getStorage());
            loader.setUpdateThrottle(3000);
            return loader;
        }
        else return super.onCreateLoader(id,args);

    }

    @Override
    public Fragment createPager(int position)
    {
        if(isReadingQueue)
        {
            Bundle arguments = new Bundle();
            arguments.putInt(ItemPager.ARG_CURRENT_POSITION, position);
            arguments.putString(ItemPager.ARG_COLLECTION_NAME, getString(R.string.navigation_reading_queue));

            ReadingQueuePager pager = new ReadingQueuePager();
            pager.setArguments(arguments);
            return pager;
        }
        else return super.createPager(position);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
        {

            private List<Long> selectedIds = new ArrayList<Long>();
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked)
            {
                if(checked)
                {
                    selectedIds.add(id);
                }
                else
                {
                    selectedIds.remove(id);
                }
                String title = getResources().getQuantityString(R.plurals.count_items_selected, selectedIds.size(),selectedIds.size());

                mode.setTitle(title);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId())
                {
                    case R.id.item_copy:
                        copySelectedItems(selectedIds);
                        selectedIds.clear();
                        mode.finish();
                        return true;
                    case R.id.item_cut:
                        cutSelectedItems(selectedIds); // cut must go before copy due to invalidate options menu
                        copySelectedItems(selectedIds);
                        selectedIds.clear();
                        mode.finish();
                        return true;
                    case R.id.item_remove_local:
                        removeLocalItems(selectedIds);
                        selectedIds.clear();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextual_item_list_menu, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                MenuItem cutItem = menu.findItem(R.id.item_cut);
                long collectionId = getCollectionId();
                assert cutItem != null;
                cutItem.setVisible(collectionId > 0);
                return true;
            }
        });
    }

    private void removeLocalItems(List<Long> selectedIds)
    {
        AsyncTask<List<Long>, Void, Void> task = new AsyncTask<List<Long>, Void, Void>()
        {
            @Override
            protected Void doInBackground(List<Long>... params)
            {
                ZoteroStorageImpl storage = getGlobalState().getStorage();
                storage.removeItemsLocalVersion(params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                getGlobalState().getStorage().invalidateItems();
            }
        };
        task.execute(new ArrayList<Long>(selectedIds));
    }

    private void cutSelectedItems(List<Long> selectedIds)
    {
        AsyncTask<List<Long>,Void, Void> task = new AsyncTask<List<Long>, Void, Void>()
        {
            @Override
            protected Void doInBackground(List<Long>... params)
            {
                ZoteroStorageImpl storage = getGlobalState().getStorage();
                CollectionEntity collection = storage.findCollectionById(getCollectionId());
                storage.removeItemsFromCollection(params[0],collection);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid)
            {
                getGlobalState().getStorage().invalidateItems();
            }
        };
        task.execute(new ArrayList<Long>(selectedIds));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.item_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        // handle menuItem selection
        switch (menuItem.getItemId()) {
            case R.id.paste:
                pasteItems();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void pasteItems()
    {
        List<Uri> uris = getPastedUris();
        AsyncTask<List<Uri>,Void,Void> pasteTask = new AsyncTask<List<Uri>, Void, Void>()
        {
            @Override
            protected Void doInBackground(List<Uri>... uris)
            {
                List<Long> itemIds = convertUrisToItemIds(uris[0]);
                ZoteroStorageImpl storage = getGlobalState().getStorage();
                CollectionEntity collection = storage.findCollectionById(getCollectionId());
                storage.addItemsToCollection(itemIds, collection);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                getGlobalState().getStorage().invalidateItems();
            }
        };
        pasteTask.execute(uris);

    }

    private List<Long> convertUrisToItemIds(List<Uri> uris)
    {
        List<Long> itemIds = new ArrayList<Long>();
        ContentResolver cr = getActivity().getContentResolver();
        for(Uri pasteUri: uris)
        {
            // Is this a content URI?
            String uriMimeType = cr.getType(pasteUri);

            // If the return value is not null, the Uri is a content Uri
            if (uriMimeType != null && uriMimeType.equals(ZoteroContract.MIMETYPE_ITEMS_ITEM) )
            {
                // Get the data from the content provider.
                Cursor pasteCursor = cr.query(pasteUri, null, null, null, null);

                // If the Cursor contains data, move to the first record
                if (pasteCursor != null && pasteCursor.moveToFirst())
                {
                    int columnIx = pasteCursor.getColumnIndex("_id");
                    long id = pasteCursor.getLong(columnIx);
                    itemIds.add(id);
                }

                // close the Cursor
                pasteCursor.close();
            }
        }
        return itemIds;
    }

    private List<Uri> getPastedUris()
    {
        ClipboardManager clipboard = (ClipboardManager)
                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        List<Uri> uris = new ArrayList<Uri>();

        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null)
        {
            for(int ix = 0; ix < clip.getItemCount(); ++ix)
            {
                // Gets the first item from the clipboard data
                ClipData.Item item = clip.getItemAt(ix);

                // Tries to get the item's contents as a URI
                Uri pasteUri = item.getUri();
                if (pasteUri != null)
                {
                    uris.add(pasteUri);
                }
            }
        }
        return uris;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        MenuItem pasteItem = menu.findItem(R.id.paste);
        ClipboardManager clipboard = (ClipboardManager)
                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        Long collectionId = getCollectionId();

        if(collectionId!= null && collectionId > 0 && pasteItem != null && clipboard.hasPrimaryClip())
        {
            boolean isItemMime = clipboard.getPrimaryClipDescription()
                    .hasMimeType(ZoteroContract.MIMETYPE_ITEMS_ITEM);
            pasteItem.setVisible(isItemMime);
        }
        else if(pasteItem != null) pasteItem.setVisible(false);
    }

    private void copySelectedItems(List<Long> selectedIds)
    {
        ClipboardManager clipboard = (ClipboardManager)
                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clipData = null;
        for(Long id : selectedIds)
        {
            if(clipData == null)
            {
                clipData = ClipData.newUri(
                    getActivity().getContentResolver(),
                    getString(R.string.clipdata_item),
                    ZoteroContract.getItemUri(id));
            }
            else
                clipData.addItem(new ClipData.Item(ZoteroContract.getItemUri(id)));
        }
        clipboard.setPrimaryClip(clipData);
        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        itemsCount = cursor.getCount();
        super.onLoadFinished(loader, cursor);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_IS_READING_QUEUE, isReadingQueue);
    }
}

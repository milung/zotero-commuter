package sk.mung.sentience.zoterocommuter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoterocommuter.provider.ZoteroContract;
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
                        copySelectedItems(selectedIds);
                        cutSelectedItems(selectedIds);
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

    private void cutSelectedItems(List<Long> selectedIds)
    {
        ZoteroStorageImpl storage = getGlobalState().getStorage();
        CollectionEntity collection = storage.findCollectionById(getCollectionId());
        storage.removeItemsFromCollection(selectedIds,collection);
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

        pasteItem.setVisible(false);
        if(getCollectionId() > 0 && pasteItem != null && clipboard.hasPrimaryClip())
        {
            boolean isItemMime = clipboard.getPrimaryClipDescription()
                    .hasMimeType(ZoteroContract.MIMETYPE_ITEMS_ITEM);
            pasteItem.setVisible(isItemMime);
        }
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
}

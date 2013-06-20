package sk.mung.sentience.zoterosentience;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoterosentience.storage.ItemsLoader;

/**
 * A fragment representing a single LibraryItem detail screen. This fragment is
 * either contained in a {@link LibraryActivity} in two-pane mode (on
 * tablets) or a {@link ItemListlActivity} on handsets.
 */
public class ItemListFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Item>>,
        ListView.OnItemClickListener
{
    public static final String ARG_COLLECTION_KEY = "collection_key" ;
    private Long collectionKey;
    private ItemListAdapter listAdapter;
    private Callback callback;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        Item item = (Item) listAdapter.getItem(position);
        callback.onItemSelected(item);
    }

    public interface Callback
    {
        public void onItemSelected(Item item);
    }

    private static Callback dummyCallback
            = new Callback()
    {
        @Override
        public void onItemSelected(Item item)
        {}
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment()
    {}

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callback)) { throw new IllegalStateException(
                "Activity must implement fragment's callbacks."); }

        callback = (Callback) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        callback = dummyCallback;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        listAdapter = new ItemListAdapter(getActivity(), R.layout.listitem_item);
        ListView listView = (ListView) getActivity().findViewById(R.id.library_itemlist);
        listView.setAdapter(listAdapter);
        listView.setSaveEnabled(true);
        listView.setOnItemClickListener(this);

        getLoaderManager().restartLoader(1, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public Loader<List<Item>> onCreateLoader(int id, Bundle args)
    {
        Long collectionId =null;
        if (getArguments().containsKey(ARG_COLLECTION_KEY))
        {
            collectionId = getArguments().getLong(ARG_COLLECTION_KEY);
        }

        ItemsLoader loader
                = new ItemsLoader(this.getActivity(), collectionId, getGlobalState().getStorage());
        loader.setUpdateThrottle(3000);
        return loader;
    }

    private GlobalState getGlobalState()
    {
        return (GlobalState) getActivity().getApplication();
    }

    @Override
    public void onLoadFinished(Loader<List<Item>> loader, List<Item> items)
    {
        listAdapter.setItems(items);
    }

    @Override
    public void onLoaderReset(Loader<List<Item>> loader)
    {
        listAdapter.setItems(null);
    }
}

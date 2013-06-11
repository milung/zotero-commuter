package sk.mung.sentience.zoterosentience;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import sk.mung.sentience.zoteroapi.items.Item;
import sk.mung.sentience.zoterosentience.storage.ItemsLoader;

/**
 * A fragment representing a single LibraryItem detail screen. This fragment is
 * either contained in a {@link LibraryItemListActivity} in two-pane mode (on
 * tablets) or a {@link LibraryItemDetailActivity} on handsets.
 */
public class LibraryItemDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Item>>
{
    public static final String ARG_COLLECTION_KEY = "collection_key" ;
    private Long collectionKey;
    private ItemListAdapter listAdapter;
    ListView listView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LibraryItemDetailFragment()
    {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        listAdapter = new ItemListAdapter(getActivity());
        listView = (ListView) getActivity().findViewById(R.id.libraryitem_detail);
        listView.setAdapter(listAdapter);
        listView.setSaveEnabled(true);

        if (getArguments().containsKey(ARG_COLLECTION_KEY))
        {
            collectionKey =getArguments().getLong(ARG_COLLECTION_KEY);
        }
        else collectionKey = null;

        getLoaderManager().restartLoader(1, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_libraryitem_detail, container, false);

        return rootView;
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

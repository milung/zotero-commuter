package sk.mung.sentience.zoterosentience;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import sk.mung.sentience.zoterocommuter.R;
import sk.mung.sentience.zoterosentience.storage.ItemsLoader;
import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;

/**
 * A fragment representing a single LibraryItem detail screen. This fragment is
 * either contained in a {@link MainActivity} in two-pane mode (on
 * tablets)
 */
public class ItemListFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ListView.OnItemClickListener
{
    public static final String ARG_COLLECTION_KEY = "collection_key" ;
    private static final String ITEM_LIST_POSITION = "item_list_position";
    private Long collectionKey;
    private ItemListAdapter listAdapter;
    private Callback callback;
    private int position=-1;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        Item item = (Item) view.getTag(R.id.tag_item);
        callback.onItemSelected(position, item, collectionKey);
        this.position = position;
    }

    private void scrollToPosition(int position)
    {
        ListView listView = (ListView) getActivity().findViewById(R.id.library_itemlist);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
        {
            listView.smoothScrollToPosition(position);
        }
        this.position = position;
    }

    private String getActionTitle()
    {
        if(collectionKey <= 0)
        {
            return getResources().getString(R.string.all_items);
        }
        CollectionEntity entity = getGlobalState().getStorage().findCollectionById(collectionKey);
        return entity.getName();
    }

    private String getActionSubtitle()
    {
        CollectionEntity entity = getGlobalState().getStorage().findCollectionById(collectionKey);
        return getResources().getQuantityString(R.plurals.number_of_items, entity.getItemsCount(), entity.getItemsCount());
    }

    public interface Callback
    {
        public void onItemSelected(int position, Item item, long collectionKey);
    }

    private static Callback dummyCallback
            = new Callback()
    {
        @Override
        public void onItemSelected(int position, Item item, long collectionKey)
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
        listAdapter = new ItemListAdapter(getActivity(), R.layout.listitem_item, getGlobalState().getStorage());
        ListView listView = (ListView) getActivity().findViewById(R.id.library_itemlist);
        listView.setAdapter(listAdapter);
        listView.setSaveEnabled(true);
        listView.setOnItemClickListener(this);
        Bundle bundle;
        if(savedInstanceState != null)  bundle = savedInstanceState; // 1
        else if(getArguments() != null) bundle = getArguments();     // 2
        else                            bundle = getActivity().getIntent().getExtras(); // 3

        if(bundle != null)
        {
            position = bundle.getInt(ITEM_LIST_POSITION, -1);
        }

        getLoaderManager().restartLoader(R.id.loader_item_list, bundle, this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {

        if (args != null && args.containsKey(ARG_COLLECTION_KEY))
        {
            collectionKey = args.getLong(ARG_COLLECTION_KEY);
        }
        else
        {
            collectionKey = 0L;
        }

        ItemsLoader loader
                = new ItemsLoader(this.getActivity(), collectionKey, getGlobalState().getStorage());
        loader.setUpdateThrottle(3000);
        return loader;
    }

    private GlobalState getGlobalState()
    {
        return (GlobalState) getActivity().getApplication();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        Cursor old = listAdapter.swapCursor(cursor);
        if(old != null) old.close();
        if(position!=-1)
        {
            scrollToPosition(position);
        }
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getActionTitle());
        actionBar.setSubtitle(getActionSubtitle());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        Cursor old = listAdapter.swapCursor(null);
        if(old != null) old.close();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        //super.onSaveInstanceState(outState);
        outState.putInt(ITEM_LIST_POSITION,position);
        if(collectionKey!=null)
        {
            outState.putLong(ARG_COLLECTION_KEY, collectionKey);
        }
    }
}

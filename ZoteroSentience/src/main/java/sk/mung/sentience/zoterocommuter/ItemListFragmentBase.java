package sk.mung.sentience.zoterocommuter;


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

import sk.mung.sentience.zoterocommuter.navigation.ActivityWithDrawer;
import sk.mung.sentience.zoterocommuter.storage.ItemsLoader;
import sk.mung.zoteroapi.entities.CollectionEntity;


class ItemListFragmentBase extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,ListView.OnItemClickListener
{
    public static final String ARG_COLLECTION_KEY = "collection_key" ;
    private static final String ITEM_LIST_POSITION = "item_list_position";
    public static final String ARG_IS_READING_QUEUE = "IS_READING_QUEUE";


    protected final Long getCollectionId()
    {
        return collectionId;
    }

    private Long collectionId =0L;
    private ItemListAdapter listAdapter;
    private int position=-1;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        ActivityWithDrawer drawer = (ActivityWithDrawer) getActivity();
        Fragment pager = createPager(position);
        drawer.navigateTo(pager, true);
        this.position = position;
    }


    public Fragment createPager(int position)
    {
        Bundle arguments = new Bundle();
        arguments.putLong(ItemPager.ARG_COLLECTION_ID, collectionId);
        arguments.putInt(ItemPager.ARG_CURRENT_POSITION, position);

        CollectionEntity entity = getGlobalState().getStorage().findCollectionById(collectionId);
        arguments.putString(ItemPager.ARG_COLLECTION_NAME, entity.getName());
        arguments.putInt(ItemPager.ARG_ITEMS_COUNT, entity.getItemsCount());

        ItemPager pager = new ItemPager();
        pager.setArguments(arguments);
        return pager;
    }

    private void scrollToPosition(int position)
    {
        ListView listView = getListView();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
        {
            listView.smoothScrollToPosition(position);
        }
        this.position = position;
    }

    protected String getActionTitle()
    {
        if(collectionId == null || collectionId <= 0)
        {
            return getResources().getString(R.string.all_items);
        }
        try
        {
            CollectionEntity entity = getGlobalState().getStorage().findCollectionById(collectionId);
            return entity.getName();
        }
        catch(IllegalArgumentException ex) //e.g if db was cleared
        {
            collectionId = 0L;
            return getResources().getString(R.string.all_items);
        }
    }

    protected String getActionSubtitle()
    {
        int count = 0;
        try
        {
            CollectionEntity entity = getGlobalState().getStorage().findCollectionById(collectionId);
            count = entity.getItemsCount();
        }
        catch (IllegalArgumentException ex) // eg. if db is cleaned
        {
            count = 0;
        }
        return getResources().getQuantityString(R.plurals.number_of_items, count , count);
    }


    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        onCreate(savedInstanceState);
        listAdapter = new ItemListAdapter(getActivity(), R.layout.listitem_item, getGlobalState().getStorage());
        ListView listView = getListView();
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

    protected ListView getListView() {
        return (ListView) getActivity().findViewById(R.id.library_itemlist);
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
            collectionId = args.getLong(ARG_COLLECTION_KEY);
        }
        else
        {
            collectionId = 0L;
        }

        ItemsLoader loader
                = new ItemsLoader(this.getActivity(), collectionId, getGlobalState().getStorage());
        return loader;
    }

    protected final GlobalState getGlobalState()
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
        outState.putInt(ITEM_LIST_POSITION,position);
        if(collectionId !=null)
        {
            outState.putLong(ARG_COLLECTION_KEY, collectionId);
        }
    }
}

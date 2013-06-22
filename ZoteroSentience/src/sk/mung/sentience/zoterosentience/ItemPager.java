package sk.mung.sentience.zoterosentience;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoterosentience.storage.ItemsLoader;

public class ItemPager extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Item>>,
            ViewPager.OnPageChangeListener
{

    public static final String ITEM_PAGER_COLLECTION_ID = "itemPager.collectionId";
    public static final String ITEM_PAGER_POSITION = "itemPager.position";

    interface Callback
    {
        public void onItemScrolled(int position, Item selectedItem);
    }

    private Callback dummyCallback = new Callback()
    {
        @Override
        public void onItemScrolled(int position, Item selectedItem)
        {

        }
    };

    private Callback callback = dummyCallback;

    private List<Item> items = new ArrayList<Item>();

    private GlobalState getGlobalState()
    {
        return (GlobalState) getActivity().getApplication();
    }

    private Long collectionId;
    private int positionId;


    void setCallback(Callback callback)
    {
        if(callback == null)
        {
            this.callback = dummyCallback;
        }
        else this.callback = callback;
    }

    public void setCollectionId( Long collectionId)
    {
        ((ViewPager)getActivity().findViewById(R.id.pager))
                .setAdapter(new ItemPagerAdapter(new ArrayList<Item>(), getActivity().getSupportFragmentManager()));
        this.collectionId = collectionId;
        Bundle bundle = new Bundle();
        bundle.putLong(ItemListFragment.ARG_COLLECTION_KEY,collectionId);

        getLoaderManager().restartLoader(2, bundle, this);
    }

    public void setPosition(Item item)
    {
        int position = -1;
        for( int ix = 0 ; ix < items.size(); ix ++ )
        {
            if(items.get(ix).getId() == item.getId())
            {
                position = ix;
                break;
            }
        }
        if(position >=0 )
        {
            ViewPager pager = ((ViewPager)getActivity().findViewById(R.id.pager));
            pager.setCurrentItem(position, true);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ViewPager pager = ((ViewPager)getActivity().findViewById(R.id.pager));
        pager.setAdapter(new ItemPagerAdapter(
                new ArrayList<Item>(),
                getActivity().getSupportFragmentManager()));
        pager.setOnPageChangeListener(this);
        Bundle bundle;
        if(savedInstanceState != null)  bundle = savedInstanceState; // 1
        else if(getArguments() != null) bundle = getArguments();     // 2
        else                            bundle = getActivity().getIntent().getExtras(); // 3

        if(bundle != null)
        {
            collectionId = bundle.getLong(ITEM_PAGER_COLLECTION_ID, 0);
            positionId = bundle.getInt(ITEM_PAGER_POSITION, 0);
        }

        getLoaderManager().restartLoader(2, null, this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_pager, container, false);
    }

    @Override
    public Loader<List<Item>> onCreateLoader(int id, Bundle args)
    {
        ItemsLoader loader
                = new ItemsLoader(this.getActivity(), this.collectionId, getGlobalState().getStorage());
        loader.setUpdateThrottle(3000);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<Item>> loader, List<Item> items)
    {
        this.items = items;
        ViewPager pager = ((ViewPager)getActivity().findViewById(R.id.pager));
        pager.setAdapter(new ItemPagerAdapter(
                items, getActivity().getSupportFragmentManager()));
        pager.setCurrentItem(positionId,true);

    }

    @Override
    public void onLoaderReset(Loader<List<Item>> loader)
    {
        ((ViewPager)getActivity().findViewById(R.id.pager))
                .setAdapter(new ItemPagerAdapter(
                        new ArrayList<Item>(),
                        getActivity().getSupportFragmentManager()));

    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {}

    @Override
    public void onPageSelected(int position)
    {
        callback.onItemScrolled(position, items.get(position));
    }

    @Override
    public void onPageScrollStateChanged(int i) {}

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(ITEM_PAGER_COLLECTION_ID, collectionId == null ? 0 : collectionId);
        outState.putInt(ITEM_PAGER_POSITION,
                ((ViewPager)getActivity().findViewById(R.id.pager)).getCurrentItem());
    }
}

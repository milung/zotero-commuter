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

/**
 * Created by sk1u00e5 on 17.6.2013.
 */
public class ItemPager extends Fragment implements LoaderManager.LoaderCallbacks<List<Item>>
{
    private List<Item> items = new ArrayList<Item>();

    public void setCollectionId( Long collectionId)
    {
        ((ViewPager)getActivity().findViewById(R.id.pager))
                .setAdapter(new ItemPagerAdapter(new ArrayList<Item>(), getActivity().getSupportFragmentManager()));

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
            ((ViewPager)getActivity().findViewById(R.id.pager)).setCurrentItem(position, true);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_pager, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ((ViewPager)getActivity().findViewById(R.id.pager))
                .setAdapter(new ItemPagerAdapter(new ArrayList<Item>(), getActivity().getSupportFragmentManager()));

        getLoaderManager().restartLoader(2, getArguments(), this);
    }

    @Override
    public Loader<List<Item>> onCreateLoader(int id, Bundle args)
    {
        Long collectionId =null;
        if (args != null && args.containsKey(ItemListFragment.ARG_COLLECTION_KEY))
        {
            collectionId = args.getLong(ItemListFragment.ARG_COLLECTION_KEY);
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
        ((ViewPager)getActivity().findViewById(R.id.pager))
                .setAdapter(new ItemPagerAdapter(items, getActivity().getSupportFragmentManager()));

    }

    @Override
    public void onLoaderReset(Loader<List<Item>> loader)
    {
        ((ViewPager)getActivity().findViewById(R.id.pager))
                .setAdapter(new ItemPagerAdapter(new ArrayList<Item>(), getActivity().getSupportFragmentManager()));

    }
}

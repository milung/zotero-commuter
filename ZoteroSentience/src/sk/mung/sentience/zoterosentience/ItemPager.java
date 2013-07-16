package sk.mung.sentience.zoterosentience;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.mung.sentience.zoterosentience.storage.ItemsLoader;

public class ItemPager extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
            ViewPager.OnPageChangeListener
{

    public static final String ARG_COLLECTION_ID = "collection_id";
    public static final String ARG_CURRENT_POSITION = "item_position";
    private static final String TAG = "ItemPager";
    public static final String ARG_COLLECTION_NAME = "collection_name";
    public static final String ARG_ITEMS_COUNT = "items_count";

    interface Callback
    {
        public void onItemScrolled(int position, long itemId);
    }

    private Callback dummyCallback = new Callback()
    {
        @Override
        public void onItemScrolled(int position, long itemId)
        {

        }
    };

    private Callback callback = dummyCallback;

    private Cursor cursor = null;

    private GlobalState getGlobalState()
    {
        return (GlobalState) getActivity().getApplication();
    }

    private long collectionId = -1;
    private int positionId = -1;


    void setCallback(Callback callback)
    {
        if(callback == null)
        {
            this.callback = dummyCallback;
        }
        else this.callback = callback;
    }

    public void setCollectionId( long collectionId)
    {
        Log.d(TAG,"setCollectionId=" + collectionId);
        if(collectionId != this.collectionId || cursor == null )
        {
            ((ViewPager)getActivity().findViewById(R.id.pager))
                    .setAdapter(
                            new ItemPagerAdapter(
                                    null,
                                    getActivity().getSupportFragmentManager(),
                                    getGlobalState().getStorage()));
            Cursor oldCursor = cursor;
            cursor = null;
            this.collectionId = collectionId;

            Bundle bundle = new Bundle();
            bundle.putLong(ItemListFragment.ARG_COLLECTION_KEY,collectionId);
            getLoaderManager().restartLoader(R.id.loder_item_pager, bundle, this);
            if(oldCursor != null) oldCursor.close();
        }
    }

    public void setPosition(int position)
    {
        Log.d(TAG,"setPosition=" + position);
        this.positionId = position;
        if(this.cursor != null)
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
                null,
                getActivity().getSupportFragmentManager(),getGlobalState().getStorage()));
        pager.setOnPageChangeListener(this);
        Bundle bundle;
        if(savedInstanceState != null)  bundle = savedInstanceState; // 1
        else if(getArguments() != null) bundle = getArguments();     // 2
        else                            bundle = getActivity().getIntent().getExtras(); // 3

        if(bundle != null)
        {
            long id = bundle.getLong(ARG_COLLECTION_ID, 0);
            int position = bundle.getInt(ARG_CURRENT_POSITION, 0);
            setCollectionId(id);
            setPosition(position);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(this.cursor != null) cursor.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_pager, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        ItemsLoader loader
                = new ItemsLoader(this.getActivity(), this.collectionId, getGlobalState().getStorage());
        loader.setUpdateThrottle(3000);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        if(this.cursor != null) cursor.close();
        this.cursor = cursor;
        ViewPager pager = ((ViewPager)getActivity().findViewById(R.id.pager));
        pager.setAdapter(new ItemPagerAdapter(
                cursor, getActivity().getSupportFragmentManager(),getGlobalState().getStorage()));
        pager.setCurrentItem(positionId,true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        if(getActivity() != null)
        {
            ViewPager pager = (ViewPager)getActivity().findViewById(R.id.pager);
            if(pager != null)
            {
                pager.setAdapter(new ItemPagerAdapter(
                        null,
                        getActivity().getSupportFragmentManager(), getGlobalState().getStorage()));
            }
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {}

    @Override
    public void onPageSelected(int position)
    {
        cursor.moveToPosition(position);
        callback.onItemScrolled(position, cursor.getLong(0));
    }

    @Override
    public void onPageScrollStateChanged(int i) {}

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_COLLECTION_ID, collectionId);
        outState.putInt(ARG_CURRENT_POSITION,positionId);
    }
}

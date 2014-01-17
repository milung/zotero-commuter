package sk.mung.sentience.zoterocommuter.navigation;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

import sk.mung.sentience.zoterocommuter.ItemListFragment;
import sk.mung.sentience.zoterocommuter.R;

public class NavigationTreeAdapter extends NavigationTreeAdapterBase
{

    public NavigationTreeAdapter(Context context, DrawerFragment.Callbacks callbacks)
	{
        super(context, callbacks);
        Resources resources = context.getResources();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addNavigationGroup(
                new ReadingQueueGroup(resources.getString(R.string.navigation_reading_queue),
                        inflater,resources.getDrawable(R.drawable.ic_read_later)),
                1);
        setCollectionsIndex(-1);
	}

    static Fragment createInitialFragment()
    {
        Bundle arguments = new Bundle();

        arguments.putLong(ItemListFragment.ARG_COLLECTION_KEY, 0);
        arguments.putBoolean(ItemListFragment.ARG_IS_READING_QUEUE, true);
        Fragment fragment = new ItemListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

}

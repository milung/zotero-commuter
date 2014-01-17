package sk.mung.sentience.zoterocommuter.navigation;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoterocommuter.R;
import sk.mung.sentience.zoterocommuter.storage.ZoteroCollection;

public class NavigationTreeAdapter extends NavigationTreeAdapterBase
{

    public NavigationTreeAdapter(Context context, DrawerFragment.Callbacks callbacks)
	{
        super(context, callbacks);

	}

    static Fragment createInitialFragment(long collectionKey)
    {
        Bundle arguments = new Bundle();
        arguments.putLong(ItemListFragment.ARG_COLLECTION_KEY, collectionKey);
        Fragment fragment = new ItemListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

}

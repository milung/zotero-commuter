package sk.mung.sentience.zoterosentience.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import sk.mung.sentience.zoterosentience.GlobalState;
import sk.mung.sentience.zoterocommuter.R;
import sk.mung.sentience.zoterosentience.storage.ZoteroCollection;
import sk.mung.sentience.zoterosentience.storage.CollectionsTreeLoader;

/**
 * A list fragment representing a list of LibraryItems. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link sk.mung.sentience.zoterosentience.ItemListFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class DrawerFragment extends Fragment
    implements LoaderCallbacks<ZoteroCollection>
{
    private static final String TREE_STATE = "TreeState";

	/**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks  mCallbacks    = DummyCallbacks;

    //private SimpleCursorAdapter adapter;
    private NavigationTreeAdapter treeAdapter;
    private ExpandableListView treeView;
    private Parcelable treeState;
    

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks
    {
        public void onAllItemsSelected();

        public void onLoginToZotero();

        public void onNavigateTo(Fragment fragment, boolean putBackState);

        public void onSettingsSelected();
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    public static final Callbacks DummyCallbacks
    	= new Callbacks()
         {
            @Override
            public void onAllItemsSelected(){}

            @Override
            public void onLoginToZotero() {}

        @Override
        public void onNavigateTo(Fragment fragment, boolean putBackState) {}

        @Override
        public void onSettingsSelected() {

        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DrawerFragment()
    {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        treeAdapter = new NavigationTreeAdapter(getActivity(), mCallbacks);
        treeView = (ExpandableListView) getActivity().findViewById(R.id.expandableListView);
        treeView.setSaveEnabled(true);
        treeView.setAdapter(treeAdapter);

        treeView.setGroupIndicator(null);
        treeView.setOnChildClickListener(treeAdapter);
        treeView.setOnGroupClickListener(treeAdapter);

        treeView.expandGroup(treeAdapter.getCollectionsIndex());

        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public View onCreateView(
    		LayoutInflater inflater, 
    		ViewGroup container,
    		Bundle savedInstanceState) 
    {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(TREE_STATE))
        {
        	treeState = savedInstanceState.getParcelable(TREE_STATE);
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) { throw new IllegalStateException(
                "Activity must implement fragment's callbacks."); }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = DummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Parcelable state = treeView.onSaveInstanceState();
        outState.putParcelable( TREE_STATE, state);
    }
    
    @Override
    public Loader<ZoteroCollection> onCreateLoader(int id, Bundle args)
    {
        CollectionsTreeLoader loader 
            = new CollectionsTreeLoader(this.getActivity(),getGlobalState().getStorage());
        loader.setUpdateThrottle(3000);
        return loader;
    }

    private GlobalState getGlobalState()
    {
        return (GlobalState) getActivity().getApplication();
    }

    @Override
    public void onLoadFinished(Loader<ZoteroCollection> loader, ZoteroCollection tree)
    {
    	treeAdapter.setRoot(tree);
    	if(treeState != null)
    	{
    		treeView.onRestoreInstanceState(treeState);
    	}
        else
        {
            treeView.setItemChecked(1,true);
        }
    }

    @Override
    public void onLoaderReset(Loader<ZoteroCollection> loader)
    {
    	treeAdapter.setRoot(null);        
    }

}

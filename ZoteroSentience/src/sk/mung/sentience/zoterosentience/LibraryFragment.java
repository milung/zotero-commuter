package sk.mung.sentience.zoterosentience;

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

import sk.mung.sentience.zoterosentience.navigation.NavigationTreeAdapter;
import sk.mung.sentience.zoterosentience.storage.ZoteroCollection;
import sk.mung.sentience.zoterosentience.storage.CollectionsTreeLoader;

/**
 * A list fragment representing a list of LibraryItems. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link ItemListFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class LibraryFragment extends Fragment
    implements LoaderCallbacks<ZoteroCollection>
{
    private static final String TREE_STATE = "TreeState";

	/**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks  mCallbacks    = sDummyCallbacks;

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
        /**
         * Callback for when an item has been selected.
         */
        public void onCollectionSelected(long id );

        public void onAllItemsSelected();
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks 
    	= new Callbacks()
         {
             @Override
             public void onCollectionSelected(long id )
             {}

        @Override
        public void onAllItemsSelected()
        {

        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LibraryFragment()
    {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        treeAdapter = new NavigationTreeAdapter(getActivity(), mCallbacks);
        treeView = (ExpandableListView) getActivity().findViewById(R.id.expandableListView);
        treeView.setSaveEnabled(true);
        treeView.setAdapter(treeAdapter);
        treeView.setActivated(true);

        treeView.setGroupIndicator(null);
        treeView.setOnChildClickListener(treeAdapter);
        treeView.setOnGroupClickListener(treeAdapter);

        treeView.expandGroup(1);

        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public View onCreateView(
    		LayoutInflater inflater, 
    		ViewGroup container,
    		Bundle savedInstanceState) 
    {
        return inflater.inflate(R.layout.fragment_library, container, false);
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
        mCallbacks = sDummyCallbacks;
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
            treeView.setItemChecked(0,true);
        }
    }

    @Override
    public void onLoaderReset(Loader<ZoteroCollection> loader)
    {
    	treeAdapter.setRoot(null);        
    }

}

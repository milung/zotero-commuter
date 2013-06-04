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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ListView;
import sk.mung.sentience.zoteroapi.ZoteroCollection;
import sk.mung.sentience.zoterosentience.storage.CollectionsTreeLoader;

/**
 * A list fragment representing a list of LibraryItems. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link LibraryItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class LibraryItemListFragment extends Fragment
    implements LoaderCallbacks<ZoteroCollection>, 
    	ExpandableListView.OnChildClickListener, 
    	ExpandableListView.OnGroupClickListener,
    	OnItemClickListener
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

    /**
     * The current activated item position. Only used on tablets.
     */
    private int  mActivatedPosition       = 0;
    
    //private SimpleCursorAdapter adapter;
    private CollectionsTreeAdapter treeAdapter;
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
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks 
    	= new Callbacks()
         {
             @Override
             public void onItemSelected(String id)
             {}
         };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LibraryItemListFragment()
    {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        treeAdapter = new CollectionsTreeAdapter(getActivity());
        treeView = (ExpandableListView) getActivity().findViewById(R.id.expandableListView);
        treeView.setSaveEnabled(true);
        treeView.setAdapter(treeAdapter);
        treeView.setActivated(true);
        
        treeView.setGroupIndicator(null);
        treeView.setOnChildClickListener(this);
        treeView.setOnGroupClickListener(this);
        getLoaderManager().initLoader(0, null, this);        
    }
    
    @Override
    public View onCreateView(
    		LayoutInflater inflater, 
    		ViewGroup container,
    		Bundle savedInstanceState) 
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(TREE_STATE))
        {
        	treeState = savedInstanceState.getParcelable(TREE_STATE);
        }
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
        {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
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
        if (mActivatedPosition != ListView.INVALID_POSITION)
        {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }
    
    

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick)
    {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        /*getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);*/
    }

    private void setActivatedPosition(int position)
    {
       if (position == ListView.INVALID_POSITION)
        {
            treeView.setItemChecked(mActivatedPosition, false);
        }
        else
        {
        	treeView.setItemChecked(position, true);
        }
        mActivatedPosition = position;
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
    }

    @Override
    public void onLoaderReset(Loader<ZoteroCollection> loader)
    {
    	treeAdapter.setRoot(null);        
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) 
    {
        int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
        parent.setItemChecked(index, true);
        return true;
    }

	@Override
	public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition,
			long id) 
	{
		int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
        boolean wasChecked = parent.isItemChecked(index);
		parent.setItemChecked(index, true);
        // finish handling if selecting expanded group - close it on second click only
		return (parent.isGroupExpanded(groupPosition) && !wasChecked);
        
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		mCallbacks.onItemSelected(Long.toString(id));
		mActivatedPosition = position;
	}
}

package sk.mung.sentience.zoterosentience.navigation;

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
import sk.mung.sentience.zoterosentience.storage.ZoteroCollection;

public class NavigationTreeAdapter extends BaseExpandableListAdapter
    implements
        ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener
{
    private final List<NavigationGroup> navigationGroups = new ArrayList<NavigationGroup>();

    private final DrawerFragment.Callbacks callbacks;
    private int collectionsIndex;

    public NavigationTreeAdapter(Context context, DrawerFragment.Callbacks callbacks)
	{
        this.callbacks = callbacks;
        Resources resources = context.getResources();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        navigationGroups.add(
                new LoginGroup(
                    context,
                    inflater,
                    resources.getString(R.string.login),
                    resources.getDrawable(R.drawable.ic_zotero),
                    resources.getDrawable(R.drawable.ic_user_holo)));

        navigationGroups.add(new AllItemsGroup(
                resources.getString(R.string.all_items),
                inflater,
                resources.getDrawable(R.drawable.ic_briefcase_holo)));
        collectionsIndex = navigationGroups.size();
        navigationGroups.add(new CollectionNavigationGroup(
                resources.getString(R.string.nav_collections),
                inflater,
                resources.getDrawable(R.drawable.ic_collection_folder),
                resources.getDrawable(R.drawable.ic_navigation_expand),
                resources.getDrawable(R.drawable.ic_navigation_collapse)));

        navigationGroups.add( new SettingsGroup(
                inflater,
                resources.getString(R.string.settings),
                resources.getDrawable(R.drawable.ic_nav_settings)));

		setRoot(null);
	}
	@Override
	public Object getChild(int groupPosition, int childPosition) 
	{
        return navigationGroups.get(groupPosition).getChild(childPosition);
    }

	@Override
	public long getChildId(int groupPosition, int childPosition) 
	{
        return navigationGroups.get(groupPosition).getChildId(childPosition);
	}

	@Override
	public View getChildView(
			int groupPosition, 
			int childPosition, 
			boolean isLastChild, 
			View convertView, 
			ViewGroup parent) 
	{
        return navigationGroups.get(groupPosition).getChildView(childPosition, isLastChild, convertView, parent);
	}

	@Override
	public int getChildrenCount(int groupPosition) 
	{
        return  navigationGroups.get(groupPosition).getChildrenCount();
	}

	@Override
	public Object getGroup(int groupPosition) 
	{		
		return navigationGroups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {

		return navigationGroups.size();
	}

	@Override
	public long getGroupId(int groupPosition)
    {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		return navigationGroups.get(groupPosition).getGroupView(convertView, isExpanded);
	}

	@Override
	public boolean hasStableIds() { return true; }

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) { return true; 	}
	
	public void setRoot(ZoteroCollection tree) 
	{
		List<ZoteroCollection> children = new ArrayList<ZoteroCollection>();
		
		if (tree != null)
		{
            addGroupChildren( tree, children);
		}

        for(NavigationGroup group : navigationGroups)
        {
            if( group instanceof CollectionNavigationGroup)
            {
                ((CollectionNavigationGroup)group).setChildren(children);
            }
        }
		notifyDataSetChanged();
	}
	
	/** flattens the children tree 
	 **/
	private void addGroupChildren(ZoteroCollection group,
			List<ZoteroCollection> groupChildren) 
	{
		for(ZoteroCollection child : group.getChildren() )
		{
			groupChildren.add(child);
			addGroupChildren( child, groupChildren);
		}		
	}

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
    {
        int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
        navigationGroups.get(groupPosition).childClicked(childPosition, id, callbacks);
        if(navigationGroups.get(groupPosition).areChildrenSelectable())
        {
            parent.setItemChecked(index, true);
        }
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
    {
        int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
        if(navigationGroups.get(groupPosition).isGroupSelectable())
        {
            parent.setItemChecked(index, true);
        }
        return navigationGroups.get(groupPosition).clicked(id, callbacks);
    }

    public int getCollectionsIndex() {
        return collectionsIndex;
    }
}

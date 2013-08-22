package sk.mung.sentience.zoterosentience.navigation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoterosentience.LibraryFragment;
import sk.mung.sentience.zoterosentience.R;
import sk.mung.sentience.zoterosentience.storage.ZoteroCollection;

public class NavigationTreeAdapter extends BaseExpandableListAdapter
    implements
        ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener
{
    private final List<NavigationGroup> navigationGroups = new ArrayList<NavigationGroup>();

	private final LayoutInflater inflater;
	private final Drawable openDrawable;
	private final Drawable closeDrawable;
    private final LibraryFragment.Callbacks callbacks;
	
	public NavigationTreeAdapter(Context context, LibraryFragment.Callbacks callbacks)
	{
        this.callbacks = callbacks;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		openDrawable = context.getResources().getDrawable(R.drawable.expander_open_holo_light);
		closeDrawable = context.getResources().getDrawable(R.drawable.expander_close_holo_light);

        navigationGroups.add(new AllItemsGroup(context.getResources().getString(R.string.all_items)));
        navigationGroups.add(new CollectionNavigationGroup(
                context.getResources().getString(R.string.nav_collections), inflater,openDrawable,closeDrawable));

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
        return navigationGroups.get(groupPosition).getChildView(childPosition, isLastChild, convertView, parent );
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
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		return bindView(navigationGroups.get(groupPosition), convertView, parent, isExpanded);
	}

	private View bindView(NavigationGroup navigationGroup, View convertView, ViewGroup parent, boolean isExpanded) {
		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.listitem_collection, null);
		}
        assert convertView != null;
        TextView textView = (TextView) convertView.findViewById(R.id.title);
		textView.setText(navigationGroup.getName());
		
		ImageView imageView = (ImageView) convertView.findViewById(R.id.indicator);
		if( navigationGroup.getChildrenCount() == 0)
		{
			imageView.setImageDrawable(null);
		}
		else imageView.setImageDrawable( isExpanded ? closeDrawable : openDrawable);
		return convertView;
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
        parent.setItemChecked(index, true);
        navigationGroups.get(groupPosition).childClicked(childPosition, id, callbacks);
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
    {
        int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
        parent.setItemChecked(index, true);
        return navigationGroups.get(groupPosition).clicked(id, callbacks);
    }
}

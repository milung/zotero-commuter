package sk.mung.sentience.zoterosentience;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sk.mung.sentience.zoterosentience.storage.ZoteroCollection;
import sk.mung.sentience.zoterosentience.storage.ZoteroStorageImpl;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class CollectionsTreeAdapter extends BaseExpandableListAdapter 
{
	private ZoteroCollection root = new ZoteroCollection();
	private final List< List<ZoteroCollection>> children 
		= new ArrayList< List<ZoteroCollection>>(); 
	private final LayoutInflater inflater;
	private final Drawable openDrawable;
	private final Drawable closeDrawable;
	
	public CollectionsTreeAdapter(Context context)
	{
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		openDrawable = context.getResources().getDrawable(R.drawable.expander_open_holo_light);
		closeDrawable = context.getResources().getDrawable(R.drawable.expander_close_holo_light);
		setRoot(null);
	}
	@Override
	public Object getChild(int groupPosition, int childPosition) 
	{		
		if(groupPosition == 0) { return null; }
		return children.get(groupPosition).get(childPosition);		
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) 
	{
		ZoteroCollection collection = (ZoteroCollection)getChild(groupPosition, childPosition);
		return collection.getId();
	}

	@Override
	public View getChildView(
			int groupPosition, 
			int childPosition, 
			boolean isLastChild, 
			View convertView, 
			ViewGroup parent) 
	{
		ZoteroCollection collection = (ZoteroCollection)getChild(groupPosition, childPosition);
		return bindView(collection, convertView, parent,false, false);
	}

	@Override
	public int getChildrenCount(int groupPosition) 
	{						
		return children.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) 
	{		
		List<ZoteroCollection> groups = root.getChildren();
		if( groupPosition == 0) return root;
		else if(groupPosition - 1 < groups.size())
		{
			return root.getChildren().get(groupPosition - 1);
		}
		else // if change in tree caused changes in collapsed states, search for closest group
		{
			ZoteroCollection parentGroup = root;
			int pos = 0;
			int parentPos = 0;
			while( pos < groupPosition && groupPosition < groups.size())
			{
				parentGroup = groups.get(parentPos);
				parentPos++;
				pos = 1 + children.get(parentPos).size();
			}
			return parentGroup;
		}
	}

	@Override
	public int getGroupCount() {

		return children.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		ZoteroCollection collection = (ZoteroCollection)getGroup(groupPosition);
		return collection.getId();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ZoteroCollection collection = (ZoteroCollection)getGroup(groupPosition);
		return bindView(collection, convertView, parent, groupPosition != 0, isExpanded);
	}
	private View bindView(ZoteroCollection collection, View convertView, ViewGroup parent, boolean isGroup, boolean isExpanded) {
		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.listitem_collection, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.title);
		textView.setText(collection.getName());
		
		ImageView imageView = (ImageView) convertView.findViewById(R.id.indicator);
		if( !isGroup || collection.getChildren().size() == 0)
		{
			imageView.setImageDrawable(null);
		}
		else imageView.setImageDrawable( isExpanded ? closeDrawable : openDrawable);
		
		LayoutParams params = (LayoutParams) textView.getLayoutParams();
		params.leftMargin = 32 * collection.getNestedLevel();
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() { return true; }

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) { return true; 	}
	
	public void setRoot(ZoteroCollection tree) 
	{
		children.clear();
		children.add(Collections.<ZoteroCollection>emptyList());
		
		if (tree == null) 
		{
			root = ZoteroStorageImpl.getEmptyLibrary();
		}
		else
		{
			root = tree;
			for( ZoteroCollection group : root.getChildren())
			{
				List<ZoteroCollection > groupChildren = new ArrayList<ZoteroCollection>();
				addGroupChildren( group, groupChildren);
				children.add( groupChildren);
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

}

package sk.mung.sentience.zoterosentience.navigation;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoterosentience.ItemListFragment;
import sk.mung.sentience.zoterosentience.R;
import sk.mung.sentience.zoterosentience.storage.ZoteroCollection;

public class CollectionNavigationGroup implements NavigationGroup
{
    private final String title;
    private final LayoutInflater inflater;
    private final Drawable iconDrawable;
    private final Drawable openDrawable;
    private final Drawable closeDrawable;

    public CollectionNavigationGroup(String title, LayoutInflater inflater, Drawable iconDrawable, Drawable openDrawable, Drawable closeDrawable)
    {
        this.title = title;
        this.inflater = inflater;
        this.iconDrawable = iconDrawable;
        this.openDrawable = openDrawable;
        this.closeDrawable = closeDrawable;
    }

    public void setChildren(List<ZoteroCollection> children)
    {
        this.children = children;
    }

    private List<ZoteroCollection> children = new ArrayList<ZoteroCollection>();

    @Override
    public Object getChild(int childPosition)
    {
        return children.get(childPosition);
    }

    @Override
    public long getChildId(int childPosition)
    {
        return children.get(childPosition).getId();
    }

    @Override
    public View getChildView(int childPosition, boolean lastChild, View convertView, ViewGroup parent)
    {
        ZoteroCollection collection = (ZoteroCollection)getChild(childPosition);
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.listitem_collection, null);
        }
        assert convertView != null;
        TextView textView = (TextView) convertView.findViewById(R.id.title);
        textView.setText(collection.getName());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
        assert params != null;
        params.leftMargin = 32 + 32 * collection.getNestedLevel();

        return convertView;
    }

    @Override
    public int getChildrenCount()
    {
        return children.size();
    }

    @Override
    public String getName()
    {
        return title;
    }

    @Override
    public void childClicked(int childPosition, long id, DrawerFragment.Callbacks callbacks)
    {
        Bundle arguments = new Bundle();
        arguments.putLong(ItemListFragment.ARG_COLLECTION_KEY, id);
        Fragment fragment = new ItemListFragment();
        fragment.setArguments(arguments);
        callbacks.onNavigateTo(fragment, false);
    }

    @Override
    public boolean clicked(long id, DrawerFragment.Callbacks callbacks)
    {
        return false;
    }

    @Override
    public boolean areChildrenSelectable() {
        return true;
    }

    @Override
    public boolean isGroupSelectable() {
        return false;
    }

    @Override
    public View getGroupView( View convertView, boolean isExpanded)
    {
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.listitem_navigation_group, null);
        }
        assert convertView != null;
        TextView textView = (TextView) convertView.findViewById(R.id.title);
        textView.setText(getName());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.navigation_group_image);
        imageView.setImageDrawable(iconDrawable);

        imageView = (ImageView) convertView.findViewById(R.id.indicator);
        imageView.setImageDrawable( isExpanded ? closeDrawable : openDrawable);
        return convertView;
    }
}

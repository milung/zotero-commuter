package sk.mung.sentience.zoterocommuter.navigation;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sk.mung.sentience.zoterocommuter.ItemListFragment;
import sk.mung.sentience.zoterocommuter.R;

class ReadingQueueGroup implements NavigationGroup
{
    private final String title;
    private final LayoutInflater inflater;
    private final Drawable icon;

    ReadingQueueGroup(String title, LayoutInflater inflater, Drawable icon)
    {
        this.title = title;
        this.inflater = inflater;
        this.icon = icon;
    }

    @Override
    public Object getChild(int childPosition) { return null; }

    @Override
    public long getChildId(int childPosition) { return 0; }

    @Override
    public View getChildView(int childPosition, boolean lastChild, View convertView, ViewGroup parent)
    {
        return null;
    }

    @Override
    public int getChildrenCount() { return 0; }

    @Override
    public String getName() { return title; }

    @Override
    public void childClicked(int childPosition, long id, DrawerFragment.Callbacks callbacks) {}

    @Override
    public boolean clicked(long id, DrawerFragment.Callbacks callbacks)
    {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ItemListFragment.ARG_IS_READING_QUEUE, true);
        Fragment fragment = new ItemListFragment();
        fragment.setArguments(arguments);

        callbacks.onNavigateTo(fragment, true);
        return true;
    }

    @Override
    public boolean areChildrenSelectable() {
        return false;
    }

    @Override
    public boolean isGroupSelectable() {
        return true;
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
        imageView.setImageDrawable(icon);

        imageView = (ImageView) convertView.findViewById(R.id.indicator);
        imageView.setImageDrawable(null);
        return convertView;
    }
}

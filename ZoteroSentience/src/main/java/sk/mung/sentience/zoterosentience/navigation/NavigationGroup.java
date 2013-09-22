package sk.mung.sentience.zoterosentience.navigation;

import android.view.View;
import android.view.ViewGroup;

interface NavigationGroup
{
    View getGroupView( View convertView, boolean isExpanded);

    Object getChild(int childPosition);

    long getChildId(int childPosition);

    View getChildView(int childPosition, boolean lastChild, View convertView, ViewGroup parent);

    int getChildrenCount();

    String getName();

    void childClicked(int childPosition, long id, DrawerFragment.Callbacks callbacks);

    boolean clicked(long id, DrawerFragment.Callbacks callbacks);

    boolean areChildrenSelectable();

    boolean isGroupSelectable();
}

package sk.mung.sentience.zoterosentience.navigation;

import android.view.View;
import android.view.ViewGroup;

import sk.mung.sentience.zoterosentience.LibraryFragment;

interface NavigationGroup
{
    Object getChild(int childPosition);

    long getChildId(int childPosition);

    View getChildView(int childPosition, boolean lastChild, View convertView, ViewGroup parent);

    int getChildrenCount();

    String getName();

    void childClicked(int childPosition, long id, LibraryFragment.Callbacks callbacks);

    boolean clicked(long id, LibraryFragment.Callbacks callbacks);
}

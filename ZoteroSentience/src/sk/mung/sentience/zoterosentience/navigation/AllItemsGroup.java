package sk.mung.sentience.zoterosentience.navigation;


import android.view.View;
import android.view.ViewGroup;

import sk.mung.sentience.zoterosentience.LibraryFragment;

class AllItemsGroup implements NavigationGroup
{
    private final String title;

    AllItemsGroup(String title)
    {
        this.title = title;
    }

    @Override
    public Object getChild(int childPosition)
    {
        return null;
    }

    @Override
    public long getChildId(int childPosition)
    {
        return 0;
    }

    @Override
    public View getChildView(int childPosition, boolean lastChild, View convertView, ViewGroup parent)
    {
        return null;
    }

    @Override
    public int getChildrenCount()
    {
        return 0;
    }

    @Override
    public String getName()
    {
        return title;
    }

    @Override
    public void childClicked(int childPosition, long id, LibraryFragment.Callbacks callbacks)
    {

    }

    @Override
    public boolean clicked(long id, LibraryFragment.Callbacks callbacks)
    {
        callbacks.onAllItemsSelected();
        return true;
    }
}

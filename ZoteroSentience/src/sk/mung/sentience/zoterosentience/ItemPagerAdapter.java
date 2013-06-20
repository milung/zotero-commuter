package sk.mung.sentience.zoterosentience;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import sk.mung.sentience.zoteroapi.entities.Item;


public class ItemPagerAdapter extends FragmentStatePagerAdapter
{
    private final List<Item> items;

    public ItemPagerAdapter(List<Item> items, FragmentManager fragmentManager)
    {
        super(fragmentManager);
        this.items = items;
    }
    @Override
    public Fragment getItem(int position)
    {
        return new ItemViewer(items.get(position));
    }

    @Override
    public int getCount()
    {
        return items.size();
    }
}

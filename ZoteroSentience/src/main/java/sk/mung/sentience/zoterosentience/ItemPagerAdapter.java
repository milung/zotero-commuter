package sk.mung.sentience.zoterosentience;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import sk.mung.sentience.zoterosentience.storage.ZoteroStorageImpl;
import sk.mung.zoteroapi.entities.Item;


public class ItemPagerAdapter extends FragmentStatePagerAdapter
{
    private final Cursor cursor;
    private final ZoteroStorageImpl storage;

    public ItemPagerAdapter(Cursor cursor, FragmentManager fragmentManager,ZoteroStorageImpl storage)
    {
        super(fragmentManager);
        this.cursor = cursor;
        this.storage = storage;
    }
    @Override
    public Fragment getItem(int position)
    {
        ItemViewer view = new ItemViewer();
        cursor.moveToPosition(position);
        Item item = storage.cursorToItem(cursor);
        view.setItem(item);
        return view;
    }

    @Override
    public int getCount()
    {
        return cursor == null ? 0 : cursor.getCount();
    }
}

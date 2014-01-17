package sk.mung.sentience.zoterocommuter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;

import sk.mung.sentience.zoterocommuter.storage.ReadingQueueLoader;

public class ReadingQueuePager extends ItemPager
{
    int itemsCount = -1;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        ReadingQueueLoader loader
                = new ReadingQueueLoader(this.getActivity(), getGlobalState().getStorage());
        loader.setUpdateThrottle(3000);
        return loader;
    }

    @Override
    protected String getActionTitle()
    {
       return getString(R.string.navigation_reading_queue);
    }

    @Override
    protected String getActionSubtitle()
    {
        if(itemsCount < 0) return "";
        else return getResources().getQuantityString(R.plurals.number_of_items, itemsCount, itemsCount);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        itemsCount = cursor.getCount();
        super.onLoadFinished(loader, cursor);
    }
}
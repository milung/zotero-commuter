package sk.mung.sentience.zoterocommuter.storage;

import android.content.Context;
import android.database.Cursor;

import sk.mung.sentience.zoterocommuter.R;


public class ReadingQueueLoader extends AbstractZoteroCursorLoader
{


    public ReadingQueueLoader(Context context,ZoteroStorageImpl storage)
    {
        super(context, storage);
        storage.addListener(this);
    }

    @Override
    protected Cursor getCursor(ZoteroStorageImpl storage)
    {
        return storage.findItemsCursorByTag(getContext().getString(R.string.read_later_tag));
    }

    @Override
    public void onItemsUpdated()
    {
        onChanged();
    }

}

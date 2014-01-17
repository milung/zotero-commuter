package sk.mung.sentience.zoterocommuter.storage;

import android.content.Context;
import android.database.Cursor;


public class TagsLoader extends AbstractZoteroCursorLoader
{
    public TagsLoader(Context context, ZoteroStorageImpl storage)
    {
        super(context, storage);
        storage.addListener(this);
    }

    @Override
    protected Cursor getCursor(ZoteroStorageImpl storage)
    {
        return storage.findAllTagsCursor();
    }

    @Override
    public void onItemsUpdated()
    {
        onChanged();
    }

    public static String getTagLabel(Cursor cursor)
    {
        return cursor.getString(cursor.getColumnIndex(TagsDao.COLUMN_TAG));
    }
}

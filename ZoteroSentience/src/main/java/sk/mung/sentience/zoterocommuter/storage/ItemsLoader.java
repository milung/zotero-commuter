package sk.mung.sentience.zoterocommuter.storage;

import android.content.Context;
import android.database.Cursor;


public class ItemsLoader extends AbstractZoteroCursorLoader
{
    private final Long collectionId;

    public ItemsLoader(Context context, Long collectionId, ZoteroStorageImpl storage)
    {
        super(context, storage);
        this.collectionId = collectionId;
        storage.addListener(this);
    }

    @Override
    protected Cursor getCursor(ZoteroStorageImpl storage)
    {
        return storage.findItemsCursorByCollectionId(collectionId == null ? 0 : collectionId);
    }

    @Override
    public void onItemsUpdated()
    {
        onChanged();
    }

}

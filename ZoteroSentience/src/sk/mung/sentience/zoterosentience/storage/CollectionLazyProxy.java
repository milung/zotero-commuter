package sk.mung.sentience.zoterosentience.storage;

import android.database.Cursor;

import java.util.List;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;

/**
 * Created by sk1u00e5 on 19.6.2013.
 */
class CollectionLazyProxy extends CollectionEntity
{
    private final ItemsDao itemsDao;
    private boolean areItemsLoaded = false;
    private int itemsCount = -1;

    CollectionLazyProxy(ItemsDao dao)
    {
        this.itemsDao = dao;
    }

    @Override
    public List<Item> getItems()
    {
        loadItems();
        return super.getItems();
    }

    @Override
    public int getItemsCount()
    {
        if(areItemsLoaded)
        {
            return super.getItemsCount();
        }
        else if(0 <= itemsCount )
        {
            return itemsCount;
        }
        else
        {
            Cursor cursor = itemsDao.cursorByCollectionId(getId());
            try
            {
                itemsCount = cursor.getCount();
                return itemsCount;
            }
            finally
            {
                cursor.close();
            }
        }

    }

    private synchronized void loadItems()
    {
        if(!areItemsLoaded )
        {
            for(Item item : itemsDao.findByCollection(this))
            {
                super.addItem(item);
            }

            areItemsLoaded = true;
        }
    }
}

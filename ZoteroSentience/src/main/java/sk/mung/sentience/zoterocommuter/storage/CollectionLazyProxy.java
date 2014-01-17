package sk.mung.sentience.zoterocommuter.storage;

import android.database.Cursor;

import java.util.List;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;

class CollectionLazyProxy extends CollectionEntity implements BaseDao.UpdateListener
{
    private final ItemsDao itemsDao;
    private boolean areItemsLoaded = false;
    private int itemsCount = -1;

    CollectionLazyProxy(ItemsDao dao)
    {
        this.itemsDao = dao;
    }

    @Override
    public synchronized List<Item> getItems()
    {
        loadItems();
        return super.getItems();
    }

    @Override
    public  synchronized void removeItem(Item item)
    {
        loadItems();
        super.removeItem(item);
    }

    @Override
    public synchronized int getItemsCount()
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

    private  void loadItems()
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

    @Override
    public synchronized void onDataUpdated(BaseDao sender, Long entityId)
    {
        areItemsLoaded = false;
        getItems().clear();
    }
}

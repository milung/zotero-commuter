package sk.mung.sentience.zoterosentience.storage;

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

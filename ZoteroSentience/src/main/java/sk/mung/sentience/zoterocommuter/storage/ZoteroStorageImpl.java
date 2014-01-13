package sk.mung.sentience.zoterocommuter.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sk.mung.zoteroapi.ZoteroStorage;
import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.SyncStatus;

public class ZoteroStorageImpl extends SQLiteOpenHelper implements ZoteroStorage
{
    public Item findItemByKey(@NotNull String key)
    {
        return itemsDao.findByKey(key);
    }

    @Override
    public Item createItem() {
        return itemsDao.createEntity();
    }

    @Override
    public Field createField() {
        return fieldsDao.createEntity();
    }

    @Override
    public void clearCaches()
    {
        for( ZoteroStorageListener listener : listeners )
        {
            listener.onCollectionsUpdated();
            listener.onItemsUpdated();
            listener.onTagsUpdated();
        }
    }

    public List<Item> findItemsBySynced(@NotNull SyncStatus syncStatus)
    {
        return itemsDao.findBySynced(syncStatus);
    }

    public Cursor findItemsCursorByCollectionId(long collectionId)
    {
        return itemsDao.cursorByCollectionId(collectionId);
    }

    public Item cursorToItem(Cursor cursor)
    {
        return itemsDao.cursorToEntity(cursor);
    }

    public void removeLocalVersion(Item item)
    {
        if(item.getSynced() == SyncStatus.SYNC_CONFLICT || item.getSynced() == SyncStatus.SYNC_LOCALLY_UPDATED)
        {
            Item remoteVersion = itemsDao.findByKey(ItemsDao.CONFLICTED_KEY_PREFIX + item.getKey());
            if(remoteVersion != null)
            {
                item.copyState(remoteVersion);
                item.setSynced(SyncStatus.SYNC_OK);
                itemsDao.delete(remoteVersion);
                itemsDao.upsert(item);
            }
            else item.setSynced(SyncStatus.SYNC_OK);
        }
    }

    public void replaceRemoteVersion(Item item)
    {
        if(item.getSynced() == SyncStatus.SYNC_CONFLICT )
        {
            Item remoteVersion = itemsDao.findByKey(ItemsDao.CONFLICTED_KEY_PREFIX + item.getKey());
            if(remoteVersion != null)
            {
                item.setVersion(remoteVersion.getVersion());
                item.setSynced(SyncStatus.SYNC_LOCALLY_UPDATED);
                itemsDao.upsert(item);
            }
        }
    }

    public void markAsDeleted(Item item)
    {
        item.setSynced(SyncStatus.SYNC_DELETED);
        Item parent = itemsDao.findByKey(item.getParentKey());
        if(parent != null)
        {
            parent.removeChild(item);
        }

    }

    public void addItemsToCollection(List<Long> itemIds, CollectionEntity collection)
    {
        List<Item> updatedItems = new ArrayList<Item>(itemIds.size());
        List<Item> collectionItems = collection.getItems();

        for(long id : itemIds)
        {
            boolean isNewItem = true;
            for(Item item : collectionItems)
            {
                if(item.getId() == id)
                {
                    isNewItem = false;
                    break;
                }
            }
            if(isNewItem)
            {
                Item item = itemsDao.findById(id);
                updatedItems.add(item);
            }
        }

        for(Item item : updatedItems)
        {
            item.addCollection(collection);
            item.setSynced(SyncStatus.SYNC_LOCALLY_UPDATED);
            itemsDao.upsert(item);
        }

        fireItemsUpdated();

    }

    public void removeItemsFromCollection(List<Long> itemIds, CollectionEntity collection)
    {
        for(long id : itemIds)
        {
            Item item = itemsDao.findById(id);
            item.removeCollection(collection);
            item.setSynced(SyncStatus.SYNC_LOCALLY_UPDATED);
            itemsDao.upsert(item);
        }
        fireItemsUpdated();
    }

    protected void fireItemsUpdated()
    {
        for( ZoteroStorageListener listener : listeners )
        {
            listener.onItemsUpdated();
        }
    }

    class DatabaseConnection
    {
        private final SQLiteOpenHelper sqlite;

        private WeakReference<SQLiteDatabase> writableDatabase = new WeakReference<SQLiteDatabase>(null);

        DatabaseConnection(SQLiteOpenHelper sqlite)
        {
            this.sqlite = sqlite;
        }

        SQLiteDatabase getReadableDatabase()
        {
            return this.getWritableDatabase();
        }

        synchronized SQLiteDatabase getWritableDatabase()
        {
            SQLiteDatabase db = writableDatabase.get();
            if(db == null || !db.isOpen())
            {
                db = sqlite.getWritableDatabase();
                writableDatabase = new WeakReference<SQLiteDatabase>(db);
            }
            return db;
        }

        public void putDatabase(SQLiteDatabase db)
        {
            writableDatabase = new WeakReference<SQLiteDatabase>(db);
        }
    }

	private static final String VERSION_COLLECTIONS = "collections";
	private static final String VERSION_DELETIONS = "deletions";

    private static final String DATABASE_NAME = "ZoteroStorage";
    private static final int DATABASE_VERSION = 19;
    public static final String VERSION_ITEMS = "items";

    private List<ZoteroStorageListener> listeners = new ArrayList<ZoteroStorageListener>();
    private final DatabaseConnection connection = new DatabaseConnection(this);
    private final CollectionsDao collectionsDao;
    private final ItemsDao itemsDao;
    private final VersionsDao versionsDao;
    private final PersonsDao personsDao;
    private final CreatorsDao creatorsDao;
    private final TagsDao tagsDao;
    private final FieldsDao fieldsDao;
    private final RelationsDao relationsDao;


    public void addListener(ZoteroStorageListener listener)
    {
        listeners.add(listener);
    }
    
    @SuppressWarnings("UnusedDeclaration")
    public void removeListener(ZoteroStorageListener listener)
    {
        listeners.remove(listener);
    }
    
    public ZoteroStorageImpl(Context context, QueryDictionary queries)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.versionsDao = new VersionsDao(connection,queries);

        this.personsDao = new PersonsDao(connection, queries);
        this.creatorsDao = new CreatorsDao(connection,queries, personsDao);
        this.tagsDao = new TagsDao(connection, queries);
        this.fieldsDao = new FieldsDao(connection, queries);
        this.relationsDao = new RelationsDao(connection, queries);
        this.itemsDao = new ItemsDao(connection,queries, creatorsDao, tagsDao, fieldsDao, relationsDao);
        this.collectionsDao = new CollectionsDao(connection,queries, itemsDao);
        this.itemsDao.setCollectionsDao(collectionsDao);

        addListener(this.itemsDao);
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        connection.putDatabase(database);
        collectionsDao.createTable();
        versionsDao.createTable();
        tagsDao.createTable();

        fieldsDao.createTable();
        personsDao.createTable();
        creatorsDao.createTable();
        itemsDao.createTable();
        relationsDao.createTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        connection.putDatabase(database);
        if(oldVersion < 20)
        {
            collectionsDao.upgrade( oldVersion, newVersion);

            fieldsDao.upgrade(oldVersion,newVersion);
            itemsDao.upgrade(oldVersion, newVersion);
            creatorsDao.upgrade(oldVersion, newVersion);
            personsDao.upgrade(oldVersion,newVersion);

            tagsDao.upgrade(oldVersion, newVersion);
            versionsDao.upgrade( oldVersion, newVersion);
            relationsDao.upgrade(oldVersion,newVersion);
            onCreate(database);
        }
    }

    public int getCollectionsVersion()
    {
        return versionsDao.getVersion(VERSION_COLLECTIONS);
    }
    
    public void setCollectionsVersion(int version)
    {
        versionsDao.setVersion(VERSION_COLLECTIONS, version);
    }

    public void updateCollections(@NotNull Iterable<CollectionEntity> collections)
    {
        SQLiteDatabase database = getWritableDatabase();
        assert database != null;
        for( CollectionEntity col : collections)
        {
            database.beginTransaction();
            try
            {
                collectionsDao.upsert( col);
                database.setTransactionSuccessful();
            }
            finally
            {
                database.endTransaction();
            }
        }
        
        for( ZoteroStorageListener listener : listeners )
        {
            listener.onCollectionsUpdated();
        }
    }

    public ZoteroCollection getCollectionTree()
	{
        return collectionsDao.getCollectionTree();
	}

    public CollectionEntity findCollectionById(long id)
    {
        return collectionsDao.findById(id);
    }

	public int getDeletionsVersion() 
	{
		return versionsDao.getVersion(VERSION_DELETIONS);
	}

	public void setDeletionsVersion(int version) {
        versionsDao.setVersion( VERSION_DELETIONS, version);
	}

	public void deleteCollections(@NotNull Iterable<String> keys)
    {
        collectionsDao.deleteForKeys(keys);

        for( ZoteroStorageListener listener : listeners )
        {
            listener.onCollectionsUpdated();
        }
	}

    public void deleteItem(@NotNull Item item)
    {
        itemsDao.delete(item);

        fireItemsUpdated();
    }

    public void deleteItems(@NotNull Iterable<String> keys)
    {
        itemsDao.deleteForKeys(keys);

        fireItemsUpdated();
    }

    public void deleteTags(@NotNull Iterable<String> tags)
    {
        for( String tag : tags)
        {
            tagsDao.deleteByName(tag);
        }

        for( ZoteroStorageListener listener : listeners )
        {
            listener.onTagsUpdated();
        }
    }

    public void updateItem(@NotNull Item item)
    {
        updateItemInternal(item);
        fireItemsUpdated();
    }

    private void updateItemInternal(Item item) {
        for(CollectionEntity col : item.getCollections())
        {
            collectionsDao.refresh(col);
        }

        SQLiteDatabase database = getWritableDatabase();
        assert database != null;
        database.beginTransaction();

        try
        {
            itemsDao.upsert(item);
            database.setTransactionSuccessful();
        }
        finally
        {
            database.endTransaction();
        }
    }

    public void updateItems(@NotNull Iterable<Item> items)
	{
		SQLiteDatabase database = getWritableDatabase();
        assert database != null;

		for(Item item : items)
		{
            updateItemInternal(item);
        }

        fireItemsUpdated();
    }

    public int getItemsVersion()
    {
        return versionsDao.getVersion(VERSION_ITEMS);
    }

    public void setItemsVersion(int version)
    {
        versionsDao.setVersion( VERSION_ITEMS, version);
    }

    public void deleteData()
    {
        versionsDao.deleteAll();
        collectionsDao.deleteAll();
        itemsDao.deleteAll();
        versionsDao.deleteAll();
        personsDao.deleteAll();
        creatorsDao.deleteAll();
        tagsDao.deleteAll();
        fieldsDao.deleteAll();

        versionsDao.clearCaches();
        collectionsDao.clearCaches();
        itemsDao.clearCaches();
        versionsDao.clearCaches();
        personsDao.clearCaches();
        creatorsDao.clearCaches();
        tagsDao.clearCaches();
        fieldsDao.clearCaches();

        for( ZoteroStorageListener listener : listeners )
        {
            listener.onCollectionsUpdated();
            listener.onItemsUpdated();
            listener.onTagsUpdated();
        }
    }

}
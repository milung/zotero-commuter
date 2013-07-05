package sk.mung.sentience.zoterosentience.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoteroapi.entities.CollectionEntity;
import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.ItemEntity;

public class ZoteroStorage extends SQLiteOpenHelper
{


    public Item findItemByKey(String key)
    {
        return itemsDao.findByKey(key);
    }

    class DatabaseConnection
    {
        private final SQLiteOpenHelper sqlite;

        private WeakReference<SQLiteDatabase> readableDatabase = new WeakReference<SQLiteDatabase>(null);
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

    private final DatabaseConnection connection = new DatabaseConnection(this);
	private static final String VERSION_COLLECTIONS = "collections";
	private static final String VERSION_DELETIONS = "deletions";

    private static final String DATABASE_NAME = "ZoteroStorage";
    private static final int DATABASE_VERSION = 15;
    public static final String VERSION_ITEMS = "items";

    private List<ZoteroStorageListener> listeners = new ArrayList<ZoteroStorageListener>();
    private final CollectionsDao collectionsDao;
    private final ItemsDao itemsDao;
    private final VersionsDao versionsDao;
    private final PersonsDao personsDao;
    private final CreatorsDao creatorsDao;
    private final TagsDao tagsDao;
    private final FieldsDao fieldsDao;

    public static ZoteroCollection getEmptyLibrary()
    {
        return CollectionsDao.getEmptyLibrary();
    }

    public void addListener(ZoteroStorageListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeListener(ZoteroStorageListener listener)
    {
        listeners.remove(listener);
    }
    
    public ZoteroStorage(Context context, QueryDictionary queries)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.versionsDao = new VersionsDao(connection,queries);

        this.personsDao = new PersonsDao(connection, queries);
        this.creatorsDao = new CreatorsDao(connection,queries, personsDao);
        this.tagsDao = new TagsDao(connection, queries);
        this.fieldsDao = new FieldsDao(connection, queries);
        this.itemsDao = new ItemsDao(connection,queries, creatorsDao, tagsDao, fieldsDao);
        this.collectionsDao = new CollectionsDao(connection,queries, itemsDao);
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        connection.putDatabase(database);
    	collectionsDao.upgrade( oldVersion, newVersion);

        fieldsDao.upgrade(oldVersion,newVersion);
    	itemsDao.upgrade(oldVersion, newVersion);
        creatorsDao.upgrade(oldVersion, newVersion);
        personsDao.upgrade(oldVersion,newVersion);

    	tagsDao.upgrade(oldVersion, newVersion);
        versionsDao.upgrade( oldVersion, newVersion);
        onCreate(database);
    }

    public int getCollectionsVersion()
    {
        return versionsDao.getVersion(VERSION_COLLECTIONS);
    }
    
    public void setCollectionsVersion(int version)
    {
        versionsDao.setVersion(VERSION_COLLECTIONS, version);
    }

    public void updateCollections(List<CollectionEntity> collections)
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

	public void deleteCollections(List<String> keys)
    {
        collectionsDao.deleteForKeys(keys);

        for( ZoteroStorageListener listener : listeners )
        {
            listener.onCollectionsUpdated();
        }
	}

    public void deleteItems(List<String> keys)
    {
        itemsDao.deleteForKeys(keys);

        for( ZoteroStorageListener listener : listeners )
        {
            listener.onItemsUpdated();
        }
    }

    public void deleteTags(List<String> tags)
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

	public void updateItems(List<ItemEntity> items)
	{
		SQLiteDatabase database = getWritableDatabase();
        assert database != null;

		for(ItemEntity item : items)
		{
            for(CollectionEntity col : item.getCollections())
            {
                collectionsDao.refresh(col);
            }

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

        for( ZoteroStorageListener listener : listeners )
        {
            listener.onItemsUpdated();
        }
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
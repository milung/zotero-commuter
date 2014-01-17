package sk.mung.sentience.zoterocommuter.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.SyncStatus;


class CollectionsDao extends BaseKeyDao<CollectionEntity> implements ZoteroStorageListener
{
    private final static String TABLE = "collections";

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PARENT = "parent";
    public static final String[] SELECT_COLUMNS = new String[]{COLUMN_KEY, COLUMN_NAME, COLUMN_VERSION, COLUMN_SYNCED, COLUMN_PARENT, COLUMN_ID};

    public final ItemsDao itemsDao;
    public CollectionsDao(ZoteroStorageImpl.DatabaseConnection sqlite, QueryDictionary queries, ItemsDao itemsDao)
    {
        super(sqlite, queries);
        this.itemsDao = itemsDao;
    }

    @Override
    public void upgrade(int oldVersion, int newVersion)
    {
        super.upgrade(oldVersion, newVersion);
        dropTable();
    }

    @Override
    public String getTable(){ return TABLE;}

    @Override
    public void createTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL( getQueries().createCollectionsTable() );
    }

    @Override
    public void upsert( CollectionEntity entity )
    {
        upsertByKey( entity);
    }

    @Override
    protected String[] getSelectColumns() { return SELECT_COLUMNS;}

    @Override
    protected CollectionEntity createEntity()
    {
        return new CollectionLazyProxy(itemsDao);
    }

    public ZoteroCollection getCollectionTree()
    {
        ZoteroCollection root = getEmptyLibrary();


        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(
                getTable(), getSelectColumns(),
                getSyncFilter(),
                null, null, null, null );

            Map<String, ZoteroCollection> flatList = new HashMap<String,ZoteroCollection>();
        try
        {
            cursor.moveToFirst();

            while(!cursor.isAfterLast())
            {
                CollectionEntity entity = cursorToEntity(cursor);
                ZoteroCollection treeEntry = new ZoteroCollection();
                treeEntry.setId(entity.getId());
                treeEntry.setKey(entity.getKey());
                treeEntry.setParentKey(entity.getParentKey());
                treeEntry.setName(entity.getName());
                treeEntry.setVersion(entity.getVersion());

                flatList.put(entity.getKey(), treeEntry);
                cursor.moveToNext();
            }
        }
        finally
        {
            cursor.close();
        }


        for(ZoteroCollection entry : flatList.values())
        {
            String parentKey = entry.getParentKey();
            ZoteroCollection parent = root;
            if( parentKey != null && flatList.containsKey(parentKey))
            {
                parent = flatList.get(parentKey);
            }
            parent.getChildren().add(entry);
        }

        root.updateLevels();
        root.sort();
        return root;
    }

    static ZoteroCollection getEmptyLibrary() {
        ZoteroCollection root = new ZoteroCollection();
        root.setName("My Library");
        root.setKey("library");
        return root;
    }

    @Override
    protected void cursorToEntity(Cursor cursor, CollectionEntity entry)
    {
        entry.setKey(cursor.getString(0));
        entry.setName(cursor.getString(1));
        entry.setVersion(cursor.getInt(2));
        entry.setSynced(SyncStatus.fromStatusCode(cursor.getInt(3)));
        entry.setParentKey(cursor.getString(4));
        entry.setId(cursor.getInt(5));
    }

    @Override
    protected ContentValues entityToValues(CollectionEntity entity)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY, entity.getKey());
        values.put(COLUMN_NAME, entity.getName());
        values.put(COLUMN_VERSION, entity.getVersion());
        values.put(COLUMN_PARENT, entity.getParentKey());
        values.put(COLUMN_SYNCED, entity.getSynced().getStatusCode());
        return values;
    }

    @Override
    public CollectionEntity findById(long id)
    {
        if(id==0)
        {
            CollectionEntity library =  new CollectionLazyProxy(itemsDao);
            library.setId(0);
            return library;
        }
        else return super.findById(id);
    }


    public List<CollectionEntity> findByItem(Item item)
    {
        Cursor c = getReadableDatabase().rawQuery(
                getQueries().getItemCollections(),
                new String[]{ Long.toString(item.getId())});
        try
        {
            return cursorToEntities( c );
        }
        finally
        {
            c.close();
        }
    }

    @Override
    public void onCollectionsUpdated()
    {
        clearCaches();
    }

    @Override
    public void onItemsUpdated()
    {
        clearCaches();
    }

    @Override
    public void onTagsUpdated()
    {
        clearCaches();
    }
}

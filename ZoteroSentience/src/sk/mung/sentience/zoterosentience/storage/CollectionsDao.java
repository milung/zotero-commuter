package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

import sk.mung.sentience.zoteroapi.entities.CollectionEntity;


class CollectionsDao extends BaseKeyDao<CollectionEntity>
{
    private final static String TABLE = "collections";

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PARENT = "parent";
    public static final String[] SELECT_COLUMNS = new String[]{COLUMN_KEY, COLUMN_NAME, COLUMN_VERSION, COLUMN_SYNCED, COLUMN_PARENT, COLUMN_ID};

    public final ItemsDao itemsDao;
    public CollectionsDao(ZoteroStorage.DatabaseConnection sqlite, QueryDictionary queries, ItemsDao itemsDao)
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
        long id = upsertByKey( entity);
        entity.setId(id);
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
                null,null, null, null, null );

        Map<String, ZoteroCollection> flatList = new HashMap<String,ZoteroCollection>();
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
        root.setSynced(true);
        return root;
    }

    @Override
    protected void cursorToEntity(Cursor cursor, CollectionEntity entry)
    {

        entry.setKey(cursor.getString(0));
        entry.setName(cursor.getString(1));
        entry.setVersion(cursor.getInt(2));
        entry.setSynced(cursor.getInt(3) != 0);
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
        values.put(COLUMN_SYNCED, 1);
        return values;
    }

}
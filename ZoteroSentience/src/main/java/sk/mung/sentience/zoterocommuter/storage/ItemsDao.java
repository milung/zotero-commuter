package sk.mung.sentience.zoterocommuter.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Creator;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemEntity;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.Relation;
import sk.mung.zoteroapi.entities.SyncStatus;
import sk.mung.zoteroapi.entities.Tag;

public class ItemsDao extends BaseKeyDao<Item> implements ZoteroStorageListener
{
    private static final String COLUMN_PARENT = "parent";
    static final String COLUMN_ITEM = "item";
    private static final String COLUMN_CREATOR = "creator";
    private static final String COLUMN_TAG = "tag";
    private static final String COLUMN_COLLECTION = "collection";

    static final String TABLE_ITEMS = "items";
    private static final String TABLE_ITEMS_TO_CREATORS = "items_to_creators";
    static final String TABLE_ITEMS_TO_TAGS = "items_to_tags";
    private static final String TABLE_ITEMS_TO_COLLECTIONS = "items_to_collections";
    public static final String SYNC_FILTER = "{SYNC_FILTER}";

    private final CreatorsDao creatorsDao;
    private final TagsDao tagsDao;
    private final FieldsDao fieldsDao;
    private CollectionsDao collectionsDao;

    private final RelationsDao relationsDao;

    public ItemsDao(
            ZoteroStorageImpl.DatabaseConnection sqlite,
            QueryDictionary queries,
            CreatorsDao creatorsDao,
            TagsDao tagsDao,
            FieldsDao fieldsDao,
            RelationsDao relationsDao)
    {
        super(sqlite, queries);
        this.creatorsDao = creatorsDao;
        this.tagsDao = tagsDao;
        this.fieldsDao = fieldsDao;
        this.relationsDao = relationsDao;
    }

    @Override
    public String getTable()
    {
        return TABLE_ITEMS;
    }

    @Override
    public void createTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL( getQueries().createItemsTable() );
        database.execSQL( getQueries().createItemsToCreatorsTable() );
        database.execSQL( getQueries().createItemsToTagsTable() );
        database.execSQL( getQueries().createItemsToCollectionsTable() );
    }

    @Override
    public void upgrade(int oldVersion, int newVersion)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS_TO_TAGS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS_TO_CREATORS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS_TO_COLLECTIONS);
        super.upgrade(oldVersion, newVersion);
        dropTable();
    }

    @Override
    public void upsert(Item entity)
    {
        upsertByKey(entity);
        updateItemCreators(entity);
        updateTags(entity);
        updateFields(entity);
        updateItemCollections(entity);
        updateRelations(entity);
    }

    private void updateRelations(Item entity)
    {
        for( Relation relation : entity.getRelations() )
        {
            relationsDao.upsert(relation);
        }
    }

    private void updateFields( Item item) {
        for(Field field : item.getFields())
        {
            fieldsDao.upsert(field);
        }
    }
    private void updateItemCollections(Item item)
    {
        List<CollectionEntity> collections = item.getCollections(); // remember before cleaning
        getWritableDatabase().delete(
                TABLE_ITEMS_TO_COLLECTIONS,
                COLUMN_ITEM + QUESTION_MARK,
                new String[]{Long.toString(item.getId())});

        for(CollectionEntity collectionKey: collections)
        {
            ContentValues collectionValues = new ContentValues();
            collectionValues.put(COLUMN_ITEM, item.getId());
            collectionValues.put(COLUMN_COLLECTION, collectionKey.getId());

            getWritableDatabase().insertWithOnConflict(
                    TABLE_ITEMS_TO_COLLECTIONS,
                    null,
                    collectionValues,
                    SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private void updateItemCreators( Item item)
    {
        List<Creator> creators = item.getCreators(); // remember before cleaning db relations
        SQLiteDatabase database = getWritableDatabase();
        database.delete(
                TABLE_ITEMS_TO_CREATORS,
                COLUMN_ITEM + QUESTION_MARK,
                new String[]{Long.toString(item.getId())});

        for(Creator creator: creators)
        {
            creatorsDao.upsert(creator);

            ContentValues itemCreatorValues = new ContentValues();
            itemCreatorValues.put(COLUMN_ITEM, item.getId());
            itemCreatorValues.put(COLUMN_CREATOR, creator.getId());

            database.insertWithOnConflict(
                    TABLE_ITEMS_TO_CREATORS, null, itemCreatorValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private void updateTags(Item item)
    {
        List<Tag> tags = item.getTags(); // remember before cleaning
        getWritableDatabase().delete(
                TABLE_ITEMS_TO_TAGS,
                COLUMN_ITEM + QUESTION_MARK,
                new String[]{Long.toString(item.getId())});

        for( Tag tag : tags )
        {
            tagsDao.upsert(tag);
            ContentValues itemTagValues = new ContentValues();
            itemTagValues.put(COLUMN_ITEM, item.getId());
            itemTagValues.put(COLUMN_TAG, tag.getId());

            getWritableDatabase().insertWithOnConflict(
                    TABLE_ITEMS_TO_TAGS, null, itemTagValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }


    @Override
    protected String[] getSelectColumns()
    {
        return new String[]{COLUMN_ID, COLUMN_KEY, COLUMN_VERSION, COLUMN_TYPE, COLUMN_SYNCED, COLUMN_TITLE, COLUMN_PARENT};
    }

    @Override
    protected Item createEntity()
    {
        return new ItemLazyProxy(new ItemEntity(),creatorsDao,this, fieldsDao, tagsDao, collectionsDao);
    }

    @Override
    protected void cursorToEntity(Cursor cursor, Item item)
    {
        item.setId( cursor.getLong(0));
        item.setKey( cursor.getString(1));
        item.setVersion( cursor.getInt(2));
        item.setItemType(ItemType.valueWithId(cursor.getInt(3)));
        item.setSynced(SyncStatus.fromStatusCode(cursor.getInt(4)));
        item.setTitle(cursor.getString(5));
        item.setParentKey(idToKey(cursor.getLong(6)));
    }

    @Override
    protected ContentValues entityToValues(Item item)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY, item.getKey());
        values.put(COLUMN_TYPE, item.getItemType().getId());
        values.put(COLUMN_VERSION, item.getVersion());
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_SYNCED, item.getSynced().getStatusCode());

        if( item.getParentKey()!= null)
        {
            values.put(COLUMN_PARENT, getOrCreateParent( item.getParentKey()).getId());
        }
        return values;
    }

    private Item getOrCreateParent( String parentKey) {
        Item parent = findByKey(parentKey);
        if(parent == null)
        {
            parent = new ItemEntity();
            parent.setTitle("<missing parent>");
            parent.setKey(parentKey);
            parent.setSynced(SyncStatus.SYNC_OK); // do not update by mistake
            upsert(parent);
        }

        return parent;

    }

    public List<Item> findByCollection(CollectionEntity collection)
    {
        String query = getQueries().getLibraryItems();

        String[] selectionParams = null;
        if(collection!=null && collection.getId() > 0)
        {
            query = getQueries().getCollectionItems();
            selectionParams = new String[]{Long.toString(collection.getId())};
        }

        query= query.replace(SYNC_FILTER, getSyncFilter());
        Cursor cursor = getReadableDatabase().rawQuery(query, selectionParams);
        try
        {
            return cursorToEntities(cursor);
        }
        finally
        {
            cursor.close();
        }
    }

    public Cursor cursorByCollectionId(long collectionId)
    {
        String query = getQueries().getLibraryItems();

        String[] selectionParams = null;
        if(collectionId > 0)
        {
            query = getQueries().getCollectionItems();
            selectionParams = new String[]{Long.toString(collectionId)};
        }

        query= query.replace(SYNC_FILTER, getSyncFilter());

        return getReadableDatabase().rawQuery(query, selectionParams);

    }

    public List<Item> findByParent(Item parent)
    {
        SQLiteDatabase database = getReadableDatabase();
        assert database != null;

        String query = getQueries().getChildrenItems();
        query= query.replace(SYNC_FILTER, getSyncFilter());
        Cursor cursor =database.rawQuery(query, new String[]{Long.toString(parent.getId())});
        try
        {
            return cursorToEntities(cursor);
        }
        finally
        {
            cursor.close();
        }
    }

    @Override
    public void onCollectionsUpdated() {}

    @Override
    public void onItemsUpdated()
    {
        clearCaches();
    }

    @Override
    public void onTagsUpdated() {}

    @Override
    public void delete( Item entity) {
        for(Field field : entity.getFields())
        {
            fieldsDao.delete(field);
        }
        super.delete(entity);
    }

    public void setCollectionsDao(CollectionsDao collectionsDao) {
        this.collectionsDao = collectionsDao;
    }

    public Cursor cursorByTagLabel(String tagLabel)
    {
        String query = getQueries().getTaggedItems();

        String[] selectionParams = new String[]{tagLabel};

        query= query.replace(SYNC_FILTER, getSyncFilter());

        return getReadableDatabase().rawQuery(query, selectionParams);
    }
}

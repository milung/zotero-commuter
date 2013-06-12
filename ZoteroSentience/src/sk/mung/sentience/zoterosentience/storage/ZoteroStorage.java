package sk.mung.sentience.zoterosentience.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.mung.sentience.zoteroapi.CollectionEntity;
import sk.mung.sentience.zoteroapi.ZoteroCollection;
import sk.mung.sentience.zoteroapi.items.Creator;
import sk.mung.sentience.zoteroapi.items.CreatorType;
import sk.mung.sentience.zoteroapi.items.Item;
import sk.mung.sentience.zoteroapi.items.ItemEntity;
import sk.mung.sentience.zoteroapi.items.ItemField;
import sk.mung.sentience.zoteroapi.items.ItemType;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ZoteroStorage extends SQLiteOpenHelper
{
    private static final String INTEGER = " INTEGER ";
	private static final String END_AND = " ), ";
	private static final String AND = " , ";
	private static final String END = " ) ";
	private static final String START = " ( ";
	private static final String UNIQUE_START = "UNIQUE (";
	private static final String UNIQUE_TEXT = " TEXT UNIQUE ";
	private static final String QUESTION_MARK = "=?";
	private static final String CREATE_TABLE = "CREATE TABLE ";
	private static final String REFERENCES = " INTEGER REFERENCES ";
	private static final String INTEGER_AND = " INTEGER, ";
	private static final String TEXT_AND = " TEXT, ";
	private static final String CONFLICT_REPLACE = "ON CONFLICT REPLACE";
	private static final String CONFLICT_IGNORE = "ON CONFLICT IGNORE";
	private static final String PRIMARY_KEY = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
	private static final String VERSION_COLLECTIONS = "collections";
	private static final String COLUMN_QUERY = "query";
	private static final String COLUMN_PARENT = "parent";
	private static final String COLUMN_NAME = "name";
	private static final String VERSION_DELETIONS = "deletions";
	private static final String COLUMN_TITLE = "title";
	private static final String COLUMN_VERSION = "version";
	private static final String COLUMN_PERSON = "person";
	private static final String COLUMN_CREATOR = "creator";
	private static final String COLUMN_SYNCED = "synced";
	private static final String COLUMN_KEY = "key";
	private static final String COLUMN_TAG = "tag";
	private static final String COLUMN_VALUE = "value";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_COLLECTION = "collection";
	private static final String COLUMN_ITEM = "item";
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_SHORT_NAME = "shortName";
	private static final String COLUMN_LAST_NAME = "lastName";
	private static final String COLUMN_FIRST_NAME = "firstName";
	private static final String TABLE_ITEMS_TO_COLLECTIONS = "items_to_collections";
	private static final String TABLE_ITEMS_TO_TAGS = "items_to_tags";
	private static final String TABLE_ITEMS_TO_CREATORS = "items_to_creators";
    private static final String TABLE_ITEMS = "items";
	private static final String TABLE_FIELDS = "fields";
	private static final String TABLE_CREATORS = "creators";
	private static final String TABLE_PERSONS = "persons";
	private static final String TABLE_TAGS = "tags";
	static final String TABLE_COLLECTIONS = "collections";
    static final String TABLE_VERSIONS = "query_versions";
    private static final String DATABASE_NAME = "ZoteroStorage";
    private static final int DATABASE_VERSION = 7;
    public static final String VERSION_ITEMS = "items";

    private List<ZoteroStorageListener> listeners = new ArrayList<ZoteroStorageListener>();
    private final QueryDictionary queries;
    
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
        this.queries = queries;
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL( queries.createCollectionsTable() );
        database.execSQL( queries.createVersionsTable() );
        database.execSQL( queries.createTagsTable() );
        database.execSQL( queries.createPersonsTable() );
        database.execSQL( queries.createCreatorsTable() );
        database.execSQL( queries.createItemsTable() );
        database.execSQL( queries.createFieldsTable() );
        database.execSQL( queries.createItemsToCreatorsTable() );
        database.execSQL( queries.createItemsToTagsTable() );
        database.execSQL( queries.createItemsToCollectionsTable() );
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {       
    	database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS_TO_COLLECTIONS);
    	database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS_TO_TAGS);
    	database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS_TO_CREATORS);
    	database.execSQL("DROP TABLE IF EXISTS " + TABLE_FIELDS);
    	database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
    	database.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONS);
    	database.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECTIONS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSIONS);
        
        onCreate(database);
    }

    public int getCollectionsVersion()
    {
        return getVersion(VERSION_COLLECTIONS);
    }

	private int getVersion(String queryType) {
		Cursor cursor = getReadableDatabase().query(
                TABLE_VERSIONS, 
                new String[]{ COLUMN_VERSION}, 
                COLUMN_QUERY + QUESTION_MARK, 
                new String[] {queryType}, 
                null, null, null,null );
        if(cursor.getCount() > 0)
        {
        	cursor.moveToFirst();
        	return cursor.getInt(0);
        }
        else return 0;
	}
    
    public void setCollectionsVersion(int version)
    {
        setVersion(VERSION_COLLECTIONS, version );
    }

	private void setVersion( String queryType, int version) {
		ContentValues values = new ContentValues();
        values.put(COLUMN_QUERY,queryType);
        values.put(COLUMN_VERSION,version);
        
        getWritableDatabase().insert(TABLE_VERSIONS, null, values);
	}

    public void updateCollections(List<CollectionEntity> collections)
    {
        SQLiteDatabase database = getWritableDatabase();
        for( CollectionEntity col : collections)
        {
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_KEY, col.getKey());
            values.put(COLUMN_NAME, col.getName());
            values.put(COLUMN_VERSION, col.getVersion());
            values.put(COLUMN_PARENT, col.getParentKey());
            values.put(COLUMN_SYNCED, 1);   
            
            database.insertWithOnConflict(
                    TABLE_COLLECTIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        
        for( ZoteroStorageListener listener : listeners )
        {
            listener.onCollectionsUpdated();
        }
    }

	
	public ZoteroCollection getCollectionTree()
	{
		ZoteroCollection root = getEmptyLibrary();
		
		Cursor cursor = getReadableDatabase().query(
                TABLE_COLLECTIONS, 
                new String[]{ COLUMN_KEY, COLUMN_NAME, COLUMN_VERSION, COLUMN_SYNCED, COLUMN_PARENT, COLUMN_ID }, 
                null,null, null, null, null );
		Map<String, ZoteroCollection> flatList = new HashMap<String,ZoteroCollection>();
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			ZoteroCollection entry = new ZoteroCollection();
			entry.setKey(cursor.getString(0));
			entry.setName(cursor.getString(1));
			entry.setVersion(cursor.getInt(2));
			entry.setSynced(cursor.getInt(3) != 0);
			entry.setParentKey(cursor.getString(4));
			entry.setId(cursor.getInt(5));
			flatList.put(entry.getKey(), entry);
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

	public static ZoteroCollection getEmptyLibrary() {
		ZoteroCollection root = new ZoteroCollection();
		root.setName("My Library");
		root.setKey("library");
		root.setSynced(true);
		return root;
	}

	public int getDeletionsVersion() 
	{
		return getVersion(VERSION_DELETIONS);
	}

	public void setDeletionsVersion(int version) {
		setVersion(VERSION_DELETIONS, version);
	}

	public void deleteCollections(List<String> collections) {
		
		SQLiteDatabase database = getWritableDatabase();
        for( String col : collections)
        {
            database.delete(
                    TABLE_COLLECTIONS, COLUMN_KEY + QUESTION_MARK, new String[]{ col });
        }
        
        for( ZoteroStorageListener listener : listeners )
        {
            listener.onCollectionsUpdated();
        }
		
	}

	public void updateItems(List<ItemEntity> items)
	{
		SQLiteDatabase database = getWritableDatabase();
		for(ItemEntity item : items)
		{
			updateItem( database, item );
		}

        for( ZoteroStorageListener listener : listeners )
        {
            listener.onItemsUpdated();
        }
	}

	private void updateItem(SQLiteDatabase database, ItemEntity item)
	{
		ContentValues values = new ContentValues();
		values.put(COLUMN_KEY, item.getKey());
		values.put(COLUMN_TYPE, item.getItemType().getId());
		values.put(COLUMN_VERSION, item.getVersion());
		values.put(COLUMN_TITLE, item.getTitle());
		values.put(COLUMN_SYNCED, item.isSynced() ? 1 : 0);
		
		if( item.getParentKey()!= null)
		{
			values.put(COLUMN_PARENT, getOrCreateParentId(database, item.getParentKey()));
		}
		long itemId = database.insertOrThrow(TABLE_ITEMS, null, values);
		
		updateItemTags(database, item, itemId);
		updateItemFields(database, item, itemId);		
		updateItemCollections(database, item, itemId);
		updateItemCreators(database, item, itemId);
	}

	private void updateItemCreators(SQLiteDatabase database, ItemEntity item,
			long itemId) {
		for(Creator creator: item.getCreators())
		{
			ContentValues personValues = new ContentValues();
			personValues.put(COLUMN_FIRST_NAME, creator.getFirstName());
			personValues.put(COLUMN_LAST_NAME, creator.getLastName());
			personValues.put(COLUMN_SHORT_NAME, creator.getShortName());
			
			long personId = database.insertWithOnConflict(
					TABLE_PERSONS, 
					null, 
					personValues, 
					SQLiteDatabase.CONFLICT_IGNORE );
			
			ContentValues creatorValues = new ContentValues();
			creatorValues.put(COLUMN_TYPE, creator.getType().getId() );
			creatorValues.put(COLUMN_PERSON, personId);
			
			long creatorId = database.insertWithOnConflict(
					TABLE_CREATORS, 
					null, 
					creatorValues, 
					SQLiteDatabase.CONFLICT_IGNORE);
			
			ContentValues itemCreatorValues = new ContentValues();
			itemCreatorValues.put(COLUMN_ITEM, itemId);
			itemCreatorValues.put(COLUMN_CREATOR, creatorId);
			
			database.insertWithOnConflict(TABLE_ITEMS_TO_CREATORS, null, itemCreatorValues, SQLiteDatabase.CONFLICT_IGNORE);
		}
	}

	private void updateItemCollections(
			SQLiteDatabase database, 
			ItemEntity item,
			long itemId) 
	{
		for(String collectionKey: item.getCollectionKeys())
		{
			Cursor cursor = database.query(
					TABLE_COLLECTIONS,
					new String[] {COLUMN_ID},
					COLUMN_KEY + QUESTION_MARK, 
					new String[]{collectionKey},
					null,null,null);
			
			if(cursor.getCount()>0)
			{
				cursor.moveToFirst();
				long collectionId  = cursor.getInt(0);
				
				ContentValues collectionValues = new ContentValues();
				collectionValues.put(COLUMN_ITEM, itemId);
				collectionValues.put(COLUMN_COLLECTION, collectionId);
				
				database.insertWithOnConflict(
						TABLE_ITEMS_TO_COLLECTIONS, 
						null, 
						collectionValues, 
						SQLiteDatabase.CONFLICT_IGNORE);
			}
		}
	}

	private void updateItemFields(SQLiteDatabase database, ItemEntity item,
			long itemId) {
		for(Map.Entry<ItemField,String> entry : item.getFields().entrySet())
		{
			ContentValues fieldValues = new ContentValues();
			fieldValues.put(COLUMN_ITEM, itemId);
			fieldValues.put(COLUMN_TYPE, entry.getKey().getId());
			fieldValues.put(COLUMN_VALUE, entry.getValue());
			
			database.insertWithOnConflict(TABLE_FIELDS, null, fieldValues, SQLiteDatabase.CONFLICT_REPLACE);
		}
	}

	private void updateItemTags(SQLiteDatabase database, ItemEntity item, long itemId) {
		Collection<Long> tagIds = getTagIds( database, item.getTags());
		for( long tagId : tagIds )
		{
			ContentValues itemTagValues = new ContentValues();
			itemTagValues.put(COLUMN_ITEM, itemId);
			itemTagValues.put(COLUMN_TAG, tagId);
			
			database.insertWithOnConflict(TABLE_ITEMS_TO_TAGS, null, itemTagValues, SQLiteDatabase.CONFLICT_IGNORE);
		}
	}

	private Collection<Long> getTagIds(SQLiteDatabase database, List<String> tags) 
	{
		List<Long> ids = new ArrayList<Long>();
		for( String tag : tags)
		{
			ContentValues values = new ContentValues();
			values.put(COLUMN_TAG, tag);
			long tagId =  database.insertWithOnConflict(TABLE_TAGS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			ids.add(tagId);
		}
		return ids;
	}

	private long getOrCreateParentId(SQLiteDatabase database, String parentKey) {
		Cursor cursor = database.query(TABLE_ITEMS,new String[] {COLUMN_ID},COLUMN_KEY + QUESTION_MARK, new String[]{parentKey},null,null,null);
		long parentId;
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			parentId = cursor.getInt(0);
		}
		else // put shallow entry so we can create valid reference to future item
		{
			ContentValues parentValues = new ContentValues();
			parentValues.put(COLUMN_KEY, parentKey);
			parentValues.put(COLUMN_SYNCED, -1);
			
			parentId = database.insertOrThrow(TABLE_ITEMS, null, parentValues);
		}
		return parentId;
	}

    public int getItemsVersion()
    {
        return getVersion(VERSION_ITEMS);
    }

    public void setItemsVersion(int version)
    {
        setVersion(VERSION_ITEMS, version);
    }

    public List<Item> getItems(Long collectionId)
    {
        SQLiteDatabase database = getReadableDatabase();
        String query = queries.getLibraryItems();

        String[] selectionParams = null;
        if(collectionId!=null && collectionId > 0)
        {
            query = queries.getCollectionItems();
            selectionParams = new String[]{collectionId.toString()};
        }

        assert database != null;
        Cursor cursor = database.rawQuery(query,selectionParams);

        List<Item> items = new ArrayList<Item>();
        while(cursor.moveToNext())
        {
            ItemEntity item = new ItemEntity();
            item.setId(cursor.getLong(0));
            item.setKey( cursor.getString(1));
            item.setTitle( cursor.getString(2));
            item.setItemType(ItemType.valueWithId(cursor.getInt(3)));

            items.add(new ItemLazyProxy(item, this));
        }

        return items;
    }

    public List<Creator> getItemCreators(long itemId)
    {
        SQLiteDatabase database = getReadableDatabase();
        assert database != null;
        Cursor cursor = database.rawQuery(queries.getItemCreators(), new String [] {Long.toString(itemId)});

        List<Creator> creators = new ArrayList<Creator>();
        while(cursor.moveToNext())
        {
            Creator creator = new Creator();
            creator.setId(cursor.getLong(0));
            creator.setType( CreatorType.forId(cursor.getInt(1)));
            creator.setFirstName( cursor.getString(2));
            creator.setLastName( cursor.getString(3));

            creators.add(creator);
        }
        return creators;
    }
}

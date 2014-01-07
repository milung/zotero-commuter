package sk.mung.sentience.zoterocommuter.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemEntity;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.SyncStatus;

public class FieldsDao extends BaseDao<Field>
{
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_ITEM = "item";

    public FieldsDao(ZoteroStorageImpl.DatabaseConnection databaseConnection, QueryDictionary queries)
    {
        super(databaseConnection, queries);
    }

    @Override
    public String getTable()
    {
        return "fields";
    }

    @Override
    public void createTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL( getQueries().createFieldsTable() );
    }

    @Override
    public void upgrade(int oldVersion, int newVersion)
    {
        super.upgrade(oldVersion, newVersion);
        dropTable();
    }

    @Override
    public void upsert(Field entity)
    {
        ContentValues fieldValues = entityToValues(entity);
        SQLiteDatabase database = getWritableDatabase();
        long rowId =database.insertWithOnConflict(getTable(), null, fieldValues, SQLiteDatabase.CONFLICT_IGNORE);
        if( 0 > rowId)
        {
            database.update(getTable(),fieldValues, COLUMN_ITEM + QUESTION_MARK + " AND " + COLUMN_TYPE + QUESTION_MARK,
                    new String[]{Long.toString(entity.getItem().getId()), Integer.toString(entity.getType().getId())});
            rowId = searchIdOfEntity(entity);
        }
        entity.setId(rowId);
    }

    @Override
    protected String[] getSelectColumns()
    {
        return new String[]{COLUMN_ID, COLUMN_ITEM,COLUMN_TYPE,COLUMN_VALUE};
    }

    @Override
    protected Field createEntity()
    {
        return new FieldLazyProxy(this);
    }

    @Override
    protected void cursorToEntity(Cursor cursor, Field entity)
    {
        entity.setType(ItemField.fromId(cursor.getInt(2)));
        entity.setValue(cursor.getString(3));
        Item item = new ItemEntity();
        entity.setItem(item);
        item.setId(cursor.getLong(1));
        entity.setId(cursor.getLong(0));
    }

    @Override
    protected ContentValues entityToValues(Field entity)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM, entity.getItem().getId());
        values.put(COLUMN_TYPE,entity.getType().getId());
        values.put(COLUMN_VALUE, entity.getValue());
        return values;
    }

    public List<Field> findByItem(Item item)
    {
        Cursor c = getReadableDatabase().query(
                getTable(),
                getSelectColumns(),
                COLUMN_ITEM+QUESTION_MARK,
                new String[]{ Long.toString(item.getId())},
                null,null,null);
        try
        {
            List<Field> fields = cursorToEntities( c );
            for(Field field: fields)
            {
                field.setItem(item);
            }
            return fields;
        }
        finally
        {
            c.close();
        }
    }

    @Override
    public void update(Field field)
    {
        super.update(field);

        // reset synced flag on the associated item
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNCED, SyncStatus.SYNC_LOCALLY_UPDATED.getStatusCode());
        getWritableDatabase().update(
                ItemsDao.TABLE_ITEMS,
                values,
                COLUMN_ID + QUESTION_MARK,
                new String[]{ Long.toString(field.getItem().getId())});
    }
}

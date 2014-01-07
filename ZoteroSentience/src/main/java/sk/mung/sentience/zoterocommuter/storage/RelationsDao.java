package sk.mung.sentience.zoterocommuter.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemEntity;
import sk.mung.zoteroapi.entities.Relation;
import sk.mung.zoteroapi.entities.SyncStatus;

public class RelationsDao extends BaseDao<Relation>
{
    private static final String COLUMN_SUBJECT = "subject";
    private static final String COLUMN_PREDICATE = "predicate";
    private static final String COLUMN_OBJECT = "object";

    public RelationsDao(ZoteroStorageImpl.DatabaseConnection databaseConnection, QueryDictionary queries)
    {
        super(databaseConnection, queries);
    }

    @Override
    public String getTable()
    {
        return "relations";
    }

    @Override
    public void createTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL( getQueries().createRelationsTable() );
    }

    @Override
    public void upgrade(int oldVersion, int newVersion)
    {
        super.upgrade(oldVersion, newVersion);
        dropTable();
    }

    @Override
    public void upsert(Relation entity)
    {
        ContentValues fieldValues = entityToValues(entity);
        SQLiteDatabase database = getWritableDatabase();
        long rowId =database.insertWithOnConflict(getTable(), null, fieldValues, SQLiteDatabase.CONFLICT_IGNORE);
        if( 0 > rowId)
        {
            database.update(
                    getTable(), fieldValues,
                    COLUMN_SUBJECT + QUESTION_MARK + " AND " + COLUMN_PREDICATE + QUESTION_MARK,
                    new String[]{
                            Long.toString(entity.getSubject().getId()),
                            entity.getPredicate()});
            rowId = searchIdOfEntity(entity);
        }
        entity.setId(rowId);
    }

    @Override
    protected String[] getSelectColumns()
    {
        return new String[]{COLUMN_ID, COLUMN_SUBJECT,COLUMN_PREDICATE,COLUMN_OBJECT};
    }

    @Override
    protected Relation createEntity()
    {
        return new Relation();
    }

    @Override
    protected void cursorToEntity(Cursor cursor, Relation entity)
    {
        entity.setPredicate(cursor.getString(2));
        entity.setObject(cursor.getString(3));
        Item item = new ItemEntity();
        entity.setSubject(item);
        item.setId(cursor.getLong(1));
        entity.setId(cursor.getLong(0));
    }

    @Override
    protected ContentValues entityToValues(Relation entity)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBJECT, entity.getSubject().getId());
        values.put(COLUMN_PREDICATE,entity.getPredicate());
        values.put(COLUMN_OBJECT, entity.getObject());
        return values;
    }

    public List<Relation> findByItem(Item item)
    {
        Cursor c = getReadableDatabase().query(
                getTable(),
                getSelectColumns(),
                COLUMN_SUBJECT+QUESTION_MARK,
                new String[]{ Long.toString(item.getId())},
                null,null,null);
        try
        {
            List<Relation> relations = cursorToEntities( c );
            for(Relation relation: relations)
            {
                relation.setSubject(item);
            }
            return relations;
        }
        finally
        {
            c.close();
        }
    }

    @Override
    public void update(Relation relation)
    {
        super.update(relation);

        // reset synced flag on the associated item
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNCED, SyncStatus.SYNC_LOCALLY_UPDATED.getStatusCode());
        getWritableDatabase().update(
                ItemsDao.TABLE_ITEMS,
                values,
                COLUMN_ID + QUESTION_MARK,
                new String[]{ Long.toString(relation.getSubject().getId())});
    }
}

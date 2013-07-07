package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoteroapi.entities.KeyEntity;
import sk.mung.sentience.zoteroapi.entities.SyncStatus;


abstract class BaseKeyDao<T extends KeyEntity> extends BaseDao<T>
{
    protected static final String COLUMN_KEY = "key";
    public static final String CONFLICTED_KEY_PREFIX = "conflicted_";

    public BaseKeyDao(ZoteroStorage.DatabaseConnection databaseConnection, QueryDictionary queries)
    {
        super(databaseConnection, queries);
    }

    @Override
    public void refresh(T entity)
    {
        long id = entity.getId();
        if(id > 0)
        {
            super.refresh(entity);
        }
        else
        {
            String key = entity.getKey();
            if(key != null)
            {
                Cursor cursor = getReadableDatabase().query(
                        getTable(),
                        getSelectColumns(),
                        COLUMN_KEY+QUESTION_MARK,
                        new String[]{ key },
                        null,null,null);

                if(cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    cursorToEntity(cursor, entity);
                }
                else
                {
                    throw new IllegalArgumentException("entity with given key does not exists");
                }
            }
            else throw new IllegalArgumentException("neither id or key is set");
        }
    }


    public void deleteForKeys( List<String> keys) {
        SQLiteDatabase database = getWritableDatabase();
        for( String key : keys)
        {
            database.delete(
                    getTable(), COLUMN_KEY + "=?", new String[]{ key });
        }
    }

    public T findByKey(String key)
    {
        Cursor cursor = getReadableDatabase().query(
                getTable(),
                getSelectColumns(),
                COLUMN_KEY + QUESTION_MARK + " AND " +
                COLUMN_SYNCED + " <> " + Integer.toString(SyncStatus.SYNC_DELETED.getStatusCode()),
                new String[] { key },
                null,null,null);

        try
        {
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                return cursorToEntity(cursor);
            }
            else return null;
        }
        finally
        {
            cursor.close();
        }
    }

    public List<T> findBySynced(SyncStatus syncStatus)
    {
        Cursor cursor = getReadableDatabase().query(
                getTable(),
                getSelectColumns(),
                COLUMN_SYNCED + QUESTION_MARK,
                new String[] { Integer.toString(syncStatus.getStatusCode()) },
                null,null,null);

        try
        {
            List<T> entities = new ArrayList<T>();
            while(cursor.moveToNext())
            {
                entities.add(cursorToEntity(cursor));
            }
            return entities;
        }
        finally
        {
            cursor.close();
        }
    }
    protected final long upsertByKey( T entity )
    {
        SQLiteDatabase database = getWritableDatabase();
        T storedVersion = findByKey(entity.getKey());

        if(storedVersion != null && storedVersion.getSynced() != SyncStatus.SYNC_OK)
        {
            SyncStatus newStatus = SyncStatus.SYNC_CONFLICT;
            if(storedVersion.getSynced() == SyncStatus.SYNC_DELETED)
            {
                newStatus = SyncStatus.SYNC_DELETED_CONFLICT;
            }
            ContentValues values = entityToValues(entity);
            values.put(COLUMN_KEY, CONFLICTED_KEY_PREFIX + entity.getKey());
            values.put(COLUMN_SYNCED, SyncStatus.SYNC_REMOTE_VERSION.getStatusCode());
            database.insertWithOnConflict( getTable(), null, values, SQLiteDatabase.CONFLICT_REPLACE);

            ContentValues statusValues = new ContentValues();
            statusValues.put(COLUMN_SYNCED, newStatus.getStatusCode());
            database.update(
                    getTable(),
                    statusValues,
                    COLUMN_ID + QUESTION_MARK,
                    new String[]{Long.toString(storedVersion.getId())});
        }
        ContentValues values = entityToValues(entity);
        long rowId;
        if(storedVersion == null)
        {
            rowId = database.insert( getTable(), null, values);
        }
        else
        {
            values.remove(COLUMN_KEY);
            database.update(getTable(), values, COLUMN_KEY + "=?", new String[]{entity.getKey()});
            rowId = storedVersion.getId();
        }
        entity.setId(rowId);
        return rowId;
    }

    protected final long keyToId( String key)
    {

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor
                = database.query(getTable(),new String[]{COLUMN_ID},COLUMN_KEY+QUESTION_MARK,new String[]{key},null,null,null);
        cursor.moveToFirst();
        return cursor.getLong(0);
    }

    public String idToKey(Long id)
    {
        if( id != null)
        {
            SQLiteDatabase database = getReadableDatabase();
            Cursor cursor
                    = database.query(
                    getTable(),
                    new String[]{COLUMN_KEY},
                    COLUMN_ID+QUESTION_MARK,
                    new String[]{id.toString()},
                    null,null,null);

            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                return cursor.getString(0);
            }
        }
        return null;
    }

    protected String getSyncFilter()
    {
        return getTable() + "." + COLUMN_SYNCED + "<>" + Integer.toString(SyncStatus.SYNC_DELETED.getStatusCode()) + " AND " +
               getTable() + "." + COLUMN_SYNCED + "<>" + Integer.toString(SyncStatus.SYNC_DELETED_CONFLICT.getStatusCode()) + " AND " +
               getTable() + "." + COLUMN_SYNCED + "<>" + Integer.toString(SyncStatus.SYNC_REMOTE_VERSION.getStatusCode());
    }
}

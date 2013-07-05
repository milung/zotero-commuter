package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import sk.mung.sentience.zoteroapi.entities.KeyEntity;


abstract class BaseKeyDao<T extends KeyEntity> extends BaseDao<T>
{

    protected static final String COLUMN_KEY = "key";

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
                COLUMN_KEY + QUESTION_MARK,
                new String[] { key },
                null,null,null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            return cursorToEntity(cursor);
        }
        else return null;

    }

    protected final long upsertByKey( T entity )
    {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = entityToValues(entity);
        long rowId = database.insert( getTable(), null, values);
        if( 0 > rowId )
        {
            values.remove(COLUMN_KEY);
            values.remove(COLUMN_SYNCED);// do not change synced flag
            database.update(getTable(), values, COLUMN_KEY + "=?", new String[]{entity.getKey()});
            rowId = keyToId(entity.getKey());
        }
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
}

package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sk.mung.sentience.zoteroapi.entities.Entity;


public abstract class BaseDao<T extends Entity>
{

    protected static final String COLUMN_ID = "_id";
    protected static final String COLUMN_SYNCED = "synced";
    protected static final String COLUMN_TITLE = "title";
    protected static final String COLUMN_TYPE = "type";
    protected static final String COLUMN_VERSION = "version";

    protected static final String QUESTION_MARK = "=?";

    public QueryDictionary getQueries()
    {
        return queries;
    }

    private final QueryDictionary queries;
    private final ZoteroStorage.DatabaseConnection databaseConnection;

    public BaseDao(ZoteroStorage.DatabaseConnection databaseConnection,  QueryDictionary queries)
    {
        this.queries = queries;
        this.databaseConnection = databaseConnection;
    }

    protected final SQLiteDatabase getReadableDatabase() { return databaseConnection.getReadableDatabase();}
    protected final SQLiteDatabase getWritableDatabase() { return databaseConnection.getWritableDatabase();}

    public abstract String getTable();

    public abstract void createTable();

    public void upgrade(int oldVersion, int newVersion)
    {}

    public abstract void upsert( T entity);

    protected abstract String[] getSelectColumns();



    protected final T cursorToEntity(Cursor cursor)
    {
        T entity = createEntity();
        cursorToEntity(cursor, entity);
        return entity;
    }

    protected abstract T createEntity();

    protected abstract void cursorToEntity(Cursor cursor, T entity);

    protected abstract ContentValues entityToValues(T entity);



    public final void dropTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + getTable());
    }




    protected final String[] buildQueryArguments(ContentValues values)
    {
        List<String> arguments = new ArrayList<String>(values.size());
        for(Map.Entry<String,Object> entry : values.valueSet())
        {
            if(entry.getValue() != null)
            {
                arguments.add(entry.toString());
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }

    protected final String buildQuery(ContentValues values)
    {
        StringBuilder query = new StringBuilder();
        boolean isFirst = true;
        for(Map.Entry<String,Object> entry : values.valueSet())
        {
            query
                    .append( isFirst ? " " : " AND ")
                    .append(entry.getKey())
                    .append( entry.getValue() == null ? " IS NULL " : QUESTION_MARK );
            isFirst = false;
        }
        return query.toString();
    }

    public void refresh(T entity)
    {
        Cursor cursor = getReadableDatabase().query(
                getTable(),
                getSelectColumns(),
                COLUMN_ID+QUESTION_MARK,
                new String[]{ Long.toString(entity.getId())},
                null,null,null);

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            cursorToEntity(cursor, entity);
        }
        else
        {
            throw new IllegalArgumentException("entity id does not exists");
        }
    }

    protected final long searchIdOfEntity(T entity)
    {
        ContentValues values = entityToValues(entity);
        String query = buildQuery(values);
        String[] arguments = buildQueryArguments(values);
        Cursor cursor
                = getReadableDatabase().query(getTable(), new String[]{COLUMN_ID}, query, arguments, null, null, null );
        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            return cursor.getLong(0);
        }
        return -1;
    }

    protected final List<T> cursorToEntities(Cursor cursor)
    {
        List<T> entities = new ArrayList<T>();
        while(cursor.moveToNext())
        {
            entities.add(cursorToEntity(cursor));
        }
        return entities;
    }

    public T findById(long id)
    {
        T entity = createEntity();
        entity.setId(id);
        refresh(entity);
        return entity;
    }
}

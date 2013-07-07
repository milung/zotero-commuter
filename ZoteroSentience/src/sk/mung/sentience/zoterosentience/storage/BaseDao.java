package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.mung.sentience.zoteroapi.entities.Entity;


public abstract class BaseDao<T extends Entity>
{
    public void updateProperty(T entity, String columnName, Object value)
    {
        ContentValues values = new ContentValues();
        values.put(columnName,value.toString());
        getWritableDatabase().update(
          getTable(),
                values,
                COLUMN_ID + QUESTION_MARK,
                new String[] { Long.toString(entity.getId())});
    }

    interface UpdateListener
    {
        void onDataUpdated(BaseDao sender, Long entityId);
    }


    protected static final String COLUMN_ID = "_id";
    protected static final String COLUMN_SYNCED = "synced";
    protected static final String COLUMN_TITLE = "title";
    protected static final String COLUMN_TYPE = "type";
    protected static final String COLUMN_VERSION = "version";

    protected static final String QUESTION_MARK = "=?";

    private final QueryDictionary queries;
    private final ZoteroStorage.DatabaseConnection databaseConnection;
    private final Map<Long,WeakReference<T>> cache = new HashMap<Long,WeakReference<T>>();

    private List<WeakReference<UpdateListener>> listeners = new ArrayList<WeakReference<UpdateListener>>();

    public synchronized void addUpdateListener(UpdateListener listener)
    {
        listeners.add(new WeakReference<UpdateListener>(listener));
    }

    public synchronized void removeUpdateListener(UpdateListener listener)
    {
        List<WeakReference<UpdateListener>> remainingListeners
                = new ArrayList<WeakReference<UpdateListener>>(listeners.size());
        for( WeakReference<UpdateListener> ref : listeners)
        {
            UpdateListener l = ref.get();
            if(l != null && l != listener)
            {
                remainingListeners.add(ref);
            }
        }
        listeners = remainingListeners;
    }

    protected synchronized void updateListeners(Long entityId)
    {
        List<WeakReference<UpdateListener>> remainingListeners
                = new ArrayList<WeakReference<UpdateListener>>(listeners.size());
        for( WeakReference<UpdateListener> ref : listeners)
        {
            UpdateListener l = ref.get();
            if(l != null )
            {
                l.onDataUpdated(this, entityId);
                remainingListeners.add(ref);
            }
        }
        listeners = remainingListeners;
    }

    public synchronized void clearCaches()
    {
        cache.clear();
        updateListeners(null);
    }

    public QueryDictionary getQueries()
    {
        return queries;
    }

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

    protected final String expandSelectColumns()
    {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        String table = getTable()+ ".";
        for(String column : getSelectColumns())
        {
            if(!first)
            {
                builder.append (", ");
            }
            builder.append(table).append(column);
            first = false;
        }
        return builder.toString();
    }

    protected final T cursorToEntity(Cursor cursor)
    {
        long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));

        T entity = getFromCache(id);
        if(entity == null)
        {
            entity = createEntity();
            cursorToEntity(cursor, entity);
            cache.put(entity.getId(),new WeakReference<T>(entity));
        }
        return entity;

    }

    private T getFromCache(long id)
    {
        if(cache.containsKey(id))
        {
            WeakReference<T> ref = cache.get(id);
            T cached = ref.get();
            if(cached != null)
            {
                return cached;
            }
        }
        return null;
    }

    private T cacheEntity(T entity)
    {
        T cached = getFromCache(entity.getId());
        if(cached != null) entity = cached;
        cache.put(entity.getId(),new WeakReference<T>(entity));
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
            cursor.close();
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
        try
        {
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        }
        finally
        {
            cursor.close();
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
        T entity = getFromCache(id);
        if(entity == null)
        {
            entity = createEntity();
            entity.setId(id);
            refresh(entity);
        }
        return entity;
    }

    public void deleteAll()
    {
        getWritableDatabase().delete(getTable(),null,null);
    }

    public void update(T entity)
    {
        getWritableDatabase().update(
                getTable(),
                entityToValues(entity),
                COLUMN_ID + QUESTION_MARK,
                new String[]{ Long.toString(entity.getId())});
    }
}

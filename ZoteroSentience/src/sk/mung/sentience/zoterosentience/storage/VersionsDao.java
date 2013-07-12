package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import sk.mung.zoteroapi.entities.Entity;

class VersionsDao extends BaseDao<VersionsDao.Version>
{
    private static final String COLUMN_QUERY = "query";

    class Version implements Entity
    {
        private String queryType;
        private int version;
        private long id;

        private String getQueryType()
        {
            return queryType;
        }

        private void setQueryType(String queryType)
        {
            this.queryType = queryType;
        }

        private int getVersion()
        {
            return version;
        }

        private void setVersion(int version)
        {
            this.version = version;
        }

        @Override
        public long getId()
        {
            return id;
        }

        @Override
        public void setId(long id)
        {
            this.id = id;
        }
    }

    public VersionsDao(ZoteroStorageImpl.DatabaseConnection databaseConnection, QueryDictionary queries)
    {
        super(databaseConnection, queries);
    }

    @Override
    public void upgrade(int oldVersion, int newVersion)
    {
        super.upgrade(oldVersion, newVersion);
        dropTable();
    }

    @Override
    public String getTable()
    {
        return "versions";
    }

    @Override
    public void createTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL( getQueries().createVersionsTable() );
    }

    @Override
    public void upsert( Version entity)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.insertWithOnConflict(getTable(), null, entityToValues(entity), SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    protected String[] getSelectColumns()
    {
        return new String[] { COLUMN_QUERY, COLUMN_VERSION, COLUMN_ID};
    }

    @Override
    protected Version createEntity()
    {
        return new Version();
    }

    @Override
    protected void cursorToEntity(Cursor cursor, Version v)
    {
        v.setQueryType(cursor.getString(0));
        v.setVersion(cursor.getInt(1));
        v.setId(cursor.getInt(2));
    }

    @Override
    protected ContentValues entityToValues(Version entity)
    {
        ContentValues values =new ContentValues();
        values.put(COLUMN_VERSION, entity.getVersion());
        values.put(COLUMN_QUERY, entity.getQueryType());
        return  values;
    }

    int getVersion( String queryType)
    {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(
                getTable(),
                new String[]{COLUMN_VERSION},
                COLUMN_QUERY + QUESTION_MARK,
                new String[]{queryType},
                null, null, null, null);
        try
        {
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } else return 0;
        }
        finally
        {
            cursor.close();
        }
    }

    long  setVersion( String queryType, int version)
    {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUERY, queryType);
        values.put(COLUMN_VERSION, version);

        long id = database.insertWithOnConflict(getTable(), null, values,SQLiteDatabase.CONFLICT_REPLACE);
        return id;
    }
}
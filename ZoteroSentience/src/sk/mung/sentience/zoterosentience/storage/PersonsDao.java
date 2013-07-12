package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import sk.mung.zoteroapi.entities.Person;


class PersonsDao extends BaseDao<Person>
{

    private static final String COLUMN_SHORT_NAME = "shortName";
    private static final String COLUMN_LAST_NAME = "lastName";
    private static final String COLUMN_FIRST_NAME = "firstName";

    public PersonsDao(ZoteroStorageImpl.DatabaseConnection databaseConnection, QueryDictionary queries)
    {
        super(databaseConnection, queries);
    }

    @Override
    public String getTable()
    {
        return "persons";
    }

    @Override
    public void createTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL( getQueries().createPersonsTable() );
    }

    @Override
    protected Person createEntity()
    {
        return new Person();
    }

    @Override
    public void upgrade(int oldVersion, int newVersion)
    {
        super.upgrade(oldVersion, newVersion);
        dropTable();
    }

    @Override
    public void upsert(Person entity)
    {
        ContentValues values = entityToValues(entity);
        SQLiteDatabase database = getWritableDatabase();
        long rowId = searchIdOfEntity(entity);
        if(rowId < 0)
        {
            rowId = database.insertWithOnConflict(getTable(), null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }

        entity.setId(rowId);
    }







    @Override
    protected String[] getSelectColumns()
    {
        return new String[]{COLUMN_ID, COLUMN_SHORT_NAME, COLUMN_LAST_NAME, COLUMN_FIRST_NAME};
    }

    @Override
    protected void cursorToEntity(Cursor cursor, Person person)
    {
        person.setId(cursor.getLong(0));
        person.setShortName(cursor.getString(1));
        person.setLastName(cursor.getString(2));
        person.setFirstName(cursor.getString(3));
    }

    @Override
    protected ContentValues entityToValues(Person entity)
    {
        ContentValues personValues = new ContentValues();
        personValues.put(COLUMN_FIRST_NAME, entity.getFirstName());
        personValues.put(COLUMN_LAST_NAME, entity.getLastName());
        personValues.put(COLUMN_SHORT_NAME, entity.getShortName());

        return personValues;
    }
}

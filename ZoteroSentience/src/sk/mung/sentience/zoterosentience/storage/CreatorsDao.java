package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import sk.mung.zoteroapi.entities.Creator;
import sk.mung.zoteroapi.entities.CreatorType;
import sk.mung.zoteroapi.entities.Item;


public class CreatorsDao extends BaseDao<Creator>
{
    private static final String COLUMN_PERSON = "person";

    private final PersonsDao personsDao;

    public CreatorsDao(ZoteroStorageImpl.DatabaseConnection databaseConnection, QueryDictionary queries, PersonsDao personsDao)
    {
        super(databaseConnection, queries);
        this.personsDao = personsDao;
    }

    @Override
    public String getTable()
    {
        return "creators";
    }

    @Override
    public void createTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL( getQueries().createCreatorsTable() );
    }

    @Override
    protected Creator createEntity()
    {
        return new Creator();
    }

    @Override
    public void upgrade(int oldVersion, int newVersion)
    {
        super.upgrade(oldVersion, newVersion);
        dropTable();
    }

    @Override
    public void upsert(Creator creator)
    {
        personsDao.upsert(creator.getPerson());
        long rowId = searchIdOfEntity(creator);
        SQLiteDatabase database = getWritableDatabase();

        if( 0 > rowId)
        {
            rowId = database.insertWithOnConflict(
                    getTable(),
                    null,
                    entityToValues(creator),
                    SQLiteDatabase.CONFLICT_IGNORE);

        }
        creator.setId(rowId);
    }

    @Override
    protected String[] getSelectColumns()
    {
        return new String[] {COLUMN_ID, COLUMN_TYPE, COLUMN_PERSON};
    }

    @Override
    protected void cursorToEntity(Cursor cursor, Creator creator)
    {
        creator.setId(cursor.getLong(0));
        creator.setType(CreatorType.forId(cursor.getInt(1)));
        creator.getPerson().setId(cursor.getLong(2));
        personsDao.refresh(creator.getPerson());
    }

    @Override
    protected ContentValues entityToValues(Creator entity)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, entity.getType().getId());
        values.put(COLUMN_PERSON, entity.getPerson().getId());
        return values;
    }

    public List<Creator> findByItem(Item item)
    {
        SQLiteDatabase database = getReadableDatabase();
        assert database != null;
        Cursor cursor = database.rawQuery(
                getQueries().getItemCreators(),
                new String [] {Long.toString(item.getId())});
        try
        {
            return cursorToEntities(cursor);
        }
        finally
        {
            cursor.close();
        }
    }

}

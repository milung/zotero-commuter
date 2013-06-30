package sk.mung.sentience.zoterosentience.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.Tag;


public class TagsDao extends BaseDao<Tag>
{
    private static final String COLUMN_TAG = "tag";

    public TagsDao(ZoteroStorage.DatabaseConnection databaseConnection, QueryDictionary queries)
    {
        super(databaseConnection, queries);
    }

    @Override
    public String getTable()
    {
        return "tags";
    }

    @Override
    public void createTable()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL( getQueries().createTagsTable() );
    }

    @Override
    public void upgrade(int oldVersion, int newVersion)
    {
        super.upgrade(oldVersion, newVersion);
        dropTable();
    }

    @Override
    public void upsert(Tag entity)
    {
        ContentValues values = entityToValues(entity);
        SQLiteDatabase database = getWritableDatabase();
        long rowId = database.insertWithOnConflict(getTable(), null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if(0 > rowId )
        {
            rowId = searchIdOfEntity(entity);
        }
        entity.setId(rowId);
    }

    @Override
    protected String[] getSelectColumns()
    {
        return new String[]{COLUMN_ID, COLUMN_TAG};
    }

    @Override
    protected Tag createEntity()
    {
        return new Tag();
    }

    @Override
    protected void cursorToEntity(Cursor cursor, Tag entity)
    {
        entity.setId(cursor.getLong(0));
        entity.setTag(cursor.getString(1));
    }

    @Override
    protected ContentValues entityToValues(Tag entity)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAG, entity.getTag());
        return values;
    }

    public List<Tag> findByItem(Item item)
    {
        Cursor c = getReadableDatabase().rawQuery(
                getQueries().getItemTags(),
                new String[]{ Long.toString(item.getId())});
        return cursorToEntities( c );
    }

    public void deleteByName(String tag)
    {
        getWritableDatabase().delete(getTable(),COLUMN_TAG + QUESTION_MARK, new String[] { tag});
    }
}

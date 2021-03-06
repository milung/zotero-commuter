package sk.mung.sentience.zoterocommuter.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.Tag;


public class TagsDao extends BaseDao<Tag>
{
    static final String COLUMN_TAG = "tag";

    public TagsDao(ZoteroStorageImpl.DatabaseConnection databaseConnection, QueryDictionary queries)
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
    protected long searchIdOfEntity(Tag entity)
    {
        // the tags are exclusive on tag column so ignore type while id search
        String query = COLUMN_TAG + QUESTION_MARK;
        String[] arguments = new String[] { entity.getTag()};
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

    @Override
    protected String[] getSelectColumns()
    {
        return new String[]{COLUMN_ID, COLUMN_TAG, COLUMN_TYPE};
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
        entity.setType(cursor.getInt(2));
    }

    @Override
    protected ContentValues entityToValues(Tag entity)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAG, entity.getTag());
        values.put(COLUMN_TYPE, entity.getType());
        return values;
    }

    public List<Tag> findByItem(Item item)
    {

        Cursor c = getReadableDatabase().rawQuery(
                getQueries().getItemTags(),
                new String[]{ Long.toString(item.getId())});
        try
        {
            return cursorToEntities( c );
        }
        finally
        {
            c.close();
        }
    }

    public void deleteByName(String tag)
    {
        getWritableDatabase().delete(getTable(),COLUMN_TAG + QUESTION_MARK, new String[] { tag});
    }

    public void deleteOrphans()
    {
        getWritableDatabase().execSQL(getQueries().getDeleteTagOrphans());

    }

    public Tag findByLabel(String tagLabel)
    {
        Cursor cursor= getReadableDatabase().query(
                getTable(),
                getSelectColumns(),
                COLUMN_TAG+QUESTION_MARK,
                new String[] { tagLabel}, null, null, null );
        try
        {
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                return cursorToEntity(cursor);
            }
        }
        finally
        {
            cursor.close();
        }
        Tag newTag = createEntity();
        newTag.setTag(tagLabel);
        return newTag;
    }
}

package sk.mung.sentience.zoterocommuter.storage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.Arrays;

import sk.mung.sentience.zoterocommuter.GlobalState;
import sk.mung.sentience.zoterocommuter.provider.ZoteroContract;


public class ZoteroCommuterProvider extends ContentProvider
{
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int URI_ITEMS = 1;
    private static final int URI_ITEMS_ID = 2;

    static
    {
        uriMatcher.addURI(ZoteroContract.AUTHORITY,ZoteroContract.ITEMS_PATH, URI_ITEMS);
        uriMatcher.addURI(ZoteroContract.AUTHORITY,ZoteroContract.ITEMS_PATH + "/#", URI_ITEMS_ID);
    }

    private ZoteroStorageImpl storage;

    public ZoteroCommuterProvider()
    {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        int match = uriMatcher.match(uri);
        switch (match)
        {
            case URI_ITEMS:
                return ZoteroContract.MIMETYPE_ITEMS_DIR;
            case URI_ITEMS_ID:
                return ZoteroContract.MIMETYPE_ITEMS_ITEM;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        return null;
    }

    @Override
    public boolean onCreate()
    {
        GlobalState globalState = ((GlobalState) getContext().getApplicationContext());
        assert globalState != null;
        this.storage = globalState.getStorage();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        switch (uriMatcher.match(uri)) {


            // If the incoming URI was for all of table3
            case URI_ITEMS:
            {
                SQLiteDatabase database = storage.getReadableDatabase();
                return database.query(ItemsDao.TABLE_ITEMS,projection,selection,selectionArgs,null,null,sortOrder);
            }

            // If the incoming URI was for a single row
            case URI_ITEMS_ID:
            {
                if(selection == null)
                {
                    selection = "_ID = ?";
                }
                else selection = selection + " AND _ID = ?";
                selectionArgs
                        = selectionArgs == null
                        ? new String[1]
                        : Arrays.copyOf(selectionArgs,selectionArgs.length + 1);
                selectionArgs[selectionArgs.length-1] = uri.getLastPathSegment();
                SQLiteDatabase database = storage.getReadableDatabase();
                return database.query(ItemsDao.TABLE_ITEMS,projection,selection,selectionArgs,null,null,sortOrder);
            }

            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        return 0;
    }
}

package sk.mung.sentience.zoterocommuter.provider;

import android.net.Uri;

/**
 * Created by sk1u00e5 on 9.1.2014.
 */
public class ZoteroContract
{
    private static final String SCHEME = "content://";
    public static final String AUTHORITY="sk.sentience.zoterocommuter.provider";
    public static final String ITEMS_PATH="items";
    public static final String ITEMS_AUTHORITY_URI=SCHEME + AUTHORITY + "/" + ITEMS_PATH;

    public static final String MIMETYPE_ITEMS_DIR="vnd.android.cursor.dir/item";
    public static final String MIMETYPE_ITEMS_ITEM="vnd.android.cursor.item/item";

    public static Uri getItemUri(long id)
    {
        return Uri.parse(ITEMS_AUTHORITY_URI + "/" + Long.toString(id));
    }

}

package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.mung.sentience.zoterocommuter.R;
import sk.mung.sentience.zoterosentience.renderers.ItemRenderer;
import sk.mung.sentience.zoterosentience.storage.ZoteroStorageImpl;
import sk.mung.zoteroapi.entities.Item;

/**
 *
 */
public class ItemListAdapter extends CursorAdapter
{
    private final ItemRenderer renderer;
    private final int layoutId;

    private final ZoteroStorageImpl storage;


    public ItemListAdapter(Context context, int layoutId, ZoteroStorageImpl storage)
    {
        super(context,null,0);
        this.layoutId = layoutId;
        this.storage = storage;
        this.renderer = new ItemRenderer(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup)
    {
        View view
                = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(layoutId, null);
        assert view != null;
        Item item =storage.cursorToItem(cursor);
        renderer.render(item,view);
        view.setTag(R.id.tag_item,item);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        renderer.render(storage.cursorToItem(cursor),view);
    }
}

package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import sk.mung.zoteroapi.entities.Item;
import sk.mung.sentience.zoterosentience.renderers.ItemRenderer;

/**
 *
 */
public class ItemListAdapter extends BaseAdapter
{
    private final Context context;
    private List<Item> items = new ArrayList<Item>();
    private final ItemRenderer renderer;
    private final int layoutId;

    public void setItems(List<Item> items)
    {
        this.items = items;
        if(items == null) this.items = new ArrayList<Item>();
        notifyDataSetChanged();
    }

    public ItemListAdapter(Context context, int layoutId)
    {
        this.context = context;
        this.layoutId = layoutId;
        this.renderer = new ItemRenderer(context);
    }

    @Override
    public int getCount()
    {
        return items.size();
    }

    @Override
    public Object getItem(int position)
    {
        return items.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        if(convertView == null)
        {
            convertView
                    = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(layoutId, null);
        }

        assert convertView != null;
        renderer.render(items.get(position),convertView);
        return convertView;
    }
}

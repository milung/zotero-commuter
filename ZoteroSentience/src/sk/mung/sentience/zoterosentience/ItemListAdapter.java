package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoteroapi.items.Item;

/**
 *
 */
public class ItemListAdapter extends BaseAdapter
{
    private final Context context;

    public void setItems(List<Item> items)
    {
        this.items = items;
        if(items == null) this.items = new ArrayList<Item>();
        notifyDataSetChanged();
    }
    private List<Item> items = new ArrayList<Item>();

    public ItemListAdapter(Context context)
    {
        this.context = context;
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
                    .inflate(R.layout.listitem_item, null);
        }
        Item item = items.get(position);
        assert convertView != null;
        TextView textView = (TextView) convertView.findViewById(R.id.textViewTitle);
        textView.setText(item.getTitle());

        return convertView;
    }
}

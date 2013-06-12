package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoteroapi.items.Creator;
import sk.mung.sentience.zoteroapi.items.Item;
import sk.mung.sentience.zoteroapi.items.ItemEntity;

/**
 *
 */
public class ItemListAdapter extends BaseAdapter
{
    private final Context context;
    private List<Item> items = new ArrayList<Item>();

    public void setItems(List<Item> items)
    {
        this.items = items;
        if(items == null) this.items = new ArrayList<Item>();
        notifyDataSetChanged();
    }


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

        String creatorFormatter = context.getResources().getString(R.string.creator_sequence_format);


        StringBuilder creatorsSequence = new StringBuilder();
        String format = context.getResources().getString(R.string.creator_sequence_format_first);
        for(Creator creator : item.getCreators())
        {
            creatorsSequence.append( String.format(format, creator.getFirstName(),creator.getLastName(), creator.getShortName()));
            format = creatorFormatter;
        }
        textView = (TextView) convertView.findViewById(R.id.textViewCreator);
        textView.setText(creatorsSequence.toString());
        return convertView;
    }
}

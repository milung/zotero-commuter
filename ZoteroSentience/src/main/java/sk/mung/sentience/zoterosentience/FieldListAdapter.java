package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoterocommuter.R;
import sk.mung.zoteroapi.entities.Field;

/**
 *
 */
public class FieldListAdapter extends BaseAdapter
{
    private final Context context;
    private List<Field> fields = new ArrayList<Field>();

    public void setItems(List<Field> items)
    {
        this.fields = items;
        if(items == null) this.fields = new ArrayList<Field>();
        notifyDataSetChanged();
    }


    public FieldListAdapter(Context context)
    {
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return fields.size();
    }

    @Override
    public Object getItem(int position)
    {
        return fields.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return fields.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        if(convertView == null)
        {
            convertView
                    = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.listitem_field, null);
        }

        assert convertView != null;
        Field field = (Field) getItem(position);
        if(field == null) return convertView;

        TextView textView = (TextView) convertView.findViewById(R.id.textViewFieldName);
        if( textView != null)
        {
            textView.setText(field.getType().getZoteroName());
        }
        textView = (TextView) convertView.findViewById(R.id.textViewFieldValue);
        if( textView != null)
        {
            textView.setText(field.getValue());
        }

        return convertView;
    }
}

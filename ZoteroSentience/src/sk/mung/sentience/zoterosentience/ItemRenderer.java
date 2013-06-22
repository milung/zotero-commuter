package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import sk.mung.sentience.zoteroapi.entities.Creator;
import sk.mung.sentience.zoteroapi.entities.Item;

/**
 * Created by sk1u00e5 on 17.6.2013.
 */
public class ItemRenderer
{
    private final String firstFormat;
    private final String creatorFormatter;

    public ItemRenderer( Context context )
    {
        creatorFormatter = context.getResources().getString(R.string.creator_sequence_format);
        firstFormat = context.getResources().getString(R.string.creator_sequence_format_first);
    }

    public void render(Item item, View view)
    {
        if(item == null) return;
        TextView textView = (TextView) view.findViewById(R.id.textViewTitle);
        if( textView != null)
        {
            textView.setText(item.getTitle());
        }
        String format = firstFormat;
        StringBuilder creatorsSequence = new StringBuilder();
        for(Creator creator : item.getCreators())
        {
            creatorsSequence.append( String.format(format, creator.getFirstName(),creator.getLastName(), creator.getShortName()));
            format = creatorFormatter;
        }
        textView = (TextView) view.findViewById(R.id.textViewCreator);
        if( textView != null)
        {
            textView.setText(creatorsSequence.toString());
        }
    }
}

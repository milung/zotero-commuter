package sk.mung.sentience.zoterocommuter.renderers;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import sk.mung.zoteroapi.entities.Creator;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.sentience.zoterocommuter.R;

/**
 * Created by sk1u00e5 on 17.6.2013.
 */
public class ItemRenderer
{
    private static class Attributes
    {
        private final int typeNameId;
        private final int typeIconId;

        private Attributes(int typeNameId, int typeIconId)
        {
            this.typeNameId = typeNameId;
            this.typeIconId = typeIconId;
        }

        private int getTypeNameId()
        {
            return typeNameId;
        }

        private int getTypeIconId()
        {
            return typeIconId;
        }
    }

    private final String firstFormat;
    private final String creatorFormatter;
    private static final Map<ItemType, Attributes> attributes = new HashMap<ItemType, Attributes>();

    static
    {
        attributes.put(ItemType.ATTACHMENT,new Attributes( R.string.zotero_attachment, R.drawable.ic_document_pdf));
        attributes.put(ItemType.BOOK,new Attributes( R.string.zotero_book, R.drawable.ic_book));
        attributes.put(ItemType.BOOK_SECTION,new Attributes( R.string.zotero_book_section, R.drawable.ic_book_section));
        attributes.put(ItemType.CONFERENCE_PAPER,new Attributes( R.string.zotero_conference_paper, R.drawable.ic_conference));
        attributes.put(ItemType.JOURNAL_ARTICLE,new Attributes( R.string.zotero_journal_article, R.drawable.ic_document));
        attributes.put(ItemType.MAGAZINE_ARTICLE, new Attributes( R.string.zotero_magazine_article, R.drawable.ic_magazine));
        attributes.put(ItemType.REPORT,new Attributes( R.string.zotero_report, R.drawable.ic_document));
    }


    public ItemRenderer( Context context )
    {
        creatorFormatter = context.getResources().getString(R.string.creator_sequence_format);
        firstFormat = context.getResources().getString(R.string.creator_sequence_format_first);
    }

    public void render(Item item, View view)
    {

        if(item == null) return;
        Attributes attr = attributes.get(item.getItemType());

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

        textView = (TextView) view.findViewById(R.id.textViewType);
        if( textView != null)
        {
            if(attr == null)
            {
                textView.setText( item.getItemType().name());
            }
            else
            {
                textView.setText(attr.getTypeNameId());
            }
        }

        ImageView icon = (ImageView) view.findViewById(R.id.icon_status);
        if(icon != null)
        {
            icon.setBackgroundResource(attr == null ? R.drawable.ic_document : attr.getTypeIconId());
        }
    }
}

package sk.mung.sentience.zoterosentience.renderers;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.sentience.zoterosentience.R;

/**
 * Created by sk1u00e5 on 27.6.2013.
 */
public class FieldRenderer
{
    static class RenderingAttributes
    {
        boolean isVisible()
        {
            return isVisible;
        }

        int getLayoutId()
        {
            return layoutId;
        }

        int getTitleId()
        {
            return titleId;
        }

        private final boolean isVisible;
        private final int layoutId;
        private final int titleId;

        int getSortingPosition()
        {
            return sortingPosition;
        }

        private final int sortingPosition;

        RenderingAttributes(boolean visible, int sortingPosition, int layoutId, int titleId)
        {
            isVisible = visible;
            this.sortingPosition = sortingPosition;
            this.layoutId = layoutId;
            this.titleId = titleId;
        }
    }

    private final static Map<ItemField, RenderingAttributes> attributes = new HashMap<ItemField, RenderingAttributes>();
    private final LayoutInflater inflater;
    private final Resources resources;
    private final ViewGroup parent;
    static
    {
        attributes.put(
                ItemField.ABSTRACT_NOTE,
                new RenderingAttributes(true,100, R.layout.listitem_field,R.string.zotero_field_abstract_note ));
        attributes.put(
                ItemField.ACCESS_DATE,
                new RenderingAttributes(false, 0, 0,0));
        attributes.put(
                ItemField.EXTRA,
                new RenderingAttributes(true,99999, R.layout.listitem_field_inline,R.string.zotero_field_extra));
        attributes.put(
                ItemField.APPLICATION_NUMBER,
                new RenderingAttributes(true,1300, R.layout.listitem_field_inline,R.string.zotero_field_application_number));
        attributes.put(
                ItemField.ARCHIVE,
                new RenderingAttributes(true, 1200,R.layout.listitem_field_inline,R.string.zotero_field_archive));
        attributes.put(
                ItemField.ARCHIVE_LOCATION,
                new RenderingAttributes(true, 1210,R.layout.listitem_field_inline,R.string.zotero_field_archive_location));
        attributes.put(
                ItemField.BLOG_TITLE,
                new RenderingAttributes(false,0, 0,0));
        attributes.put(
                ItemField.TITLE,
                new RenderingAttributes(false, 0,0,0));
        attributes.put(
                ItemField.NOTE,
                new RenderingAttributes(false, 0,0,0));
        attributes.put(
                ItemField.SHORT_TITLE,
                new RenderingAttributes(true,900, R.layout.listitem_field_inline,R.string.zotero_field_short_title));
        attributes.put(
                ItemField.URL,
                new RenderingAttributes(true,700, R.layout.listitem_field_inline,R.string.zotero_field_url));
        attributes.put(
                ItemField.DATE,
                new RenderingAttributes(true,600, R.layout.listitem_field_inline,R.string.zotero_field_date));
        attributes.put(
                ItemField.LIBRARY_CATALOG,
                new RenderingAttributes(true, 1100,R.layout.listitem_field_inline,R.string.zotero_field_library_catalog));
        attributes.put(
                ItemField.PAGES,
                new RenderingAttributes(true,300, R.layout.listitem_field_inline,R.string.zotero_field_pages));
        attributes.put(
                ItemField.VOLUME,
                new RenderingAttributes(true, 500,R.layout.listitem_field_inline,R.string.zotero_field_volume));
        attributes.put(
                ItemField.ISSN,
                new RenderingAttributes(true, 1000,R.layout.listitem_field_inline,R.string.zotero_field_issn));
        attributes.put(
                ItemField.ISBN,
                new RenderingAttributes(true, 1010, R.layout.listitem_field_inline,R.string.zotero_field_isbn));
        attributes.put(
                ItemField.PUBLICATION_TITLE,
                new RenderingAttributes(true,200, R.layout.listitem_field_inline,R.string.zotero_field_publication_title));
        attributes.put(
                ItemField.ISSUE,
                new RenderingAttributes(true, 400,R.layout.listitem_field_inline,R.string.zotero_field_issue));
    }

    public FieldRenderer(Context context, ViewGroup parent)
    {
        this.parent = parent;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = context.getResources();
    }

    public List<Field> sortFields(List<Field> input)
    {
        Comparator<Field> comparator = new Comparator<Field>(){
            @Override
            public int compare(Field lhs, Field rhs)
            {
                RenderingAttributes l = attributes.get(lhs.getType());
                RenderingAttributes r = attributes.get(rhs.getType());
                int lPos = l == null ? 999999 : l.getSortingPosition();
                int rPos = r == null ? 999999 : r.getSortingPosition();

                return lPos - rPos;
            }
        };

        Collections.sort(input,comparator);
        return input;
    }
    public View createView(Field field )
    {
        int layoutResource = R.layout.listitem_field_inline;
        String title = field.getType().getZoteroName();
        if(attributes.containsKey(field.getType()))
        {
            RenderingAttributes attr = attributes.get(field.getType());
            if(!attr.isVisible) return null;
            layoutResource = attr.getLayoutId();
            title = resources.getString(attr.getTitleId());
        }
        View view = inflater.inflate(layoutResource, parent, false);

        TextView textView = (TextView) view.findViewById(R.id.textViewFieldName);
        if( textView != null)
        {
            textView.setText(title);
        }
        textView = (TextView) view.findViewById(R.id.textViewFieldValue);
        if( textView != null)
        {
            textView.setText(field.getValue());
        }
        return view;
    }
}

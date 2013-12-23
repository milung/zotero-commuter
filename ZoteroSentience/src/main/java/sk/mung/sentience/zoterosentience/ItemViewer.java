package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoterocommuter.R;
import sk.mung.sentience.zoterosentience.renderers.AttachmentRenderer;
import sk.mung.sentience.zoterosentience.renderers.FieldRenderer;
import sk.mung.sentience.zoterosentience.renderers.ItemConflictFragment;
import sk.mung.sentience.zoterosentience.renderers.ItemRenderer;
import sk.mung.sentience.zoterosentience.renderers.NoteRenderer;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.Tag;

public class ItemViewer extends Fragment
{
    public static final String HIDDEN_TAG_PREFIX = "_";
    private Item item;
    private ItemRenderer itemRenderer;
    private AttachmentRenderer attachmentRenderer;
    private NoteRenderer noteRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.item_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // handle menuItem selection
        switch (menuItem.getItemId()) {
            case R.id.add_note:
                noteRenderer.createNewNote();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }




    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        displayItems();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(attachmentRenderer!=null)
        {
            attachmentRenderer.onDestroy();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_item_viewer, container, false);
        assert view != null;
        itemRenderer = new ItemRenderer(getActivity());

        ViewGroup parent = (ViewGroup) view.findViewById(R.id.itemsGroup);
        attachmentRenderer = new AttachmentRenderer(getActivity(), itemRenderer,parent);
        noteRenderer = new NoteRenderer(this, parent, item, new  ItemConflictFragment.Callback()
        {
            @Override
            public void itemStatusChanged(Item target, View view)
            {
                displayItems();
            }
        });
        return view;
    }

    public void setItem(Item item)
    {
        this.item = item;
        if(item != null &&  getView() != null)
        {
            displayItems();
        }
    }

    private void displayItems()
    {
        if(item == null) return;

        itemRenderer.render(item, getView().findViewById(R.id.headerGroup));
        renderTags();

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewGroup parent = (ViewGroup) getView().findViewById(R.id.itemsGroup);
        parent.removeAllViews();

        boolean topLine = renderAttachments(parent, inflater);

        parent = (ViewGroup) getView().findViewById(R.id.fieldsGroup);
        parent.removeAllViews();

        renderFields(parent,inflater,topLine);

        Field urlField = item.getField(ItemField.URL);
        if(urlField != null && !urlField.getValue().isEmpty())
        {
            getView().findViewById(R.id.imageViewUrl).setVisibility(View.VISIBLE);
            TextView linkView = (TextView) getView().findViewById(R.id.textViewUrl);
            if(linkView !=null)
            {
                linkView.setText(urlField.getValue());
            }
        }
        else
        {
            getView().findViewById(R.id.imageViewUrl).setVisibility(View.INVISIBLE);

        }

    }

    private void renderFields(ViewGroup parent, LayoutInflater inflater, boolean topLine)
    {
        FieldRenderer renderer = new FieldRenderer(getActivity(), parent);
        List<Field> fields = renderer.sortFields(new ArrayList<Field>(item.getFields()));

        for(Field field : fields)
        {
            if(field == null || field.getValue() == null || field.getValue().isEmpty()) continue;

            if(topLine)
            {
                inflater.inflate(R.layout.line, parent);
                topLine = false;
            }
            View view =renderer.createView(field);
            if(view == null) continue;
            parent.addView(view);
            inflater.inflate(R.layout.line, parent);
        }
    }

    private boolean renderAttachments(ViewGroup parent, LayoutInflater inflater)
    {
        boolean topLine = true;
        if(item.getItemType() == ItemType.ATTACHMENT)
        {
            inflater.inflate(R.layout.line, parent);
            attachmentRenderer.createView(item);
            inflater.inflate(R.layout.line, parent);
            topLine = false;
        }

        for(Item child : item.getChildren())
        {
            if(topLine)
            {
                inflater.inflate(R.layout.line, parent);
                topLine = false;
            }

            if(child.getItemType() == ItemType.ATTACHMENT)
            {
                attachmentRenderer.createView(child);
            }
            else if(child.getItemType() == ItemType.NOTE)
            {
                noteRenderer.createView(child);
            }
            else
            {
                View view = inflater.inflate(R.layout.listitem_item, parent, false);
                assert view != null;
                itemRenderer.render(child, view);
                parent.addView(view);
            }
            inflater.inflate(R.layout.line, parent);
        }
        return topLine;
    }

    private void renderTags()
    {
        StringBuilder tagsText = new StringBuilder();
        boolean isFirst = true;
        for(Tag tag : item.getTags())
        {
            if( tag.getTag() != null && !tag.getTag().startsWith(HIDDEN_TAG_PREFIX) )
            {
                if(!isFirst)
                {
                    tagsText.append(getString(R.string.tags_separator));
                }
                tagsText.append(tag.getTag());
                isFirst = false;
            }
        }
        TextView tagsView = (TextView) getView().findViewById(R.id.textViewTags);
        if(tagsText.length() == 0)
        {
            tagsText.append(getString(R.string.tags_no_tags));
            tagsView.setTypeface(null, Typeface.ITALIC);
        }
        else tagsView.setTypeface(null, Typeface.NORMAL);

        tagsView.setText(tagsText.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        noteRenderer.onActivityResult(requestCode,resultCode,data);
        displayItems();
    }


}
package sk.mung.sentience.zoterosentience;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.Tag;
import sk.mung.sentience.zoterosentience.renderers.AttachmentRenderer;
import sk.mung.sentience.zoterosentience.renderers.FieldRenderer;
import sk.mung.sentience.zoterosentience.renderers.ItemRenderer;

public class ItemViewer extends Fragment
{
    private static final int REQUEST_EDIT_NOTE = 1;
    private Item item;
    private DownloadManager downloadManager=null;
    private ItemRenderer itemRenderer;
    private AttachmentRenderer attachmentRenderer;
    private Item editingChild = null;

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
        attachmentRenderer.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        downloadManager=(DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_item_viewer, container, false);
        itemRenderer = new ItemRenderer(getActivity());

        ViewGroup parent = (ViewGroup) view.findViewById(R.id.itemsGroup);
        attachmentRenderer = new AttachmentRenderer(getActivity(), itemRenderer,parent);


        assert view != null;
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

        View.OnClickListener editListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int position = (Integer)view.getTag(R.id.position);
                Item child = item.getChildren().get(position);
                editNote(child);
            }
        };
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewGroup parent = (ViewGroup) getView().findViewById(R.id.itemsGroup);
        parent.removeAllViews();

        if(item.getItemType() == ItemType.ATTACHMENT)
        {
            attachmentRenderer.createView(item);
        }
        int position = 0;

        for(Item child : item.getChildren())
        {
            if(position == 0)
            {
                inflater.inflate(R.layout.line, parent);
            }

            if(child.getItemType() == ItemType.ATTACHMENT)
            {
                attachmentRenderer.createView(child);
            }
            else if(child.getItemType() == ItemType.NOTE)
            {
                View view = inflater.inflate(R.layout.listitem_item_note, parent, false);
                TextView noteView = (TextView) view.findViewById(R.id.textViewNote);
                noteView.setText(Html.fromHtml(child.getField(ItemField.NOTE).getValue()));
                view.setTag(R.id.position,new Integer(position));
                view.setOnClickListener(editListener);
                view.setBackgroundResource(R.drawable.selector);
                parent.addView(view);
            }
            else
            {
                View view = inflater.inflate(R.layout.listitem_item, parent, false);
                itemRenderer.render(child, view);
                parent.addView(view);
            }
            inflater.inflate(R.layout.line, parent);
            position++;
        }
        FieldRenderer renderer = new FieldRenderer(getActivity(), parent);
        List<Field> fields = renderer.sortFields(new ArrayList<Field>(item.getFields()));
        for(Field field : fields)
        {
            if(field == null || field.getValue() == null || field.getValue().isEmpty()) continue;
            View view =renderer.createView(field);
            if(view == null) continue;
            parent.addView(view);
        }
    }

    private void renderTags()
    {
        StringBuilder tagsText = new StringBuilder();
        boolean isFirst = true;
        for(Tag tag : item.getTags())
        {
            if( tag.getTag() != null )
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

    private void editNote(Item child)
    {
       Intent intent = new Intent();

        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(Intent.EXTRA_HTML_TEXT, child.getField(ItemField.NOTE).getValue());
        intent.setType("text/html");
        editingChild = child;
        try
        {
            startActivityForResult(intent,REQUEST_EDIT_NOTE);
        }
        catch (ActivityNotFoundException ex)
        {
            intent.setAction(Intent.ACTION_VIEW);
            try
            {
                startActivity(intent);
            }
            catch (ActivityNotFoundException ex2)
            {
                Toast.makeText(
                        getActivity(),
                        R.string.attachment_no_viewer,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(REQUEST_EDIT_NOTE == requestCode && resultCode== Activity.RESULT_OK)
        {
            String text = data.getStringExtra(Intent.EXTRA_HTML_TEXT);
            if(text != null && editingChild != null)
            {
                editingChild.getField(ItemField.NOTE).setValue(text);
                displayItems();
            }
        }
    }


}
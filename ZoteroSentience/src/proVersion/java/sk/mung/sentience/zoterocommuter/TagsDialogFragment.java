package sk.mung.sentience.zoterocommuter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sk.mung.sentience.zoterocommuter.storage.TagsLoader;
import sk.mung.sentience.zoterocommuter.utils.FlowLayout;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.Tag;

public class TagsDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor>
{

    private final Item item;
    private final List<String> itemTags = new ArrayList<String>();
    private FlowLayout flowLayout;
    private EditText tagsText;

    public TagsDialogFragment(Item item)
    {
        this.item = item;
        for (Tag tag : item.getTags())
        {
            String tagLabel = tag.getTag();
            if (tagLabel != null)
            {
                tagLabel = tagLabel.trim();
                if (!tagLabel.isEmpty())
                {
                    itemTags.add(tagLabel);
                }
            }
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_tags_dialog, null);
        tagsText = (EditText) view.findViewById(R.id.tags);
        if (tagsText != null)
        {
            tagsText.setText(getSeparatedTags());
        }
        flowLayout = (FlowLayout) view.findViewById(R.id.flow_layout);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                        processTags(tagsText.getText().toString());

                        ((GlobalState) getActivity().getApplication()).getStorage().invalidateItems();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(1, savedInstanceState, this);
    }

    private void processTags(String text)
    {
        List<String> tags = new ArrayList<String>( Arrays.asList(text.split(getActivity().getString(R.string.tags_split_regex))));
        for (String tag : itemTags)
        {
            if (tag.startsWith("_"))
            {
                tags.add(tag);
            }
        }
        ((GlobalState) getActivity().getApplication()).getStorage().assignTagsToItem(item, tags.toArray(new String[0]));
    }

    private String getSeparatedTags()
    {
        StringBuilder tagsText = new StringBuilder();
        boolean isFirst = true;
        String separator = getString(R.string.tags_separator) + "  ";
        for (Tag tag : item.getTags())
        {
            if (tag.getTag() != null && !tag.getTag().startsWith("_"))
            {
                if (!isFirst)
                {
                    tagsText.append(separator);
                }
                tagsText.append(tag.getTag());
                isFirst = false;
            }
        }
        return tagsText.toString();
    }

    /*  ArrayList subjects_list, stores string value so that there would not be duplicate names */
    public void addTagViewToFlow(
            final String tag,
            final FlowLayout flowlayout)
    {
        /// creates bubble button
        final Button bubbleButton = new Button(getActivity());
        FlowLayout.LayoutParams flowLP = new FlowLayout.LayoutParams(5, 5);
        bubbleButton.setBackgroundResource(R.drawable.tag_selector);
        bubbleButton.setPadding(6, 2, 6, 2);
        bubbleButton.setTextSize(14);
        bubbleButton.setSingleLine(true);
        bubbleButton.setEllipsize(TextUtils.TruncateAt.END);
        bubbleButton.setTextColor(Color.BLACK);
        // sets the text
        bubbleButton.setText(tag);
        bubbleButton.setSelected(itemTags.contains(tag));

        final String splitter = getString(R.string.tags_split_regex);
        final String separator = getString(R.string.tags_separator) + "  ";
        bubbleButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                boolean isSelected = view.isSelected();
                view.setSelected(!isSelected);

                Button bubble = (Button) view;
                String tagLabel = bubble.getText().toString();
                String[] tagLabels = tagsText.getText().toString().split(splitter);
                StringBuilder newLabels = new StringBuilder();
                boolean isFirst = true;
                for (String current : tagLabels)
                {
                    String trimmed = current.trim();
                    if (trimmed.isEmpty()) continue;
                    if (isSelected && trimmed.equals(tagLabel)) continue;
                    if (!isFirst)
                    {
                        newLabels.append(separator);
                    }
                    isFirst = false;
                    newLabels.append(trimmed);
                }
                if (!isSelected)
                {
                    if (!isFirst)
                    {
                        newLabels.append(separator);
                    }
                    newLabels.append(tagLabel);
                }
                tagsText.setText(newLabels);
            }
        });
        // flowLP is related to FlowLayout
        flowlayout.addView(bubbleButton, flowLP);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        TagsLoader loader
                = new TagsLoader(this.getActivity(), ((GlobalState) getActivity().getApplication()).getStorage());
        loader.setUpdateThrottle(3000);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        List<String> insertedTags = new ArrayList<String>(cursor.getCount());
        while (cursor.moveToNext())
        {
            String tag = TagsLoader.getTagLabel(cursor);
            if (tag == null) continue;
            tag = tag.trim();
            if (tag.isEmpty() || tag.startsWith("_") || insertedTags.contains(tag)) continue;
            insertedTags.add(tag);
            addTagViewToFlow(tag, flowLayout);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        flowLayout.removeAllViews();
    }
}

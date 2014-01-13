package sk.mung.sentience.zoterocommuter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.Tag;

/**
 * Created by sk1u00e5 on 13.1.2014.
 */
public class TagsDialogFragment extends DialogFragment
{

    private final Item item;

    public TagsDialogFragment(Item item)
    {
        this.item = item;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_tags_dialog, null);
        final EditText tagsText = (EditText) view.findViewById(R.id.tags);
        if(tagsText != null)
        {

            tagsText.setText(getSeparatedTags());
        }
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        processTags(tagsText.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    private void processTags(String text)
    {
        String[] tags = text.split("\\,|\\;");
        ((GlobalState)getActivity().getApplication()).getStorage().assignTagsToItem(item, tags);
    }

    private  String getSeparatedTags()
    {
        StringBuilder tagsText = new StringBuilder();
        boolean isFirst = true;
        for(Tag tag : item.getTags())
        {
            if( tag.getTag() != null && !tag.getTag().startsWith("_") )
            {
                if(!isFirst)
                {
                    tagsText.append(getString(R.string.tags_separator));
                }
                tagsText.append(tag.getTag());
                isFirst = false;
            }
        }
        return tagsText.toString();
    }
}

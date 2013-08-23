package sk.mung.sentience.zoterosentience.renderers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import sk.mung.sentience.zoterosentience.R;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;

public class NoteRenderer
{
    private static final int REQUEST_EDIT_NOTE = 1;

    private Item editingChild = null;
    private final Fragment context;
    private final ViewGroup parent;
    private final LayoutInflater inflater;

    private View.OnClickListener editListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Item child = (Item) view.getTag(R.id.item_tag);
            editNote(child);
        }
    };

    private void editNote(Item child)
    {
        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(Intent.EXTRA_HTML_TEXT, child.getField(ItemField.NOTE).getValue());
        intent.setType("text/html");
        editingChild = child;
        try
        {
            context.startActivityForResult(intent, REQUEST_EDIT_NOTE);
        }
        catch (ActivityNotFoundException ex)
        {
            intent.setAction(Intent.ACTION_VIEW);
            try
            {
                context.startActivity(intent);
            }
            catch (ActivityNotFoundException ex2)
            {
                Toast.makeText(
                        context.getActivity(),
                        R.string.attachment_no_viewer,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    public NoteRenderer(Fragment fragment, ViewGroup parent)
    {
        this.context = fragment;
        this.parent = parent;
        this.inflater = fragment.getActivity().getLayoutInflater();
    }

    public void createView(Item noteItem)
    {
        View view = inflater.inflate(R.layout.listitem_item_note, parent, false);
        assert view != null;
        TextView noteView = (TextView) view.findViewById(R.id.textViewNote);
        noteView.setText(Html.fromHtml(noteItem.getField(ItemField.NOTE).getValue()));
        view.setTag(R.id.item_tag, noteItem);
        view.setOnClickListener(editListener);
        view.setBackgroundResource(R.drawable.selector);
        parent.addView(view);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(REQUEST_EDIT_NOTE == requestCode && resultCode== Activity.RESULT_OK)
        {
            String text = data.getStringExtra(Intent.EXTRA_HTML_TEXT);
            if(text != null && editingChild != null)
            {
                editingChild.getField(ItemField.NOTE).setValue(text);
            }
        }
    }
}

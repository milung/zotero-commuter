package sk.mung.sentience.zoterocommuter.renderers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import sk.mung.sentience.zoterocommuter.GlobalState;
import sk.mung.sentience.zoterocommuter.NoteEditor;
import sk.mung.sentience.zoterocommuter.R;
import sk.mung.sentience.zoterocommuter.storage.ItemsDao;
import sk.mung.sentience.zoterocommuter.storage.ZoteroStorageImpl;
import sk.mung.zoteroapi.ZoteroStorage;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemEntity;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.SyncStatus;

public class NoteRenderer
{
    private static final int REQUEST_EDIT_NOTE = 1;
    public static final int REQUEST_CREATE_NOTE = 2;

    private final Item item;
    private Item editingChild = null;
    private final Fragment context;
    private final ViewGroup parent;
    private final LayoutInflater inflater;
    private final ItemConflictFragment.Callback callback;

    private View.OnClickListener editListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Item child = (Item) view.getTag(R.id.item_tag);
            editNote(child, REQUEST_EDIT_NOTE);
        }
    };

    private View.OnLongClickListener resolutionListener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View view) {
            Item target = (Item) view.getTag(R.id.item_tag);
            DialogFragment dialog
                    = new ItemConflictFragment(target, view, callback);
            dialog.show(context.getChildFragmentManager(),"conflict_dialog");
            return true;
        }
    };

    public void createNewNote() {
        GlobalState globalState= (GlobalState)context.getActivity().getApplication();
        ZoteroStorage storage = globalState.getStorage();
        Item note = storage.createItem();
        note.setKey(ItemEntity.NEW_ITEM_KEYP_PREFIX + Long.toString(globalState.getKeyCounter(),16));
        note.setItemType(ItemType.NOTE);
        note.setParentKey(item.getKey());
        note.setSynced(SyncStatus.SYNC_LOCALLY_UPDATED);
        Field field = storage.createField();
        field.setType(ItemField.NOTE);
        field.setValue(context.getActivity().getString(R.string.new_note_content));
        note.addField(field);
        editNote(note, NoteRenderer.REQUEST_CREATE_NOTE);
    }

    private void editNote(Item child, int requestCode)
    {
        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(NoteEditor.getTextIntent(), child.getField(ItemField.NOTE).getValue());
        intent.setType("text/html");
        editingChild = child;
        try
        {
            context.startActivityForResult(intent, requestCode);
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

    public NoteRenderer(Fragment fragment, ViewGroup parent, Item item, ItemConflictFragment.Callback callback)
    {
        this.context = fragment;
        this.parent = parent;
        this.inflater = fragment.getActivity().getLayoutInflater();
        this.item = item;
        this.callback = callback;
    }

    public void createView(Item noteItem)
    {
        View view = inflater.inflate(R.layout.listitem_item_note, parent, false);
        assert view != null;

        TextView noteView = (TextView) view.findViewById(R.id.textViewNote);
        noteView.setText(Html.fromHtml(noteItem.getField(ItemField.NOTE).getValue()));
        view.setTag(R.id.item_tag, noteItem);
        view.setOnClickListener(editListener);
        view.setOnLongClickListener(resolutionListener);

        ImageView syncStatus = (ImageView) view.findViewById(R.id.sync_status);
        if(syncStatus != null)
        {
            syncStatus.setVisibility(noteItem.getSynced() == SyncStatus.SYNC_OK ? View.GONE : View.VISIBLE);
            if(noteItem.getSynced() == SyncStatus.SYNC_LOCALLY_UPDATED )
            {
                syncStatus.setBackgroundResource(R.drawable.blue_dot);
            }
            else if(noteItem.getSynced() != SyncStatus.SYNC_OK )
            {
                syncStatus.setBackgroundResource(R.drawable.red_dot);
            }
        }
        view.setBackgroundResource(R.drawable.selector);
        parent.addView(view);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        ZoteroStorageImpl storage = ((GlobalState) context.getActivity().getApplication())
                .getStorage();
        if(REQUEST_EDIT_NOTE == requestCode && resultCode== Activity.RESULT_OK)
        {
            String text = data.getStringExtra(NoteEditor.getTextIntent());
            if(text != null && editingChild != null)
            {
                if(editingChild.getSynced() == SyncStatus.SYNC_OK)
                {
                    Item remoteVersion = editingChild.createCopy();
                    remoteVersion.setKey(ItemsDao.CONFLICTED_KEY_PREFIX + item.getKey());
                    remoteVersion.setSynced(SyncStatus.SYNC_REMOTE_VERSION);
                    editingChild.setSynced(SyncStatus.SYNC_LOCALLY_UPDATED);
                }
                editingChild.getField(ItemField.NOTE).setValue(text);
            }
        }
        if(requestCode == REQUEST_CREATE_NOTE && resultCode== Activity.RESULT_OK)
        {
            String text = data.getStringExtra(NoteEditor.getTextIntent());
            if(text != null && editingChild != null)
            {
                editingChild.getField(ItemField.NOTE).setValue(text);
            }

            item.addChild(editingChild);
            storage.updateItem(editingChild);
        }
    }
}

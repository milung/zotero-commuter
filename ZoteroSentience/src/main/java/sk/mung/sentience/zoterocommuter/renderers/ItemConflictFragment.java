package sk.mung.sentience.zoterocommuter.renderers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import sk.mung.sentience.zoterocommuter.GlobalState;
import sk.mung.sentience.zoterocommuter.R;
import sk.mung.sentience.zoterocommuter.storage.ZoteroStorageImpl;
import sk.mung.zoteroapi.entities.Item;

public class ItemConflictFragment extends DialogFragment
{
    public interface Callback
    {
        public void itemStatusChanged(Item target, View view);
    }

    private final Item target;
    private final View view;
    private final Callback callback;

    public static final int DELETE = 0;
    public static final int REMOVE_LOCAL = 1;
    public static final int REMOVE_REMOTE= 2;

    public ItemConflictFragment(Item target, View view, Callback callback)
    {
        this.target = target;
        this.view = view;
        this.callback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.resolve_attachment_conflict)
        .setItems(R.array.item_resolution_options, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                ZoteroStorageImpl storage = ((GlobalState)getActivity().getApplication()).getStorage();
                switch (which)
                {
                    case DELETE:
                        storage.markAsDeleted(target);
                    case REMOVE_LOCAL:
                        storage.removeLocalVersion(target);
                        break;
                    case REMOVE_REMOTE:
                        storage.replaceRemoteVersion(target);
                        break;
                }
                callback.itemStatusChanged(target, view);
            }
        });
        return builder.create();
    }
}

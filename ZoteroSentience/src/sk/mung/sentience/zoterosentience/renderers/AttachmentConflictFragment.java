package sk.mung.sentience.zoterosentience.renderers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import sk.mung.sentience.zoterosentience.GlobalState;
import sk.mung.sentience.zoterosentience.R;
import sk.mung.zoteroapi.ZoteroSync;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;

public class AttachmentConflictFragment extends DialogFragment
{
    public final Item target;

    public static final int REMOVE_LOCAL = 0;
    public static final int REMOVE_REMOTE= 1;

    public AttachmentConflictFragment(Item target)
    {
        this.target = target;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.resolve_attachment_conflict)
        .setItems(R.array.attachment_resolution_options, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                ZoteroSync sync = ((GlobalState)getActivity().getApplication()).getZoteroSync();
                switch (which)
                {
                    case REMOVE_LOCAL:
                        sync.deleteAttachment(target);
                        break;
                    case REMOVE_REMOTE:
                        removeRemoteDeltas();
                        break;
                }
            }
        });
        return builder.create();
    }

    private void removeRemoteDeltas()
    {
        Field downloadTime
                = Field.create(ItemField.DOWNLOAD_TIME, target.getField(ItemField.MODIFICATION_TIME).getValue());
        target.addField(downloadTime);

        Field downloadHash
                = Field.create(ItemField.DOWNLOAD_MD5, target.getField(ItemField.MD5).getValue());
        target.addField(downloadHash);
    }
}

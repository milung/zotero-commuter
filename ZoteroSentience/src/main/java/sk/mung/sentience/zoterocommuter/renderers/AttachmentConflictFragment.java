package sk.mung.sentience.zoterocommuter.renderers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import sk.mung.pdfextraction.PdfExtractor;
import sk.mung.sentience.zoterocommuter.GlobalState;
import sk.mung.sentience.zoterocommuter.R;
import sk.mung.zoteroapi.ZoteroStorage;
import sk.mung.zoteroapi.ZoteroSync;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.SyncStatus;

public class AttachmentConflictFragment extends DialogFragment
{
    public interface Callback
    {
        public void attachmentStatusChanged(Item target, View view);
    }

    private final Item target;
    private final View view;
    private final Callback callback;

    public static final int REMOVE_LOCAL = 0;
    public static final int REMOVE_REMOTE= 1;
    public static final int EXTRACT_ANNOTATIONS= 2;


    public AttachmentConflictFragment(Item target, View view, Callback callback)
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
                    case EXTRACT_ANNOTATIONS:
                        try {
                            File dir = ((GlobalState)getActivity().getApplication()).getDownloadDirectory();
                            dir = new File(dir, target.getKey());
                            extractAnnotations(new File(dir,ZoteroSync.getFileName(target, false)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                callback.attachmentStatusChanged(target,view);
            }
        });
        return builder.create();
    }

    private void extractAnnotations(File file) throws IOException, ParserConfigurationException, SAXException {
        PdfExtractor extractor = new PdfExtractor(file);
        String annotations = extractor.extractAnnotations(
                getActivity().getString(R.string.annotation_template),
                getActivity().getString(R.string.highlight),
                getActivity().getString(R.string.note));
        ZoteroStorage storage = ((GlobalState)getActivity().getApplication()).getStorage();
        Item parent = null;
        String parentKey = target.getParentKey();
        if(parentKey!=null)
        {
            parent = storage.findItemByKey(parentKey);
        }
        Item note = storage.createItem();
        note.setKey("");
        note.setItemType(ItemType.NOTE);
        note.setParentKey(parentKey);
        note.setSynced(SyncStatus.SYNC_LOCALLY_UPDATED);
        Field field = storage.createField();
        field.setType(ItemField.NOTE);
        field.setValue(annotations);
        note.addField(field);
        if(parent !=null)
        {
            parent.addChild(note);
        }
        storage.updateItem(note);
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

package sk.mung.sentience.zoterocommuter.renderers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Toast;

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
        boolean isPdf = ZoteroSync.getFileName(target,true).endsWith(".pdf");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.resolve_attachment_conflict)
        .setItems(isPdf ? R.array.attachment_resolution_options_pdf : R.array.attachment_resolution_options , new DialogInterface.OnClickListener()
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

    private void extractAnnotations(File file) throws IOException, ParserConfigurationException, SAXException
    {

        final Activity activity = getActivity();
        final GlobalState globalState = (GlobalState)getActivity().getApplication();
        AsyncTask<File, Integer, String> extractTask = new AsyncTask<File, Integer, String>() {

            @Override
            protected void onPreExecute() {
                globalState.addProcessedItem(target.getKey(),GlobalState.PROCESS_TEXT_EXTRACTION);
            }

            @Override
            protected String doInBackground(File... files) {
                PdfExtractor extractor = null;
                try {
                    extractor = new PdfExtractor(files[0]);

                    String annotations = extractor.extractAnnotations(
                        activity.getString(R.string.annotation_template),
                        activity.getString(R.string.highlight),
                        activity.getString(R.string.note));
                    extractor.close();
                    return annotations;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String annotations) {
                globalState.removeProcessedItem(target.getKey(),GlobalState.PROCESS_TEXT_EXTRACTION);

                if(annotations==null)
                {
                    Toast toast = Toast.makeText(getActivity(),R.string.extraction_error,Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                ZoteroStorage storage = globalState.getStorage();
                Item parent = null;
                String parentKey = target.getParentKey();
                if(parentKey!=null)
                {
                    parent = storage.findItemByKey(parentKey);
                }
                Item note = storage.createItem();

                note.setKey(Long.toString(globalState.getKeyCounter(),16));
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
        };
        extractTask.execute(file);

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

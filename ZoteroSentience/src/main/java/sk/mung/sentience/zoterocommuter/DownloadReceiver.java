package sk.mung.sentience.zoterocommuter;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import java.io.File;

import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;


public class DownloadReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        DownloadManager downloadManager=(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

        File downloadDir = GlobalState.getInstance(context).getDownloadDirectory();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
        {
            Long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            Cursor cursor = downloadManager.query(query);
            assert cursor != null;
            if (cursor.moveToFirst())
            {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                String localFileName = cursor.getString(columnIndex);

                if(localFileName != null && localFileName.contains(downloadDir.getPath()))
                {
                    File downloadedFileParent = new File(new File(localFileName).getParent());
                    String itemKey = downloadedFileParent.getName();
                    Item item = GlobalState.getInstance(context).getStorage().findItemByKey(itemKey);

                    if(item != null)
                    {
                        columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(columnIndex);
                        if (DownloadManager.STATUS_SUCCESSFUL == status)
                        {
                            File file = new File(localFileName);
                            updateModificationTime(item, file);
                        }
                        else if (DownloadManager.STATUS_FAILED == status)
                        {
                            showFailureToast(context, cursor);
                        }
                    }
                }
            }
        }
    }

    private void showFailureToast(Context context, Cursor cursor)
    {
        int columnIndex;
        columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        if(HttpStatus.SC_NOT_FOUND == cursor.getInt(columnIndex))
        {
            Toast.makeText(
                    context,
                    R.string.attachment_removed,
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(
                    context,
                    R.string.network_error,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateModificationTime(Item item, File file)
    {
        Field modificationTime = item.getField(ItemField.MODIFICATION_TIME);
        Field hashField = item.getField(ItemField.MD5);
        if(modificationTime != null)
        {
            item.addField(Field.create(ItemField.DOWNLOAD_TIME, modificationTime.getValue()));
            item.addField(Field.create(ItemField.DOWNLOAD_MD5, hashField.getValue()));
            item.addField(Field.create(ItemField.LOCAL_TIME, Long.toString(file.lastModified())));
        }
    }
}

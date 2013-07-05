package sk.mung.sentience.zoterosentience.renderers;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;

import sk.mung.sentience.zoteroapi.entities.Field;
import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.ItemField;
import sk.mung.sentience.zoteroapi.entities.ItemType;
import sk.mung.sentience.zoterosentience.GlobalState;
import sk.mung.sentience.zoterosentience.R;

import static android.app.DownloadManager.STATUS_PENDING;

public class AttachmentRenderer
{
    private final LayoutInflater inflater;
    private final ItemRenderer renderer;
    private final ViewGroup parent;
    private final Activity context;
    private final DownloadManager downloadManager;
    private final File downloadDir;

    private View.OnClickListener downloadListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Integer position = (Integer)view.getTag(R.id.position);
            Item child = (Item) view.getTag(R.id.item_tag);
            downloadAttachment(child, view);
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
            {
                Long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                View childView = findViewByProgressTag(downloadId);

                if(childView != null)
                {
                    Item item = (Item) childView.getTag(R.id.item_tag);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = downloadManager.query(query);
                    assert cursor != null;
                    if (cursor.moveToFirst())
                    {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(columnIndex);
                        if (DownloadManager.STATUS_SUCCESSFUL == status)
                        {
                            updateModificationTime(item, cursor);
                        }
                        else if (DownloadManager.STATUS_FAILED == status)
                        {
                            showFailureToast(context, cursor);
                        }
                    }
                    cursor.close();
                    renderStatusIcon(childView, item);
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

        private void updateModificationTime(Item item, Cursor cursor)
        {
            int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
            String filename = cursor.getString(filenameIndex);
            File file = new File(filename);

            Field modificationTime = item.getField(ItemField.MODIFICATION_TIME);
            if(modificationTime != null)
            {
                //noinspection ResultOfMethodCallIgnored
                file.setLastModified(Long.valueOf(modificationTime.getValue()));
            }
        }
    };

    private View findViewByProgressTag(Long downloadId)
    {
        View childView = null;
        int count = parent.getChildCount();
        for(int ix=0; ix < count; ix++)
        {
            childView = parent.getChildAt(ix);
            assert childView != null;
            Long inProgressId = (Long) childView.getTag(R.id.tag_download_in_progress);
            if(downloadId.equals( inProgressId))
            {
                childView.setTag(R.id.tag_download_in_progress, -1L);
                break;
            }
            childView = null;
        }
        return childView;
    }

    public AttachmentRenderer(Activity context, ItemRenderer renderer, ViewGroup parent)
    {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.renderer = renderer;
        this.parent = parent;
        this.downloadManager=(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.downloadDir = ((GlobalState)this.context.getApplication()).getDownloadDirectory();

        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void onDestroy()
    {
        context.unregisterReceiver(receiver);
    }
    public void createView(Item child )
    {
        if(child.getItemType() == ItemType.ATTACHMENT)
        {
            View view = inflater.inflate(R.layout.listitem_item_attachment, parent, false);
            renderer.render(child,view);
            assert view != null;
            renderStatusIcon(view, child);
            view.setTag(R.id.item_tag,child);
            view.setOnClickListener(downloadListener);
            view.setBackgroundResource(R.drawable.selector);
            parent.addView(view);
        }

    }

    private boolean isDownloadInprogress(View view, Item item)
    {
        Long inProgressId = (Long) view.getTag(R.id.tag_download_in_progress);
        if(inProgressId != null)
        {
            return inProgressId > 0;
        }
        else
        {
            inProgressId = -1L;
            try
            {
                String uri = ((GlobalState)context.getApplication()).getZotero().getAttachmentUri(item).toString();

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PAUSED | STATUS_PENDING);
                Cursor cursor = downloadManager.query(query);
                assert cursor != null;
                assert uri!=null;
                while (cursor.moveToNext()) {
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
                    if ( uri.equals(cursor.getString(columnIndex)))
                    {
                        columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
                        inProgressId = cursor.getLong(columnIndex);
                    }
                }
                cursor.close();
            }
            catch (IOException e)
            {
                inProgressId = -1L;
            }
            view.setTag(R.id.tag_download_in_progress, inProgressId);
            return inProgressId > 0;
        }

    }

    private void renderStatusIcon(final View view, Item child)
    {
        Resources r = context.getResources();
        Drawable icon = r.getDrawable(R.drawable.ic_document_pdf);
        assert icon != null;
        final ImageView imageView = (ImageView) view.findViewWithTag("icon_status");

        if(isDownloadInprogress(view, child))
        {
            icon = r.getDrawable(R.drawable.animation_download);
            assert icon != null;
            icon.setAlpha(255);
            imageView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    AnimationDrawable frameAnimation =
                            (AnimationDrawable) imageView.getBackground();
                    assert frameAnimation != null;
                    frameAnimation.start();
                }
            });
        }
        else
        {
            String fileName =  child.getTitle();

            File file  = new File(downloadDir,child.getKey() + "/" + fileName);
            if (!file.exists())
            {
                icon.setAlpha(64);
            }
            else{icon.setAlpha(255);}
        }
        imageView.setBackground(icon);
    }

    private void downloadAttachment(Item item, View view)
    {
        GlobalState state = (GlobalState) context.getApplication();
        String fileName =  item.getTitle();
        File dir = new File(downloadDir, item.getKey() );
        assert dir != null;
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();

        File file  = new File(dir, fileName);
        if (file.exists())
        {
            showDownloadedAttachment(file, item);
            return;
        }
        try
        {
            if(isDownloadInprogress(view, item))
            {
                return;
            }
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                Toast.makeText(
                        context,
                        R.string.no_external_storage,
                        Toast.LENGTH_SHORT).show();
            }
            long lastDownload=
                    downloadManager.enqueue(
                            new DownloadManager.Request(state.getZotero().getAttachmentUri(item))
                                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                            DownloadManager.Request.NETWORK_MOBILE)
                                    .setAllowedOverRoaming(false)
                                    .setTitle(item.getTitle())
                                    .setDescription(context.getString(R.string.attachment_download_description))
                                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS + "/" + item.getKey(), fileName));
            view.setTag(R.id.tag_download_in_progress, lastDownload);
            renderStatusIcon(view, item);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(
                    context,
                    R.string.network_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showDownloadedAttachment(File file, Item item)
    {
        try
        {
            Field field = item.getField(ItemField.CONTENT_TYPE);
            String contentType = field == null ? null : field.getValue();
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), contentType);
            context.startActivity(intent);
        }
        catch (ActivityNotFoundException ex)
        {
            Toast.makeText(
                    context,
                    R.string.attachment_no_viewer,
                    Toast.LENGTH_SHORT).show();
        }
    }
}

package sk.mung.sentience.zoterocommuter.renderers;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sk.mung.sentience.zoterocommuter.GlobalState;
import sk.mung.sentience.zoterocommuter.R;
import sk.mung.zoteroapi.ZoteroSync;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.SyncStatus;

import static android.app.DownloadManager.STATUS_PENDING;

public class AttachmentRenderer
{
    public static final String IMPORTED_URL = "imported_url";
    public static final String LINKED_URL = "linked_url" ;
    public static final String LINKED_FILE = "linked_file" ;
    private final LayoutInflater inflater;
    private final ItemRenderer renderer;
    private final ViewGroup parent;
    private final FragmentActivity context;
    private final DownloadManager downloadManager;
    private final File downloadDir;

    private View.OnClickListener downloadListener = new View.OnClickListener() {

        @Override
        public void onClick(View view)
        {
            Item child = (Item) view.getTag(R.id.item_tag);
            assert child != null;
            Field linkMode = child.getField(ItemField.LINK_MODE);
            if(linkMode != null && LINKED_URL.equals(linkMode.getValue()))
            {
                Field url = child.getField(ItemField.URL);
                if(url != null)
                {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.getValue()));
                    context.startActivity(browserIntent);
                }
            }
            else if (linkMode == null || !LINKED_FILE.equals(linkMode.getValue())) {
                downloadAttachment(child, view);
            }
        }
    };


    private View.OnLongClickListener resolutionListener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View view) {
            Item target = (Item) view.getTag(R.id.item_tag);
            DialogFragment dialog = new AttachmentConflictFragment(target, view, new  AttachmentConflictFragment.Callback()
            {

                @Override
                public void attachmentStatusChanged(Item target, View view)
                {
                    renderStatus(target, view);
                }
            });
            dialog.show(context.getSupportFragmentManager(),"conflict_dialog");
            return true;
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, final Intent intent)
        {
            // execute delayed so that central receiver can update the modification time
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    String action = intent.getAction();
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)   )
                    {
                        Long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                        View childView = findViewByProgressTag(downloadId);

                        if(childView != null)
                        {
                            Item item = (Item) childView.getTag(R.id.item_tag);
                            renderStatus(item, childView);
                        }
                    }
                }
            }, 2000);

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

    public AttachmentRenderer(FragmentActivity context, ItemRenderer renderer, ViewGroup parent)
    {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.renderer = renderer;
        this.parent = parent;
        this.downloadManager=(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.downloadDir = getGlobalState().getDownloadDirectory();

        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private GlobalState getGlobalState()
    {
        return (GlobalState)this.context.getApplication();
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
            renderStatus(child, view);
            parent.addView(view);
        }
    }

    private void renderStatus(Item child, View view)
    {
        Resources resources = context.getResources();
        final ImageView imageView = (ImageView) view.findViewWithTag("icon_status");
        assert imageView != null;
        Field linkMode = child.getField(ItemField.LINK_MODE);
        if(linkMode != null && LINKED_URL.equals(linkMode.getValue()))
        {
            Drawable icon = resources.getDrawable(R.drawable.ic_url);
            imageView.setImageDrawable(icon);
        }
        else if(linkMode != null && LINKED_FILE.equals(linkMode.getValue()))
        {
            Drawable icon = resources.getDrawable(R.drawable.ic_linked_file);
            imageView.setImageDrawable(icon);
        }
        else
        {
            EditStatus status = getEditStatus(child);
            renderStatusIcon(view, child, status);
            if(status != EditStatus.REMOTE && status != EditStatus.EXTRACTING)
            {
                view.setOnLongClickListener(resolutionListener);
            }
        }

        view.setOnClickListener(downloadListener);
        view.setTag(R.id.item_tag, child);
        view.setBackgroundResource(R.drawable.selector);

        ImageView syncStatus = (ImageView) view.findViewById(R.id.sync_status);
        if(syncStatus != null)
        {
            syncStatus.setVisibility(child.getSynced() == SyncStatus.SYNC_OK ? View.GONE : View.VISIBLE);
            if(child.getSynced() == SyncStatus.SYNC_LOCALLY_UPDATED )
            {
                syncStatus.setBackgroundResource(R.drawable.blue_dot);
            }
            else if(child.getSynced() != SyncStatus.SYNC_OK )
            {
                syncStatus.setBackgroundResource(R.drawable.red_dot);
            }
        }
    }

    private boolean isDownloadInProgress(View view, Item item)
    {
        long inProgressId = -1L;
        try
        {
            String uri = (getGlobalState()).getZotero().getAttachmentUri(item).toString();

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PAUSED | STATUS_PENDING);
            Cursor cursor = downloadManager.query(query);
            assert cursor != null;
            assert uri!=null;
            while (cursor.moveToNext())
            {
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

    private void renderStatusIcon(final View view, Item child, EditStatus status)
    {
            Resources resources = context.getResources();
            Drawable icon = resolveAttachmentIcon(child, resources);
            assert icon != null;
            final ImageView imageView = (ImageView) view.findViewWithTag("icon_status");
            assert imageView != null;
            if(getGlobalState().isItemProcessed(child.getKey(),GlobalState.PROCESS_TEXT_EXTRACTION))
            {
                icon = resources.getDrawable(R.drawable.animation_extract);
                assert icon != null;
                imageView.setImageDrawable(icon);
                supportImageAlpha(imageView, 255);
                imageView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        AnimationDrawable frameAnimation =
                                (AnimationDrawable) imageView.getDrawable();
                        assert frameAnimation != null;
                        frameAnimation.start();
                    }
                });
            }
            else if(isDownloadInProgress(view, child))
            {
                icon = resources.getDrawable(R.drawable.animation_download);
                assert icon != null;
                imageView.setImageDrawable(icon);
                supportImageAlpha(imageView, 255);
                imageView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        AnimationDrawable frameAnimation =
                                (AnimationDrawable) imageView.getDrawable();
                        assert frameAnimation != null;
                        frameAnimation.start();
                    }
                });
            }
            else
            {
                imageView.setImageDrawable(icon);
                int alpha = EditStatus.REMOTE == status ? 64 : 255;
                supportImageAlpha(imageView, alpha);
            }
    }

    private void supportImageAlpha(ImageView imageView, int alpha) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            imageView.setImageAlpha(alpha);
        }
        else
        {
            //noinspection deprecation
            imageView.setAlpha(alpha);
        }
    }

    private Drawable resolveAttachmentIcon(Item child, Resources resources)
    {
        String fileName = ZoteroSync.getFileName(child, true);
        String ext = fileName.substring(fileName.lastIndexOf(".")+1);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getMimeTypeFromExtension(ext);
        if(type.contains("pdf"))
        {
            return resources.getDrawable(R.drawable.ic_document_pdf);
        }
        else return resources.getDrawable(R.drawable.ic_file_attachment);
    }

    private void downloadAttachment(Item item, View view)
    {
        try
        {
            GlobalState state = getGlobalState();
            String fileName = ZoteroSync.getFileName(item, true);
            File dir = new File(downloadDir, item.getKey() );

            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();

            File file  = new File(dir, fileName);
            if (file.exists())
            {
                showDownloadedAttachment(file, item);
                return;
            }
            if(isDownloadInProgress(view, item))
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
            int networkTypes = DownloadManager.Request.NETWORK_WIFI;
            SharedPreferences preferences = getGlobalState().getPreferences();
            if(preferences.getBoolean("mobile_download", false))
            {
                networkTypes = networkTypes | DownloadManager.Request.NETWORK_MOBILE;
            }

            String url =state.getZotero().getAttachmentUri(item).toString();
            long lastDownload=
                    downloadManager.enqueue(
                            new DownloadManager.Request(Uri.parse(url))
                                    .setAllowedNetworkTypes(networkTypes)
                                    .setAllowedOverRoaming(preferences.getBoolean("roaming_download", false))
                                    .setTitle(item.getTitle())
                                    .setDescription(context.getString(R.string.attachment_download_description))
                                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS + "/" + item.getKey(), fileName));
            view.setTag(R.id.tag_download_in_progress, lastDownload);
            renderStatus(item, view);
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
            Field linkMode = item.getField(ItemField.LINK_MODE);
            if(linkMode != null && IMPORTED_URL.equals(linkMode.getValue()))
            {
                file = decompressUrl(file,item);
            }

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

    private File decompressUrl(File file, Item item)
    {
        File targetDir = new File(file.getParent(),"content");
        File targetFile = new File(targetDir, ZoteroSync.getFileName(item, false));
        boolean isUnpacked = true;
        if(!targetFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            targetDir.mkdir();
            isUnpacked = unpackZip(targetDir, file);
        }
        return isUnpacked ? targetFile : file;
    }

    private boolean unpackZip(File targetDir, File zippedFile)
    {
        ZipInputStream zipInputStream;
        try
        {
            zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zippedFile)));
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            int count;

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                String filename = zipEntry.getName();

                if (zipEntry.isDirectory()) {
                    //noinspection ResultOfMethodCallIgnored
                    new File(targetDir,filename).mkdirs();
                }
                else
                {
                    FileOutputStream outputStream = new FileOutputStream(new File(targetDir, filename));
                    while ((count = zipInputStream.read(buffer)) != -1)
                    {
                        outputStream.write(buffer, 0, count);
                    }

                    outputStream.close();
                    zipInputStream.closeEntry();
                }
            }
            zipInputStream.close();
            return true;
        }

        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

    }

    private EditStatus getEditStatus(Item item)
    {
        String fileName = ZoteroSync.getFileName(item, true);
        File file = new File(downloadDir, item.getKey() + "/" + fileName );
        Field modificationField = item.getField(ItemField.MODIFICATION_TIME);
        Field downloadTimeField = item.getField(ItemField.DOWNLOAD_TIME);
        Field localTimeField = item.getField(ItemField.LOCAL_TIME);
        long serverModificationTime = 0;
        long downloadTime = 0;

        if(modificationField!= null)
        {
            serverModificationTime = Long.valueOf(modificationField.getValue());
        }
        if(downloadTimeField != null)
        {
            downloadTime = Long.valueOf(downloadTimeField.getValue());
        }

        EditStatus editStatus;

        if(!file.exists())
        {
            editStatus = EditStatus.REMOTE;
        }
        else if(SyncStatus.SYNC_ATTACHMENT_CONFLICT == item.getSynced())
        {
            editStatus = EditStatus.CONFLICT;
        }
        else
        {
            long localModificationTime;
            long localTime;
            if(localTimeField != null)
            {
                localTime = Long.valueOf(localTimeField.getValue());
            }
            else
            {
                localTime = downloadTime;
            }

            localModificationTime = file.lastModified();


            boolean isLocallyModified = Math.abs(localModificationTime - localTime) > ZoteroSync.MODIFICATION_TOLERANCE_MILISECONDS;
            boolean isServerModified = Math.abs(serverModificationTime - downloadTime) > ZoteroSync.MODIFICATION_TOLERANCE_MILISECONDS;

            if(!isLocallyModified && !isServerModified)
            {
                editStatus = EditStatus.SYNCED;
            }
            else if (!isLocallyModified )
            {
                editStatus = EditStatus.SERVER_UPDATE;
            }
            else if(!isServerModified )
            {
                editStatus = EditStatus.LOCAL_UPDATE;
            }
            else
            {
                editStatus = EditStatus.CONFLICT;
            }
        }
        return editStatus;
    }


}

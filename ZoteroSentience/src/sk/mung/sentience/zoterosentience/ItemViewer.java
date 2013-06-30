package sk.mung.sentience.zoterosentience;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.mung.sentience.zoteroapi.entities.Field;
import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.ItemField;
import sk.mung.sentience.zoteroapi.entities.ItemType;
import sk.mung.sentience.zoteroapi.entities.Tag;
import sk.mung.sentience.zoterosentience.renderers.FieldRenderer;
import sk.mung.sentience.zoterosentience.renderers.ItemRenderer;

import static android.app.DownloadManager.Query;
import static android.app.DownloadManager.STATUS_PENDING;

public class ItemViewer extends Fragment implements AdapterView.OnItemClickListener
{
    private Item item;
    private DownloadManager downloadManager=null;
    private Map<Long,Long> progressingDownloads = new HashMap<Long,Long>();
    private ItemRenderer renderer;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getActivity().registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        displayItems((ViewGroup) getView().findViewById(R.id.itemsGroup));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        downloadManager=(DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_item_viewer, container, false);
        renderer = new ItemRenderer(getActivity());

        assert view != null;
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        Item item = (Item) adapterView.getAdapter().getItem(position);
        downloadAttachment(item);
    }

    public void setItem(Item item)
    {
        this.item = item;
        if(item != null &&  getView() != null)
        {
            ViewGroup parent = (ViewGroup) getView().findViewById(R.id.itemsGroup);
            displayItems(parent);
        }
    }

    private void downloadAttachment(Item item)
    {
        GlobalState state = (GlobalState) getActivity().getApplication();
        String fileName =  item.getTitle();
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + item.getKey() );
        assert dir != null;
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();

        File file  = new File(dir,fileName);
        if (file.exists())
        {
            showDownloadedAttachment(file, item);
            return;
        }
        try
        {
            if(isDownloadInprogress(item))
            {
                return;
            }
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                Toast.makeText(
                        getActivity(),
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
                                    .setDescription(getString(R.string.attachment_download_description))
                                    .setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS + "/" + item.getKey(), fileName));
            this.progressingDownloads.put(lastDownload,item.getId());
            ViewGroup itemsView = (ViewGroup) getView().findViewById(R.id.itemsGroup);
            itemsView.removeAllViews();
            displayItems(itemsView);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(
                    getActivity(),
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
            startActivity(intent);
        }
        catch (ActivityNotFoundException ex)
        {
            Toast.makeText(
                    getActivity(),
                    R.string.attachment_no_viewer,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
            {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                if(progressingDownloads.containsKey(downloadId))
                {
                    progressingDownloads.remove(downloadId);
                    ViewGroup itemsView = (ViewGroup) getActivity().findViewById(R.id.itemsGroup);
                    itemsView.removeAllViews();
                    displayItems(itemsView);

                    Query query = new Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = downloadManager.query(query);
                    assert cursor != null;
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_FAILED == cursor.getInt(columnIndex))
                        {
                            columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                            if(HttpStatus.SC_NOT_FOUND == cursor.getInt(columnIndex))
                            {
                                Toast.makeText(
                                        getActivity(),
                                        R.string.attachment_removed,
                                        Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(
                                        getActivity(),
                                        R.string.network_error,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
        }
    };

    private boolean isDownloadInprogress( Item item)
    {
        if(progressingDownloads.containsValue(item.getId()))
        {
            return true;
        }
        else
        {
            try
            {
                String uri = ((GlobalState)getActivity().getApplication()).getZotero().getAttachmentUri(item).toString();

                Query query = new Query();
                query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PAUSED | STATUS_PENDING);
                Cursor cursor = downloadManager.query(query);
                assert cursor != null;
                assert uri!=null;
                while (cursor.moveToNext()) {
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
                    if ( uri.equals(cursor.getString(columnIndex)))
                    {
                        columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
                        progressingDownloads.put(cursor.getLong(columnIndex),item.getId());
                        return true;
                    }
                }
                return false;
            }
            catch (IOException e)
            {
                return false;
            }
        }

    }

    private void displayItems(ViewGroup parent)
    {
        if(item == null) return;

        renderer.render(item, getView().findViewById(R.id.headerGroup));

        StringBuilder tagsText = new StringBuilder();
        boolean isFirst = true;
        for(Tag tag : item.getTags())
        {
            if( tag.getTag() != null )
            {
                if(!isFirst)
                {
                    tagsText.append(getString(R.string.tags_separator));
                }
                tagsText.append(tag.getTag());
                isFirst = false;
            }
        }
        TextView tagsView = (TextView) getView().findViewById(R.id.textViewTags);
        if(tagsText.length() == 0)
        {
            tagsText.append(getString(R.string.tags_no_tags));
            tagsView.setTypeface(null, Typeface.ITALIC);
        }
        else tagsView.setTypeface(null, Typeface.NORMAL);

        tagsView.setText(tagsText.toString());

        View.OnClickListener downloadListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int position = (Integer)view.getTag(R.id.position);
                Item child = item.getChildren().get(position);
                downloadAttachment(child);
            }
        };

        View.OnClickListener editListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int position = (Integer)view.getTag(R.id.position);
                Item child = item.getChildren().get(position);
                editNote(child);
            }
        };
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int position = 0;
        for(Item child : item.getChildren())
        {
            if(position == 0)
            {
                inflater.inflate(R.layout.line, parent);
            }
            View view;
            if(child.getItemType() == ItemType.ATTACHMENT)
            {
                view = inflater.inflate(R.layout.listitem_item_attachment, parent, false);
                renderer.render(child,view);
                assert view != null;
                renderStatusIcon((ImageView)view.findViewById(R.id.icon_status), child);
                view.setTag(R.id.position,new Integer(position));
                view.setOnClickListener(downloadListener);
                view.setBackgroundResource(R.drawable.selector);

            }
            else if(child.getItemType() == ItemType.NOTE)
            {
                view = inflater.inflate(R.layout.listitem_item_note, parent, false);
                TextView noteView = (TextView) view.findViewById(R.id.textViewNote);
                noteView.setText(Html.fromHtml(child.getField(ItemField.NOTE).getValue()));
                view.setTag(R.id.position,new Integer(position));
                view.setOnClickListener(editListener);
                view.setBackgroundResource(R.drawable.selector);
            }
            else
            {
                view = inflater.inflate(R.layout.listitem_item, parent, false);
                renderer.render(child,view);
            }
            parent.addView(view);
            inflater.inflate(R.layout.line, parent);
            position++;
        }
        FieldRenderer renderer = new FieldRenderer(getActivity(), parent);
        List<Field> fields = renderer.sortFields(new ArrayList<Field>(item.getFields()));
        for(Field field : fields)
        {
            if(field == null || field.getValue() == null || field.getValue().isEmpty()) continue;
            View view =renderer.createView(field);
            if(view == null) continue;
            parent.addView(view);
        }
    }

    private void editNote(Item child)
    {
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + item.getKey() );
        assert dir != null;
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        String filename="note.html";
        File file  = new File(dir,filename);



        Intent intent = new Intent();

        try
        {
            FileWriter writer = new FileWriter(file);
            writer.write(child.getField(ItemField.NOTE).getValue());
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
         intent.setAction(Intent.ACTION_EDIT);
        intent.setDataAndType(Uri.fromFile(file), "text/html");
        try
        {
            startActivityForResult(intent,0);
        }
        catch (ActivityNotFoundException ex)
        {
            intent.setAction(Intent.ACTION_VIEW);
            try
            {
                startActivity(intent);
            }
            catch (ActivityNotFoundException ex2)
            {
                Toast.makeText(
                        getActivity(),
                        R.string.attachment_no_viewer,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void renderStatusIcon(final ImageView imageView, Item child)
    {
        Resources r = getResources();
        Drawable icon = r.getDrawable(R.drawable.ic_document_pdf);

        if(isDownloadInprogress(child))
        {
            icon = r.getDrawable(R.drawable.animation_download);
            icon.setAlpha(255);
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    AnimationDrawable frameAnimation =
                            (AnimationDrawable) imageView.getBackground();
                    frameAnimation.start();
                }
            });
        }
        else
        {
            String fileName =  child.getTitle();
            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + child.getKey() );
            File file  = new File(dir,fileName);
            if (!file.exists())
            {
                icon.setAlpha(64);
            }
            else{icon.setAlpha(255);}
        }
        imageView.setBackground(icon);
    }
}
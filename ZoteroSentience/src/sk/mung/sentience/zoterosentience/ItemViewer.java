package sk.mung.sentience.zoterosentience;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sk.mung.sentience.zoteroapi.entities.Field;
import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.ItemField;

import static android.app.DownloadManager.Query;
import static android.app.DownloadManager.STATUS_PENDING;

public class ItemViewer extends Fragment implements AdapterView.OnItemClickListener
{
    private Item item;
    private ItemListAdapter childrenAdapter;
    private FieldListAdapter fieldsAdapter;
    private DownloadManager downloadManager=null;
    private Map<Long,Long> progressingDownloads = new HashMap<Long,Long>();
    private ItemRenderer renderer;
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        downloadManager=(DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_item_viewer, container, false);
        renderer = new ItemRenderer(getActivity());
        childrenAdapter = new ItemListAdapter(getActivity(),R.layout.listitem_item_child);
        fieldsAdapter = new FieldListAdapter(getActivity());
        if(item != null)
        {
            childrenAdapter.setItems(item.getChildren());
            fieldsAdapter.setItems(item.getFields());

            renderer.render(item, view);
            displayItems((ViewGroup) view.findViewById(R.id.itemsGroup));
        }

        assert view != null;
//        ListView children = (ListView) view.findViewById(R.id.listViewChildren);
//        children.setAdapter(childrenAdapter);
//        children.setOnItemClickListener(this);
//
//        ListView fields = (ListView) view.findViewById(R.id.listViewFields);
//        fields.setAdapter(fieldsAdapter);

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
        if(item != null && childrenAdapter != null && fieldsAdapter != null)
        {
            childrenAdapter.setItems(item.getChildren());
            fieldsAdapter.setItems(item.getFields());
            ViewGroup parent = (ViewGroup) getActivity().findViewById(R.id.itemsGroup);
            displayItems(parent);
        }
    }

    private void downloadAttachment(Item item)
    {
        GlobalState state = (GlobalState) getActivity().getApplication();
        String fileName =  item.getTitle();
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + item.getKey() );
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
                                .setDestinationInExternalFilesDir(getActivity(),Environment.DIRECTORY_DOWNLOADS + "/" + item.getKey(),fileName));
            this.progressingDownloads.put(lastDownload,item.getId());
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

        View.OnClickListener childrenListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int position = (Integer)view.getTag(R.id.position);
                Item item = (Item) childrenAdapter.getItem(position);
                downloadAttachment(item);
            }
        };
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(int i = 0; i < childrenAdapter.getCount(); ++i)
        {
            View view = inflater.inflate(R.layout.listitem_item_child, parent, false);
            renderer.render(item.getChildren().get(i),view);
            view.setTag(R.id.position,new Integer(i));
            view.setOnClickListener(childrenListener);
            view.setBackgroundResource(R.drawable.selector);
            parent.addView(view);
            inflater.inflate(R.layout.line, parent);
        }
        for(int i = 0; i < fieldsAdapter.getCount(); ++i)
        {
            Field field = (Field) item.getFields().get(i);
            if(field == null) continue;
            View view = inflater.inflate(R.layout.listitem_field, parent, false);

            TextView textView = (TextView) view.findViewById(R.id.textViewFieldName);
            if( textView != null)
            {
                textView.setText(field.getType().getZoteroName());
            }
            textView = (TextView) view.findViewById(R.id.textViewFieldValue);
            if( textView != null)
            {
                textView.setText(field.getValue());
            }

            parent.addView(view);
            inflater.inflate(R.layout.line, parent);
        }
    }
}
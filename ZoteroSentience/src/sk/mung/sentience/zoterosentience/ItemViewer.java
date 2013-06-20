package sk.mung.sentience.zoterosentience;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import sk.mung.sentience.zoteroapi.entities.Field;
import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.ItemField;

public class ItemViewer extends Fragment implements AdapterView.OnItemClickListener
{
    private final Item item;

    public ItemViewer(Item item)
    {
        this.item = item;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_item_viewer, container, false);
        ItemRenderer renderer = new ItemRenderer(getActivity());
        renderer.render(item, view);

        ItemListAdapter adapter = new ItemListAdapter(getActivity(),R.layout.listitem_item_child);
        adapter.setItems(item.getChildren());
        assert view != null;
        ListView children = (ListView) view.findViewById(R.id.listViewChildren);
        children.setAdapter(adapter);
        children.setOnItemClickListener(this);
        return view;
    }

    private class OpenAttachmentTask extends AsyncTask<Item, Integer, File>
    {
        private String contentType = "";

        @Override
        protected File doInBackground(Item... items)
        {
            try
            {
                Item item = items[0];
                GlobalState state = (GlobalState) getActivity().getApplication();
                String fileName = item.getKey() + "_" + item.getTitle();
                Field field = item.getField(ItemField.CONTENT_TYPE);
                contentType = field == null ? null : field.getValue();
                File file  = state.getFileStreamPath(fileName);
                if (file.exists()) return file;

                URL url = state.getZotero().getAttachmentUrl(item);
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = getActivity().getApplicationContext().openFileOutput(
                        fileName,
                        Context.MODE_PRIVATE| Context.MODE_WORLD_READABLE);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                return file;
            }
            catch (Exception e)
            {
                //TODO: show toaster
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(File file)
        {
            if(file != null && contentType != null)
            {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), contentType);
                startActivity(intent);
            }
        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        Item item = (Item) adapterView.getAdapter().getItem(position);
        new OpenAttachmentTask().execute(item);
    }
}
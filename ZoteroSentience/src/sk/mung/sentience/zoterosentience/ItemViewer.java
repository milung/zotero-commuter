package sk.mung.sentience.zoterosentience;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import sk.mung.sentience.zoteroapi.entities.Item;

public class ItemViewer extends Fragment
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
        return view;
    }

}
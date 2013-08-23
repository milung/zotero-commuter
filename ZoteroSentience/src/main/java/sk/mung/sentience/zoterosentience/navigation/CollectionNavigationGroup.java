package sk.mung.sentience.zoterosentience.navigation;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.mung.sentience.zoterosentience.LibraryFragment;
import sk.mung.sentience.zoterosentience.R;
import sk.mung.sentience.zoterosentience.storage.ZoteroCollection;

public class CollectionNavigationGroup implements NavigationGroup
{
    private final String title;
    private final LayoutInflater inflater;
    private final Drawable openDrawable;
    private final Drawable closeDrawable;

    public CollectionNavigationGroup(String title, LayoutInflater inflater, Drawable openDrawable, Drawable closeDrawable)
    {
        this.title = title;
        this.inflater = inflater;
        this.openDrawable = openDrawable;
        this.closeDrawable = closeDrawable;
    }

    public void setChildren(List<ZoteroCollection> children)
    {
        this.children = children;
    }

    private List<ZoteroCollection> children = new ArrayList<ZoteroCollection>();

    @Override
    public Object getChild(int childPosition)
    {
        return children.get(childPosition);
    }

    @Override
    public long getChildId(int childPosition)
    {
        return children.get(childPosition).getId();
    }

    @Override
    public View getChildView(int childPosition, boolean lastChild, View convertView, ViewGroup parent)
    {
        ZoteroCollection collection = (ZoteroCollection)getChild(childPosition);
        return bindView(collection, convertView, parent,false, false);
    }

    @Override
    public int getChildrenCount()
    {
        return children.size();
    }

    @Override
    public String getName()
    {
        return title;
    }

    @Override
    public void childClicked(int childPosition, long id, LibraryFragment.Callbacks callbacks)
    {
        callbacks.onCollectionSelected(id);
    }

    @Override
    public boolean clicked(long id, LibraryFragment.Callbacks callbacks)
    {
        return false;
    }

    private View bindView(ZoteroCollection collection, View convertView, ViewGroup parent, boolean isGroup, boolean isExpanded) {
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.listitem_collection, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.title);
        textView.setText(collection.getName());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.indicator);
        if( !isGroup || collection.getChildren().size() == 0)
        {
            imageView.setImageDrawable(null);
        }
        else imageView.setImageDrawable( isExpanded ? closeDrawable : openDrawable);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
        params.leftMargin = 32 * collection.getNestedLevel();

        return convertView;
    }

}

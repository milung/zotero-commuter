package sk.mung.sentience.zoterosentience.navigation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sk.mung.sentience.zoterosentience.GlobalState;
import sk.mung.sentience.zoterosentience.R;

public class LoginGroup implements NavigationGroup
{
    private final LayoutInflater inflater;
    private final Drawable loginIcon;
    private final Drawable userIcon;
    private final String title;
    private final Context context;

    public LoginGroup(Context context, LayoutInflater inflater, String title, Drawable loginIcon, Drawable userIcon)
    {
        this.context = context;
        this.inflater = inflater;
        this.loginIcon = loginIcon;
        this.title = title;
        this.userIcon = userIcon;
    }

    @Override
    public View getGroupView(View convertView, boolean isExpanded) {
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.listitem_navigation_group, null);
        }
        assert convertView != null;
        TextView textView = (TextView) convertView.findViewById(R.id.title);
        GlobalState state = GlobalState.getInstance(context);
        String title = state.isUserLogged() ? state.getUserName() : getName();
        textView.setText(title);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.navigation_group_image);

        Drawable icon = state.isUserLogged() ? userIcon : loginIcon;
        imageView.setImageDrawable(icon);

        imageView = (ImageView) convertView.findViewById(R.id.indicator);
        imageView.setImageDrawable(null);
        return convertView;
    }

    @Override
    public Object getChild(int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int childPosition, boolean lastChild, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getChildrenCount() { return 0; }

    @Override
    public String getName() { return title; }

    @Override
    public void childClicked(int childPosition, long id, DrawerFragment.Callbacks callbacks) {}

    @Override
    public boolean clicked(long id, DrawerFragment.Callbacks callbacks) {
        callbacks.onLoginToZotero();
        return true;
    }

    @Override
    public boolean areChildrenSelectable() {
        return false;
    }

    @Override
    public boolean isGroupSelectable() {
        return true;
    }
}

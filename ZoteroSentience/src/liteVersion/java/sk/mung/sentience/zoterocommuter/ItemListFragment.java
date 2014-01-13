package sk.mung.sentience.zoterocommuter;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import sk.mung.sentience.zoterocommuter.storage.ItemsLoader;
import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;

/**
 * A fragment representing a single LibraryItem detail screen. This fragment is
 * either contained in a {@link MainActivity} in two-pane mode (on
 * tablets)
 */
public class ItemListFragment
        extends ItemListFragmentBase {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment()
    {}


}

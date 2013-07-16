package sk.mung.sentience.zoterosentience;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class ItemPagerActivity extends FragmentActivity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle arguments = getIntent().getExtras();

        setupActionBar(
                arguments == null ? null : arguments.getString(ItemPager.ARG_COLLECTION_NAME),
                arguments == null ? 0 : arguments.getInt(ItemPager.ARG_ITEMS_COUNT));
        ItemPager pager = new ItemPager();
        pager.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, pager)
                .commit();
    }

    private void setupActionBar(String name, int itemsCount)
    {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(name == null)
        {
            name = getString(R.string.my_library);
        }

        actionBar.setTitle(name);
        String itemsString = getResources().getQuantityString(R.plurals.number_of_items, itemsCount, itemsCount);
        actionBar.setSubtitle(itemsString);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
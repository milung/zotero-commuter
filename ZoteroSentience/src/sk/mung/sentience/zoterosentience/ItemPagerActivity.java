package sk.mung.sentience.zoterosentience;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class ItemPagerActivity extends FragmentActivity
{
    private ItemPager pager = null;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        Bundle bundle = null;
        if(savedInstanceState != null)  bundle = savedInstanceState;
        else                            bundle = getIntent().getExtras(); // 3

        if(bundle != null)
        {
            long id = bundle.getLong(ItemPager.ARG_COLLECTION_ID, 0);
            int position = bundle.getInt(ItemPager.ARG_CURRENT_POSITION, 0);

            setupActionBar(
                    bundle == null ? null : bundle.getString(ItemPager.ARG_COLLECTION_NAME),
                    bundle == null ? 0 : bundle.getInt(ItemPager.ARG_ITEMS_COUNT));
        }

        pager = new ItemPager();
        pager.setArguments( bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, pager)
                .commit();
}

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
        outState.putLong(ItemPager.ARG_COLLECTION_ID, pager.getCollectionId());
        outState.putInt(ItemPager.ARG_CURRENT_POSITION,pager.getPosition());
        outState.putString(ItemPager.ARG_COLLECTION_NAME,getIntent().getExtras().getString(ItemPager.ARG_COLLECTION_NAME));
        outState.putInt(ItemPager.ARG_ITEMS_COUNT,getIntent().getExtras().getInt(ItemPager.ARG_ITEMS_COUNT));
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
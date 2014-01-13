package sk.mung.sentience.zoterocommuter;

import android.view.MenuItem;

public class ItemViewer extends ItemViewerBase
{

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // handle menuItem selection
        switch (menuItem.getItemId()) {
            case R.id.tags:
                editTags();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void editTags()
    {

    }
}
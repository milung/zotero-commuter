package sk.mung.sentience.zoterocommuter;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.output.Bibliography;
import sk.mung.sentience.zoterocommuter.renderers.AttachmentRenderer;
import sk.mung.zoteroapi.ZoteroSync;
import sk.mung.zoteroapi.entities.CslItemProxy;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.ItemType;
import sk.mung.zoteroapi.entities.SyncStatus;
import sk.mung.zoteroapi.entities.Tag;

public class ItemViewer extends ItemViewerBase
{

    private ShareActionProvider shareActionProvider;

    @Override
    public void setItem(Item item)
    {
        super.setItem(item);
        if(item != null &&  shareActionProvider!=null)
        {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        if(shareItem!=null)
        {
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
            if(shareActionProvider != null && getItem()!=null)
            {
                shareActionProvider.setShareIntent(createShareIntent());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.tags:
                editTags();
                return true;
            case R.id.read_later:
                changeReadLaterFlag();
                getActivity().invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void changeReadLaterFlag()
    {
        Item item = getItem();
        GlobalState globalState = (GlobalState) getActivity().getApplication();
        Tag tag = globalState.getStorage().findTagByName( getString(R.string.read_later_tag));
        if(isItemInReadingQueue())
        {
            item.removeTag(tag);
        }
        else
        {
            item.addTag(tag);
        }
        item.setSynced(SyncStatus.SYNC_LOCALLY_UPDATED);
        globalState.getStorage().updateItem(item);
    }

    @Override
    protected void onTagsClicked(View view)
    {
        editTags();
    }

    private void editTags()
    {
        new TagsDialogFragment(getItem()).show(getFragmentManager(),"tagEditor");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        MenuItem readLaterItem = menu.findItem(R.id.read_later);
        boolean isMarked = isItemInReadingQueue();
        if(isMarked)
        {
            readLaterItem.setIcon(R.drawable.ic_read_later_remove);
        }
    }

    private boolean isItemInReadingQueue()
    {
        boolean isMarked = false;
        final String queueTag = getString(R.string.read_later_tag);
        Item item = getItem();
        if(item == null) return false;
        for(Tag tag : getItem().getTags())
        {
            if(queueTag.equals(tag.getTag()))
            {
                isMarked = true;
                break;
            }
        }
        return isMarked;
    }

    private Intent createShareIntent()
    {
        //need to "send multiple" to get more than one attachment
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getItem().getTitle());
        emailIntent.putExtra(Intent.EXTRA_TEXT, getItem().exportText());
        emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, getItem().exportText());
        //has to be an ArrayList
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for(Item child: getItem().getChildren())
        {
            if(child.getItemType() == ItemType.ATTACHMENT)
            {
                Field linkMode = child.getField(ItemField.LINK_MODE);
                if(!(linkMode != null && AttachmentRenderer.LINKED_URL.equals(linkMode.getValue())
                        || linkMode != null && AttachmentRenderer.LINKED_FILE.equals(linkMode.getValue())))
                {
                    GlobalState state = getGlobalState();
                    String fileName = ZoteroSync.getFileName(child, true);
                    File dir = new File(state.getDownloadDirectory(), child.getKey() );
                    File file  = new File(dir, fileName);
                    if (file.exists())
                    {
                        Uri u = Uri.fromFile(file);
                        uris.add(u);
                    }
                }
            }
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        return emailIntent;
    }

}
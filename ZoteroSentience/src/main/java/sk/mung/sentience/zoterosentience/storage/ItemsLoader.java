package sk.mung.sentience.zoterosentience.storage;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;


public class ItemsLoader extends AsyncTaskLoader<Cursor>
implements ZoteroStorageListener
{
    private ZoteroStorageImpl storage;
    private Cursor loadedItems;
    private boolean wasChanged = true;
    private final Long collectionId;

    public ItemsLoader(Context context, Long collectionId, ZoteroStorageImpl storage)
    {
        super(context);
        this.collectionId = collectionId;
        this.storage = storage;
        storage.addListener(this);
    }

    @Override
    public Cursor loadInBackground()
    {
        Cursor items = storage.findItemsCursorByCollectionId(collectionId == null ? 0 : collectionId);
        wasChanged = false;
        return items;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(Cursor items) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (items != null) {
                onReleaseResources(items);
            }
        }

        Cursor oldItems = null;
        if(items != loadedItems)
        {
            oldItems = loadedItems;
            loadedItems = items;
        }

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(items);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldItems != null)
        {
            onReleaseResources(oldItems);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {

        if (loadedItems != null && ! loadedItems.isClosed()) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(loadedItems);
        }

        if ( loadedItems == null || loadedItems.isClosed() || wasChanged) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(Cursor tree)
    {
        super.onCanceled(tree);
        onReleaseResources(tree);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (loadedItems != null) {
            onReleaseResources(loadedItems);
            loadedItems = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(Cursor items)
    {
        items.close();
    }

    @Override
    public void onCollectionsUpdated()
    {
    }

    @Override
    public void onItemsUpdated()
    {
        wasChanged = true;
        onContentChanged();
    }

    @Override
    public void onTagsUpdated()  {}

}

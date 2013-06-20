package sk.mung.sentience.zoterosentience.storage;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class CollectionsTreeLoader extends AsyncTaskLoader<ZoteroCollection> 
    implements ZoteroStorageListener
{
    private ZoteroStorage storage;
    private ZoteroCollection loadedTree;
    private boolean wasChanged = true;
    
    public CollectionsTreeLoader(Context context, ZoteroStorage storage)
    {
        super(context);
        this.storage = storage;
        storage.addListener(this);
    }

    @Override
    public ZoteroCollection loadInBackground()
    {
    	ZoteroCollection collection = storage.getCollectionTree();
        wasChanged = false;
        return collection;
    }
    
    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(ZoteroCollection tree) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (tree != null) {
                onReleaseResources(tree);
            }
        }
        ZoteroCollection oldTree = loadedTree;
        loadedTree = tree;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(tree);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldTree != null) {
            onReleaseResources(oldTree);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (loadedTree != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(loadedTree);
        }

        if ( loadedTree == null || wasChanged) {
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
    @Override public void onCanceled(ZoteroCollection tree) 
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
        if (loadedTree != null) {
            onReleaseResources(loadedTree);
            loadedTree = null;
        }

     }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    @SuppressWarnings("UnusedParameters")
    protected void onReleaseResources(ZoteroCollection tree)
    {
        // noop
    }

    @Override
    public void onCollectionsUpdated()
    {
        wasChanged = true;
        onContentChanged();
    }

    @Override
    public void onItemsUpdated()
    {

    }

}

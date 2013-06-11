package sk.mung.sentience.zoterosentience.storage;

public interface ZoteroStorageListener
{
    public void onCollectionsUpdated();

    void onItemsUpdated();
}

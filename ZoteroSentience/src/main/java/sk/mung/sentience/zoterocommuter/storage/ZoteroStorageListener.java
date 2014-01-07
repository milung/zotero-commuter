package sk.mung.sentience.zoterocommuter.storage;

public interface ZoteroStorageListener
{
    public void onCollectionsUpdated();

    void onItemsUpdated();

    void onTagsUpdated();
}

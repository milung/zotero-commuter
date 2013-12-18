package sk.mung.zoteroapi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.SyncStatus;


public interface ZoteroStorage
{
    int getCollectionsVersion();
    void setCollectionsVersion(int lastModifiedVersion);

    int getDeletionsVersion();
    void setDeletionsVersion(int lastModifiedVersion);

    int getItemsVersion();
    void setItemsVersion(int lastModifiedVersion);

    void updateCollections(@NotNull Iterable<CollectionEntity> collections);
    void updateItems(@NotNull Iterable<Item> items);
    void updateItem(@NotNull Item item);

    void deleteCollections(@NotNull Iterable<String> collections);
    void deleteItems(@NotNull Iterable<String> items);
    void deleteTags(@NotNull Iterable<String> tags);

    List<Item> findItemsBySynced(@NotNull SyncStatus syncStatus);
    Item findItemByKey(@NotNull String key);

    Item createItem();

    Field createField();

    /// called after syncing to ensure local caches are reloaded
    void clearCaches();
}

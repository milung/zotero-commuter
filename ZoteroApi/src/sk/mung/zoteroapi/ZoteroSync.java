package sk.mung.zoteroapi;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;
import sk.mung.zoteroapi.entities.SyncStatus;

public class ZoteroSync
{
    private final ZoteroStorage storage;
    private final Zotero zotero;
    private final File downloadDir;
    
    public ZoteroSync(ZoteroStorage storage, Zotero zotero, File downloadDir)
    {
        this.storage = storage;
        this.zotero = zotero;
        this.downloadDir = downloadDir;
    }
    
    private void syncCollections() throws IOException, XmlPullParserException, URISyntaxException
    {
        int version = storage.getCollectionsVersion();
        Map<String, Integer> collectionVersions = zotero.getCollectionsVersions(version);
        int lastModifiedVersion = zotero.getLastModifiedVersion();
        if( collectionVersions.size()> 0 )
        {
            List<CollectionEntity> collections 
                = zotero.getCollections(collectionVersions.keySet(), 0, -1);  
            storage.updateCollections( collections);
        }
        
        storage.setCollectionsVersion(lastModifiedVersion);
    }
    
    private void syncDeletions() throws IOException, XmlPullParserException, URISyntaxException
    {
        int version = storage.getDeletionsVersion();
        Map<String, List<String>> deletions = zotero.getDeletions(version);
        int lastModifiedVersion = zotero.getLastModifiedVersion();
        
        if( deletions.containsKey("collections") )
        {
        	List<String> collections = deletions.get("collections");
            if(collections.size() > 0)
            {
                storage.deleteCollections( collections);
            }
        }

        if( deletions.containsKey("items") )
        {
            List<String> items = deletions.get("items");
            if(items.size() > 0)
            {
                storage.deleteItems( items);
            }
        }

        if( deletions.containsKey("tags") )
        {
            List<String> tags = deletions.get("tags");
            if(tags.size() > 0)
            {
                storage.deleteTags(tags);
            }
        }
        
        storage.setDeletionsVersion(lastModifiedVersion);
    }

    private void syncItems() throws IOException, XmlPullParserException, URISyntaxException
    {
        int version = storage.getItemsVersion();
        Map<String, Integer> itemVersions = zotero.getItemsVersions(version);
        int lastModifiedVersion = zotero.getLastModifiedVersion();
        if( itemVersions.size()> 0 )
        {
            List<Item> items
                    = zotero.getItems(itemVersions.keySet(), 0, -1);
            storage.updateItems( items);
        }

        storage.setItemsVersion(lastModifiedVersion);
    }

    public void fullSync() throws IOException, XmlPullParserException, URISyntaxException
    {
        int collectionsVersion;
        int deletionsVersion;
        do
        {
            syncCollections();
            syncItems();
            syncDeletions();
            collectionsVersion = storage.getCollectionsVersion();
            deletionsVersion = storage.getDeletionsVersion();
        } while( collectionsVersion != deletionsVersion);
        uploadUpdatedItems();
        uploadAttachments();
    }

    private void uploadUpdatedItems()
    {
        List<Item> items =  storage.findItemsBySynced(SyncStatus.SYNC_LOCALLY_UPDATED);
        List<UploadStatus> status = zotero.updateItems(items, storage.getItemsVersion());
        storage.setItemsVersion(zotero.getLastModifiedVersion());
    }

    private void uploadAttachments()
    {
        List<ItemFilePair> pairs =  scanForUpdates();
        for( ItemFilePair pair : pairs)
        {
            UploadStatus status = zotero.uploadAttachment(pair.getFile(), pair.getItem());
            if(status == UploadStatus.SUCCESS)
            {
                pair.getItem()
                        .getField(ItemField.MODIFICATION_TIME)
                        .setValue(Long.toString(pair.getFile().lastModified()));
            }
            else if( status == UploadStatus.UPDATE_CONFLICTS)
            {
                pair.getItem().setSynced(SyncStatus.SYNC_ATTACHMENT_CONFLICT);
            }
            else if(status == UploadStatus.STORAGE_EXCEEDED)
            {
                pair.getItem().setSynced(SyncStatus.SYNC_ATTACHMENT_TOO_BIG);
            }
            else if(status == UploadStatus.NOT_AUTHORIZED)
            {
                pair.getItem().setSynced(SyncStatus.SYNC_ATTACHMENT_NOT_AUTHORIZED);
            }
        }
    }

    private class ItemFilePair
    {
        private final File file;
        private final Item item;


        private ItemFilePair(File file, Item item)
        {
            this.file = file;
            this.item = item;
        }

        private File getFile()
        {
            return file;
        }

        private Item getItem()
        {
            return item;
        }
    }
    private List<ItemFilePair> scanForUpdates()
    {
        List<ItemFilePair> pairs = new ArrayList<ItemFilePair>();
        if(downloadDir.exists())
        {
            File[] keys = downloadDir.listFiles();
            if(null!=keys)
            {
                for(File keyDir : keys)
                {
                    File[] attachments = keyDir.listFiles();
                    if(null != attachments && attachments.length > 0)
                    {
                        long modificationTime = attachments[0].lastModified();
                        Item item = storage.findItemByKey(keyDir.getName());
                        if(item != null)
                        {
                            Field field = item.getField(ItemField.MODIFICATION_TIME);
                            if(field != null)
                            {
                                long libraryTime = Long.valueOf(field.getValue());
                                if(modificationTime > libraryTime)
                                {
                                    pairs.add(new ItemFilePair(attachments[0],item));
                                }
                            }
                        }
                    }

                }
            }
        }
        return pairs;
    }
}

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
    public static final String IMPORTED_URL = "imported_url";
    public static final int MODIFICATION_TOLERANCE_MILISECONDS = 3000;
    private final ZoteroStorage storage;
    private final Zotero zotero;
    private final File downloadDir;
    
    public ZoteroSync(ZoteroStorage storage, Zotero zotero, File downloadDir)
    {
        this.storage = storage;
        this.zotero = zotero;
        this.downloadDir = downloadDir;
    }
    
    private void syncCollections() throws IOException, XmlPullParserException {
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
    
    private void syncDeletions() throws IOException {
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

    private void syncItems() throws IOException, XmlPullParserException {
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
        uploadItems(SyncStatus.SYNC_LOCALLY_UPDATED);
    }

    private void uploadItems(SyncStatus status)
    {
        List<Item> items =  storage.findItemsBySynced(status);
        zotero.updateItems(items, storage.getItemsVersion());
        storage.setItemsVersion(zotero.getLastModifiedVersion());
    }

    private void uploadAttachments()
    {
        List<ItemFilePair> pairs =  scanForUpdates();
        for( ItemFilePair pair : pairs)
        {
            Item item = pair.getItem();

            // do not try upload imported urls - they are not editable
            Field linkMode = item.getField(ItemField.LINK_MODE);
            if(linkMode != null && IMPORTED_URL.equals(linkMode.getValue()))
            {
                continue;
            }
            UploadStatus status = zotero.uploadAttachment(pair.getFile(), item);
            if(status == UploadStatus.SUCCESS)
            {
                String lastModified = Long.toString(pair.getFile().lastModified());

                item.addField(Field.create(ItemField.MODIFICATION_TIME, lastModified));
                item.addField(Field.create(ItemField.DOWNLOAD_TIME, lastModified));
                item.addField(Field.create(ItemField.LOCAL_TIME, lastModified));
                item.addField(Field.create(ItemField.DOWNLOAD_MD5,zotero.calculateFileHash(pair.getFile())));
                item.setSynced(SyncStatus.SYNC_OK);
            }
            else if( status == UploadStatus.UPDATE_CONFLICTS)
            {
                item.setSynced(SyncStatus.SYNC_ATTACHMENT_CONFLICT);
            }
            else if(status == UploadStatus.STORAGE_EXCEEDED)
            {
                item.setSynced(SyncStatus.SYNC_ATTACHMENT_TOO_BIG);
            }
            else if(status == UploadStatus.NOT_AUTHORIZED)
            {
                item.setSynced(SyncStatus.SYNC_ATTACHMENT_NOT_AUTHORIZED);
            }
        }
    }

    public void deleteAllAttachments()
    {
          //noinspection ResultOfMethodCallIgnored
        deleteDirectory(downloadDir);
    }


    private boolean deleteDirectory(File directory)
    {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for (File file : files)
                {
                    if (file.isDirectory())
                    {
                        deleteDirectory(file);
                    } else
                    {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
        return(directory.delete());
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

    public void deleteAttachment(Item item)
    {
        File dir = new File(downloadDir,item.getKey());
        deleteDirectory(dir);
    }

    public static String getFileName(Item item, boolean withSuffix)
    {

        String fileName = item.getKey();
        Field fileNameField = item.getField(ItemField.FILE_NAME);
        if(fileNameField != null)
        {
            fileName = fileNameField.getValue();
        }

        Field linkMode = item.getField(ItemField.LINK_MODE);
        String suffix = "";
        if( withSuffix && linkMode != null && IMPORTED_URL.equals(linkMode.getValue()))
        {
            suffix = ".zip";
        }

        return fileName + suffix;
    }

    private List<ItemFilePair> scanForUpdates()
    {
        List<ItemFilePair> pairs = new ArrayList<ItemFilePair>();
        if(downloadDir!=null && downloadDir.exists())
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
                            Field field = item.getField(ItemField.LOCAL_TIME);
                            if(field == null)
                            {
                                //TODO: remove for the release - only backward compatibility for testing
                                field = item.getField(ItemField.DOWNLOAD_TIME);
                        }
                            long libraryTime = field != null ? Long.valueOf(field.getValue()):0L ;
                            if((modificationTime - libraryTime) > MODIFICATION_TOLERANCE_MILISECONDS)
                            {
                                pairs.add(new ItemFilePair(attachments[0],item));
                            }
                        }
                    }
                }
            }
        }
        return pairs;
    }
}

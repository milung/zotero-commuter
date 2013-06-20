package sk.mung.sentience.zoterosentience.storage;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import sk.mung.sentience.zoteroapi.Zotero;
import sk.mung.sentience.zoteroapi.entities.CollectionEntity;
import sk.mung.sentience.zoteroapi.entities.ItemEntity;

public class ZoteroSync
{
    private ZoteroStorage storage;
    private Zotero zotero;
    
    public ZoteroSync(ZoteroStorage storage, Zotero zotero)
    {
        this.storage = storage;
        this.zotero = zotero;
    }
    
    public void syncCollections() throws IOException, XmlPullParserException
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
    
    public void syncDeletions() throws IOException, XmlPullParserException
    {
        int version = storage.getDeletionsVersion();
        Map<String, List<String>> deletions = zotero.getDeletions(version);
        int lastModifiedVersion = zotero.getLastModifiedVersion();
        
        if( deletions.containsKey("collections") )
        {
        	List<String> collections = deletions.get("collections");
              
            storage.deleteCollections( collections);
        }
        
        storage.setDeletionsVersion(lastModifiedVersion);
    }

    public void syncItems() throws IOException, XmlPullParserException
    {
        int version = storage.getItemsVersion();
        Map<String, Integer> itemVersions = zotero.getItemsVersions(version);
        int lastModifiedVersion = zotero.getLastModifiedVersion();
        if( itemVersions.size()> 0 )
        {
            List<ItemEntity> items
                    = zotero.getItems(itemVersions.keySet(), 0, -1);
            storage.updateItems( items);
        }

        storage.setItemsVersion(lastModifiedVersion);
    }


}

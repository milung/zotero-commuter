package sk.mung.sentience.zoterosentience.storage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import sk.mung.sentience.zoteroapi.Zotero;
import sk.mung.sentience.zoteroapi.CollectionEntity;

public class ZoteroSync
{
    private ZoteroStorage database;
    private Zotero zotero;
    
    public ZoteroSync(ZoteroStorage database, Zotero zotero)
    {
        this.database = database;
        this.zotero = zotero;
    }
    
    public void syncCollections() throws IOException, XmlPullParserException
    {
        int version = database.getCollectionsVersion();
        Map<String, Integer> collectionVersions = zotero.getCollectionsVersions(version);
        int lastModifiedVersion = zotero.getLastModifiedVersion();
        if( collectionVersions.size()> 0 )
        {
            List<CollectionEntity> collections 
                = zotero.getCollections(collectionVersions.keySet(), 0, -1);  
            database.updateCollections( collections);
        }
        
        database.setCollectionsVersion(lastModifiedVersion);
    }
    
    public void syncDeletions() throws IOException, XmlPullParserException
    {
        int version = database.getDeletionsVersion();
        Map<String, List<String>> deletions = zotero.getDeletions(version);
        int lastModifiedVersion = zotero.getLastModifiedVersion();
        
        if( deletions.containsKey("collections") )
        {
        	List<String> collections = deletions.get("collections");
              
            database.deleteCollections( collections);
        }
        
        database.setDeletionsVersion(lastModifiedVersion);
    }
}

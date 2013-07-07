package sk.mung.sentience.zoteroapi.parsers;

import java.util.Map;

import sk.mung.sentience.zoteroapi.entities.CollectionEntity;
import sk.mung.sentience.zoteroapi.entities.SyncStatus;

public class CollectionParser extends AbstractAtomParser<CollectionEntity>
{
    @Override
    protected CollectionEntity processContent( Map<String, Object> content)
    {
        CollectionEntity result = new CollectionEntity();
        result.setName( content.get("name").toString());
        result.setKey( content.get("collectionKey").toString());
        String version = content.get("collectionVersion").toString();
        result.setVersion(Integer.parseInt(version));
        
        String parent =content.get("parentCollection").toString();
        if(!parent.equalsIgnoreCase("false"))
        {
            result.setParentKey(parent);
        }
        result.setSynced(SyncStatus.SYNC_OK);
        return result;
    }
}

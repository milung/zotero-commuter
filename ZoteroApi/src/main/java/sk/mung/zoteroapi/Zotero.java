package sk.mung.zoteroapi;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.http.HttpStatus;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sk.mung.zoteroapi.entities.CollectionEntity;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.KeyEntity;
import sk.mung.zoteroapi.entities.SyncStatus;
import sk.mung.zoteroapi.parsers.AbstractAtomParser;
import sk.mung.zoteroapi.parsers.CollectionParser;
import sk.mung.zoteroapi.parsers.ItemParser;

public class Zotero
{    
    private static final String ITEMS = "items";
    private static final String ITEM_KEY = "itemKey";
	private static final String COLLECTIONS = "collections";
    private static final String COLLECTION_KEY = "collectionKey";

	private static final int CHUNK_SIZE = 50;
    private static final String TAG = "Zotero";

    private ZoteroRestful restful;
    private final ObjectMapper mapper = new ObjectMapper();
        
    public void setRestfull( ZoteroRestful restful) { this.restful = restful; }   
    
	public Map<String, Integer> getCollectionsVersions(Integer sinceVersion)
            throws IOException
    {
		return getVersions(COLLECTIONS, sinceVersion);
    }
	
	public Map<String, Integer> getItemsVersions(Integer sinceVersion)
            throws IOException
    {
		return getVersions(ITEMS, sinceVersion);
    }

	private Map<String, Integer> getVersions(String sectionName,
			Integer sinceVersion) throws IOException
    {
		ZoteroRestful.Response response = restful.callCurrentUserApi(
            sectionName, 
            new String[][]{ 
                    {"format", "versions"}, 
                    {"newer", sinceVersion.toString()}},
            sinceVersion);
        if(response.StatusCode == HttpStatus.SC_NOT_MODIFIED)
        {
        	return  new HashMap<String, Integer>();
        }
        else
        {
	        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally  
	        Iterator<Map.Entry<String,JsonNode>> fields 
	        	= mapper.readTree(response.ResponseString).fields();
	        Map<String, Integer> result = new HashMap<String, Integer>();
	        while( fields.hasNext())
	        {
	        	Map.Entry<String,JsonNode> node = fields.next();
	        	result.put( node.getKey(), node.getValue().asInt());
	        }
	        return result;
        }
	}
        
    public List<CollectionEntity> getCollections(
    		Collection<String> versions, int startPosition, int endPosition )
            throws IOException, XmlPullParserException
    {   	
        return loadEntities(
        		COLLECTIONS, COLLECTION_KEY, versions, startPosition, endPosition, new CollectionParser());
    }

    public List<Item> getItems(
            Collection<String> versions, int startPosition, int endPosition )
            throws IOException, XmlPullParserException
    {
        return loadEntities(
                ITEMS, ITEM_KEY, versions, startPosition, endPosition, new ItemParser());
    }

	private <T> List<T> loadEntities(
			String section,String sectionKey,
			Collection<String> versions, int startPosition, int endPosition,
			AbstractAtomParser<T> parser)
            throws IOException, XmlPullParserException
    {
		List<T> entities = new ArrayList<T>();
        if( endPosition < 0 ) { endPosition = versions.size(); }
        
        Iterator<String> iterator = versions.iterator();
        
        for(int chunk = 0; chunk < (endPosition - startPosition) / CHUNK_SIZE + 1; ++chunk)
        {
            StringBuilder builder = new StringBuilder();
            String separator = ""; 
            for( int position = startPosition + chunk * CHUNK_SIZE; 
                    position < startPosition + (chunk + 1) * CHUNK_SIZE && position < endPosition && iterator.hasNext();
                    ++position)
            {
                builder.append(separator).append(iterator.next());
                separator = ",";
            }
            ZoteroRestful.Response response = restful.callCurrentUserApi(
            		section, 
            		new String[][] {
	                    {"format","atom"},
	                    {"content","json"},
	                    {sectionKey,builder.toString()}},
            		0);            
            entities.addAll( parser.parse(response.ResponseString));            
        }        
        
        return entities;
	}

	public int getLastModifiedVersion() {
		
		return restful.getLastModifiedVersion();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, List<String>> getDeletions(int sinceVersion) throws IOException
    {
		ZoteroRestful.Response response = restful.callCurrentUserApi(
	            "deleted", 
	            new String[][]{ 
	                    {"newer", Integer.toString(sinceVersion)}},
	            sinceVersion);
	        if(response.StatusCode == HttpStatus.SC_NOT_MODIFIED)
	        {
	        	return  new HashMap<String, List<String>>();
	        }
	        else
	        {
		        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally               
		        Map ids =  mapper.readValue(response.ResponseString, Map.class); //new TypeReference<Map<String,Integer>>() {});
		        Map<String, List<String>> result = new HashMap<String, List<String>>();
		        for( Object entry : ids.entrySet())
		        {
		        	result.put( 
		        			(String)((Map.Entry)entry).getKey(), 
		        			(List<String>) ((Map.Entry)entry).getValue());
		        }
		        return result;
	        }
	}

    public URI getAttachmentUri(Item item) throws IOException
    {
        return restful.getAttachmentUri(item);
    }

    public UploadStatus uploadAttachment(File file, Item item)
    {
        return restful.uploadAttachment(file, item);
    }

    public String calculateFileHash(File file)
    {
        return restful.calculateFileHash(file);
    }

    public List<UploadStatus> updateItems(List<Item> items, int sinceVersion)
    {
        if(items.size() == 0)
        {
            return new ArrayList<UploadStatus>();
        }
        UploadStatus status[] = new UploadStatus[items.size()];
        Arrays.fill(status,UploadStatus.NETWORK_ERROR);

        try
        {
            String jsonStruct = multiUploadToJson(items);

            ZoteroRestful.Response response = restful.uploadEntities(
                    jsonStruct, ITEMS, sinceVersion);
            processUploadStatus(items, status, response);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return Arrays.asList(status);
    }

    private String multiUploadToJson(List<Item> items) throws IOException
    {
        StringWriter writer = new StringWriter();
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createGenerator(writer);
        ItemParser itemParser = new ItemParser();

        g.writeStartObject();
        {
            g.writeArrayFieldStart("items");
            for(Item item : items)
            {
                itemParser.itemToJson(item, g);
            }
            g.writeEndArray(); //items
        }
        g.writeEndObject(); // result
        g.flush();
        g.close();
        return writer.getBuffer().toString();
    }

    private void processUploadStatus(List<Item> items, UploadStatus[] status, ZoteroRestful.Response response) throws IOException {
        int version = getLastModifiedVersion();
        if(response.StatusCode == HttpStatus.SC_OK)
        {

            JsonNode root =  mapper.readValue(response.ResponseString, JsonNode.class);
            JsonNode success = root.get("success");
            if(success != null)
            {
                Iterator<Map.Entry<String,JsonNode>> elements = success.fields();
                while( elements.hasNext() )
                {
                    Map.Entry<String,JsonNode> el = elements.next();
                    int ix = Integer.valueOf(el.getKey());
                    status[ix] = UploadStatus.SUCCESS;
                    Item item = items.get(ix);
                    item.setKey(el.getValue().asText());
                    item.setSynced(SyncStatus.SYNC_OK);
                    item.setVersion(version);
                }
            }
            JsonNode unchanged = root.get("unchanged");
            if(unchanged != null)
            {
                Iterator<Map.Entry<String,JsonNode>> elements = unchanged.fields();
                while( elements.hasNext() )
                {
                    Map.Entry<String,JsonNode> el = elements.next();
                    int ix = Integer.valueOf(el.getKey());
                    status[ix] = UploadStatus.SUCCESS;
                    Item item = items.get(ix);
                    item.setSynced(SyncStatus.SYNC_OK);
                    item.setVersion(version);
                }
            }

            JsonNode failed = root.get("failed");
            if(unchanged != null)
            {
                Iterator<Map.Entry<String,JsonNode>> elements = failed.fields();
                while( elements.hasNext() )
                {
                    Map.Entry<String,JsonNode> el = elements.next();
                    int ix = Integer.valueOf(el.getKey());
                    JsonNode failure = el.getValue();
                    Item item = items.get(ix);
                    switch( failure.get("code").asInt())
                    {
                        case HttpStatus.SC_OK:
                            status[ix] = UploadStatus.SUCCESS;
                            item.setSynced(SyncStatus.SYNC_OK);
                            item.setVersion(version);
                            break;
                        case HttpStatus.SC_PRECONDITION_FAILED:
                            status[ix] = UploadStatus.UPDATE_CONFLICTS;
                            item.setSynced(SyncStatus.SYNC_CONFLICT);
                            break;
                        default:
                            status[ix] = UploadStatus.NETWORK_ERROR;
                            break;
                    }
                }
            }
        }
    }

    public List<UploadStatus> deleteItems(List<Item> items, int sinceVersion)
    {
        if(items.size() == 0)
        {
            return new ArrayList<UploadStatus>(0);
        }


        try
        {
            return deleteEntities(ITEMS, ITEM_KEY, items, sinceVersion);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        UploadStatus status[] = new UploadStatus[items.size()];
        Arrays.fill(status,UploadStatus.NETWORK_ERROR);
        return Arrays.asList(status);
    }

    private <T extends KeyEntity> List<UploadStatus> deleteEntities(
            String section, String sectionKey,List<T> entities,
            int sinceVersion)
            throws IOException
    {

        UploadStatus status[] = new UploadStatus[entities.size()];
        Arrays.fill(status,UploadStatus.NETWORK_ERROR);
        int ix = 0;

        for( T entity : entities)
        {

            ZoteroRestful.Response response = restful.deleteEntities(
                    entity.getKey(), section, sectionKey, sinceVersion);

            if(response.StatusCode == HttpStatus.SC_NO_CONTENT)
            {
                status[ix] = UploadStatus.SUCCESS;
            }
            ix++;
        }
        return Arrays.asList(status);
    }
}

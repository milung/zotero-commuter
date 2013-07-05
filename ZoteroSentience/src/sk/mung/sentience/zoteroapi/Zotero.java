package sk.mung.sentience.zoteroapi;

import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.xmlpull.v1.XmlPullParserException;

import sk.mung.sentience.zoteroapi.entities.CollectionEntity;
import sk.mung.sentience.zoteroapi.entities.Item;
import sk.mung.sentience.zoteroapi.entities.ItemEntity;
import sk.mung.sentience.zoteroapi.parsers.AbstractAtomParser;
import sk.mung.sentience.zoteroapi.parsers.CollectionParser;
import sk.mung.sentience.zoteroapi.parsers.ItemParser;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Zotero
{    
    private static final String ITEMS = "items";

	private static final String COLLECTIONS = "collections";

	private static final int CHUNK_SIZE = 50;
    
    private ZoteroRestful restful;
        
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
			Integer sinceVersion) throws IOException, ClientProtocolException,
			JsonProcessingException {
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
        		COLLECTIONS, versions, startPosition, endPosition, new CollectionParser());
    }

    public List<ItemEntity> getItems(
            Collection<String> versions, int startPosition, int endPosition )
            throws IOException, XmlPullParserException
    {
        return loadEntities(
                ITEMS, versions, startPosition, endPosition, new ItemParser());
    }

	private <T> List<T> loadEntities(
			String section,
			Collection<String> versions, int startPosition, int endPosition,
			AbstractAtomParser<T> parser) 
					throws IOException, ClientProtocolException, XmlPullParserException 
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
                builder.append(separator + iterator.next());
                separator = ",";
            }
            ZoteroRestful.Response response = restful.callCurrentUserApi(
            		section, 
            		new String[][] {
	                    {"format","atom"},
	                    {"content","json"},
	                    {"itemKey",builder.toString()}},
            		0);            
            entities.addAll( parser.parse(response.ResponseString));            
        }        
        
        return entities;
	}

	public int getLastModifiedVersion() {
		
		return restful.getLastModifiedVersion();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, List<String>> getDeletions(int sinceVersion) throws JsonParseException, JsonMappingException, IOException {
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

    public Uri getAttachmentUri(Item item) throws IOException
    {
        return restful.getAttachmentUri(item);
    }

    public UploadStatus uploadAttachment(File file, Item item)
    {
        return restful.uploadAttachment(file, item);
    }
}

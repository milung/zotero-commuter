package sk.mung.sentience.zoteroapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import sk.mung.sentience.zoteroapi.ZoteroRestful.Response;

import junit.framework.TestCase;

public class ZoteroTest extends TestCase 
{
	private String deletionsData;
	private String emptyDeletionsData;
	
	
	public void setUp() throws IOException
	{
		deletionsData = getContent("Deletions.json");
		emptyDeletionsData = getContent("EmptyDeletions.json");
	}
	
	private InputStream createInputStream(String resource)
    {
        return getClass().getResourceAsStream(resource);
    }
	 
    private String getContent(String resourceName) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(createInputStream(resourceName));
        char[] tmp = new char[4096];
        StringBuilder b = new StringBuilder();
        try
        {
            while (true)
            {
                int len = reader.read(tmp);
                if (len < 0)
                {
                    break;
                }
                b.append(tmp, 0, len);
            }
            reader.close();
        }
        finally
        {
            reader.close();
        }
        return b.toString();
    }

	public void test_getCollectionVersions_emptyResponse_validMap() throws IOException
	{
		Zotero subject = new Zotero();
		ZoteroRestful restfulMock = mock(ZoteroRestful.class);
		Response response = restfulMock.new Response(HttpStatus.SC_OK, "{}");
		when(restfulMock.callCurrentUserApi(anyString(), any(String[][].class), anyInt()))
		.thenReturn( response );
		subject.setRestfull(restfulMock);
		
		Map<String, Integer> result = subject.getCollectionsVersions(0);
		assertEquals(0, result.size());
	}
	
	public void test_getCollectionVersions_emptyArray_validMap() throws IOException
	{
		Zotero subject = new Zotero();
		ZoteroRestful restfulMock = mock(ZoteroRestful.class);
		Response response = restfulMock.new Response(HttpStatus.SC_OK, "[]");
		when(restfulMock.callCurrentUserApi(anyString(), any(String[][].class), anyInt()))
		.thenReturn( response );
		subject.setRestfull(restfulMock);
		
		Map<String, Integer> result = subject.getCollectionsVersions(0);
		assertEquals(0, result.size());
	}
	
	public void test_getDeletions_collectionsAreFilled() throws IOException
	{
		Zotero subject = new Zotero();
		ZoteroRestful restfulMock = mock(ZoteroRestful.class);
		Response response = restfulMock.new Response(HttpStatus.SC_OK, deletionsData);
		when(restfulMock.callCurrentUserApi(anyString(), any(String[][].class), anyInt()))
		.thenReturn( response );
		subject.setRestfull(restfulMock);
		
		Map<String, List<String>> result = subject.getDeletions(0);
		assertEquals(3, result.get("collections").size());
	}
	
	public void test_getDeletions_emptyCollections() throws IOException
	{
		Zotero subject = new Zotero();
		ZoteroRestful restfulMock = mock(ZoteroRestful.class);
		Response response = restfulMock.new Response(HttpStatus.SC_OK, emptyDeletionsData);
		when(restfulMock.callCurrentUserApi(anyString(), any(String[][].class), anyInt()))
		.thenReturn( response );
		subject.setRestfull(restfulMock);
		
		Map<String, List<String>> result = subject.getDeletions(0);
		assertEquals(0, result.get("collections").size());
	}
}

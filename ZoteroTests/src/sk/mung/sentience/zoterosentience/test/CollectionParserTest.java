package sk.mung.sentience.zoterosentience.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

import sk.mung.sentience.zoteroapi.CollectionEntity;
import sk.mung.sentience.zoteroapi.parsers.CollectionParser;

import junit.framework.TestCase;

public class CollectionParserTest extends TestCase
{
	private String collectionData;
	public void setUp() throws IOException
	{
		collectionData = getContent("CollectionExampleData.xml");
	}
	
    public void test_parse_numberOfEntriesFits() throws IOException, XmlPullParserException
    {
        CollectionParser subject = new CollectionParser();
        List<CollectionEntity> collections = subject.parse(collectionData);
        assertEquals(6, collections.size());
    }
    
    public void test_parse_VersionFits() throws IOException, XmlPullParserException
    {
        
        CollectionParser subject = new CollectionParser();
        
        List<CollectionEntity> collections = subject.parse(collectionData);
        
        assertEquals(408, (int)collections.get(0).getVersion());
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
}

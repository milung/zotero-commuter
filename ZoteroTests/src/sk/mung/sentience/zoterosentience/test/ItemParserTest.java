package sk.mung.sentience.zoterosentience.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

import sk.mung.zoteroapi.entities.Creator;
import sk.mung.zoteroapi.entities.CreatorType;
import sk.mung.zoteroapi.entities.ItemEntity;
import sk.mung.zoteroapi.parsers.ItemParser;

import junit.framework.TestCase;

public class ItemParserTest extends TestCase
{
	private String data;
	public void setUp() throws IOException
	{
		data = getContent("ItemExampleData.xml");
	}
	
    public void test_parse_numberOfEntriesFits() throws IOException, XmlPullParserException
    {
        ItemParser subject = new ItemParser();
        List<ItemEntity> items = subject.parse(data);
        assertEquals(2, items.size());
    }
    
    public void test_parse_versionFits() throws IOException, XmlPullParserException
    {        
    	ItemParser subject = new ItemParser();        
        List<ItemEntity> items = subject.parse(data);
        assertEquals(459, items.get(0).getVersion());
    }
    
    public void test_parse_titleSet() throws IOException, XmlPullParserException
    {        
    	ItemParser subject = new ItemParser();        
        List<ItemEntity> items = subject.parse(data);
        assertEquals("Learning from the past", items.get(0).getTitle());
    }
    
    public void test_parse_fieldsLoaded() throws IOException, XmlPullParserException
    {        
    	ItemParser subject = new ItemParser();        
        List<ItemEntity> items = subject.parse(data);
        assertEquals(23, items.get(0).getFields().size());
    }

    public void test_parse_creatorsLoaded() throws IOException, XmlPullParserException
    {        
    	ItemParser subject = new ItemParser();        
        List<ItemEntity> items = subject.parse(data);
        Creator expected = new Creator();
        expected.setType(CreatorType.AUTHOR);
        expected.setFirstName("Nancy G.");
        expected.setLastName("Leveson");
        
        assertEquals(expected, items.get(0).getCreators().get(0));
    }
    
    public void test_parse_collectionsLoaded() throws IOException, XmlPullParserException
    {        
    	ItemParser subject = new ItemParser();        
        List<ItemEntity> items = subject.parse(data);
        //assertEquals(1, items.get(0).getCollectionKeys().size());
        //assertEquals("EFSI75K4", items.get(0).getCollectionKeys().get(0));
    }
    
    public void test_parse_tagsLoaded() throws IOException, XmlPullParserException
    {        
    	ItemParser subject = new ItemParser();        
        List<ItemEntity> items = subject.parse(data);
        assertEquals(2, items.get(0).getTags().size());
        assertEquals("read", items.get(0).getTags().get(0));
    }
    
    public void test_parse_parentSet() throws IOException, XmlPullParserException
    {        
    	ItemParser subject = new ItemParser();        
        List<ItemEntity> items = subject.parse(data);
        assertEquals("7VCIGNKJ", items.get(1).getParentKey());
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

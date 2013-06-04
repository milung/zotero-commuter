package sk.mung.sentience.zoteroapi.parsers;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractAtomParser<T>
{
    private static final String ns = null;

    protected abstract T processContent( Map<String,Object> content);

    private ObjectMapper mapper = new ObjectMapper();

    public List<T> parse(String atom) throws XmlPullParserException, IOException
    {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(atom));
        parser.nextTag();
        return readFeed(parser);        
    }

    private List<T> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        List<T> entries = new ArrayList<T>();
        parser.require(XmlPullParser.START_TAG, ns, "feed");
        
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }  
        return entries;
    }

    public AbstractAtomParser()
    {
        super();
    }

    @SuppressWarnings("rawtypes")
	private T readEntry(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        
        Map<String,Object> content = new HashMap<String, Object>();
        
        parser.require(XmlPullParser.START_TAG, ns, "entry");               
        while (parser.next() != XmlPullParser.END_TAG) 
        {
            if (parser.getEventType() != XmlPullParser.START_TAG) 
            {
                continue;
            }
            String name = parser.getName();
            if (name.equals("content")) 
            {
                for( Object entry : readContent(parser).entrySet())
                {
                	content.put(
                			(String)((Map.Entry)entry).getKey(), 
                			((Map.Entry)entry).getValue());
                }
            } 
            else if(name.startsWith("zapi:"))
            {
            	String key = name;
            	String value = readZapi(parser, name);
            	content.put(key, value);
            }
            else 
            {
                skip(parser);
            }
        }
        
        return processContent(content);
    }

    @SuppressWarnings("rawtypes")
    private Map readContent(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, "content");
        Map content = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "content");
        
        return content;
    }
    
    
    private String readZapi(XmlPullParser parser, String name) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) 
        {
            result = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, ns, name);
        
        return result;
    }

    @SuppressWarnings("rawtypes")
    private Map readText(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) 
        {
            result = parser.getText();
            parser.nextTag();
        }
        return mapper.readValue( result, Map.class);
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                depth--;
                break;
            case XmlPullParser.START_TAG:
                depth++;
                break;
            }
        }
     }

}
package sk.mung.sentience.zoteroapi;

import android.net.Uri;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import sk.mung.sentience.zoteroapi.entities.Item;

public class ZoteroRestful {

    public static final String ZOTERO_API_VERSION_HEADER = "Zotero-API-Version";
    public static final String ZOTERO_API_VERSION_VALUE = "2";
    public static final String IF_MODIFIED_SINCE_VERSION_HEADER = "If-Modified-Since-Version";

    public class Response
	{
		public final int StatusCode;
		public final String ResponseString;
		
		public Response(int statusCode, String response)
		{
			this.StatusCode = statusCode;
			this.ResponseString = response;
		}
	}

	private static final String API_BASE = "https://api.zotero.org";
	private String accessToken;
	private String userId;
	private int lastModifiedVersion;
	
	public int getLastModifiedVersion()
    {
        return lastModifiedVersion;
    }
	
	public ZoteroRestful( String userId, String accessToken)
	{
		this.userId = userId;
		this.accessToken = accessToken;
	}
	
	public Response callCurrentUserApi(String method, String[][] parameters, int ifModifiedSinceVersion) 
            throws IOException
    {        
        Uri.Builder builder =  
            Uri.parse(API_BASE + getCurrentUserUriPrefix() + "/" + method)
                .buildUpon()                
                .appendQueryParameter("key", accessToken);
        for( String[] param : parameters)
        {
            builder.appendQueryParameter(param[0], param[1]);
        }
        
        HttpGet request;
        request = new HttpGet(builder.build().toString());
        request.addHeader(ZOTERO_API_VERSION_HEADER, ZOTERO_API_VERSION_VALUE);
        request.addHeader(
                IF_MODIFIED_SINCE_VERSION_HEADER,
        		Integer.toString(ifModifiedSinceVersion));
        
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        StatusLine statusLine = response.getStatusLine();
        
        int status = statusLine.getStatusCode();
        if(status == HttpStatus.SC_NOT_MODIFIED)
        {
        	return new Response(status, null);
        }
        else if( status == HttpStatus.SC_OK){
            lastModifiedVersion = Integer.parseInt(response.getHeaders("Last-Modified-Version")[0].getValue());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            
            out.close();
            return  new Response(status, out.toString());
            
        } else{
            //Closes the connection.
            response.getEntity().getContent().close();
            lastModifiedVersion = -1;
            throw new IOException(statusLine.getReasonPhrase());
        }
    }
   
    private String getCurrentUserUriPrefix()
    {
        return "/users/" + userId;
    }

    public URL getAttachmentUrl(Item item) throws IOException
    {
        Uri.Builder builder =
                Uri.parse(API_BASE + getCurrentUserUriPrefix() + "/items/" + item.getKey() + "/file")
                        .buildUpon()
                        .appendQueryParameter("key", accessToken);
        return new URL(builder.build().toString());
    }
}

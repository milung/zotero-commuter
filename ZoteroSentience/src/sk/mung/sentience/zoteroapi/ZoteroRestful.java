package sk.mung.sentience.zoteroapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;

public class ZoteroRestful {
	
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
            throws IOException, ClientProtocolException
    {        
        Uri.Builder builder =  
            Uri.parse(API_BASE + getCurrentUserUriPrefix() + "/" + method)
                .buildUpon()                
                .appendQueryParameter("key", accessToken);
        for( String[] param : parameters)
        {
            builder.appendQueryParameter(param[0], param[1]);
        }
        
        HttpGet request = new HttpGet(builder.build().toString());        
        request.addHeader("Zotero-API-Version", "2");
        request.addHeader(
        		"If-Modified-Since-Version", 
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
    
}

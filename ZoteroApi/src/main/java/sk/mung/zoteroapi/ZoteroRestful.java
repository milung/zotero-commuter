package sk.mung.zoteroapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sk.mung.zoteroapi.entities.Field;
import sk.mung.zoteroapi.entities.Item;
import sk.mung.zoteroapi.entities.ItemField;

public class ZoteroRestful {

    public static final String ZOTERO_API_VERSION_HEADER = "Zotero-API-Version";
    public static final String ZOTERO_API_VERSION_VALUE = "2";
    public static final String IF_MODIFIED_SINCE_VERSION_HEADER = "If-Modified-Since-Version";
    private static final String IF_MATCH = "If-Match";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String MIME_APPLICATION_JSON = "application/json;charset=UTF-8";
    public static final String IF_UNMODIFIED_SINCE_VERSION = "If-Unmodified-Since-Version";


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
        HttpGet request;
        URIBuilder builder;
        try
        {
            builder = new URIBuilder(API_BASE + getCurrentUserUriPrefix() + "/" + method)
            .addParameter("key", accessToken);

            for( String[] param : parameters)
            {
                builder.addParameter(param[0], param[1]);
            }

            request = new HttpGet(builder.build().toString());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        request.addHeader(ZOTERO_API_VERSION_HEADER, ZOTERO_API_VERSION_VALUE);
        request.addHeader(
                IF_MODIFIED_SINCE_VERSION_HEADER,
        		Integer.toString(ifModifiedSinceVersion));
        
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        StatusLine statusLine = response.getStatusLine();
        
        int status = statusLine.getStatusCode();
        storeLastModifiedVersion(response, ifModifiedSinceVersion);

        if(status == HttpStatus.SC_NOT_MODIFIED)
        {
            return new Response(status, null);
        }
        else if( status == HttpStatus.SC_OK){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            
            out.close();
            return  new Response(status, out.toString());
            
        } else{
            //Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }
    }

    private void storeLastModifiedVersion(HttpResponse response, int defaultValue)
    {
        Header header[]  = response.getHeaders("Last-Modified-Version");
        if(header.length > 0)
        {
            lastModifiedVersion = Integer.parseInt(header[0].getValue());
        }
        else
        {
            lastModifiedVersion = defaultValue; // keep expected version
        }
    }

    private String getCurrentUserUriPrefix()
    {
        return "/users/" + userId;
    }

    public URI getAttachmentUri(Item item) {
        try
        {
            return new URIBuilder(API_BASE + getCurrentUserUriPrefix() + "/items/" + item.getKey() + "/file")
                    .addParameter("key", accessToken)
                    .build();
        } catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private class UploadParameters
    {
        String url = null;
        String uploadKey = null;
        boolean exists = false;
        Map<String, String> parameters = new HashMap<String, String>(10);
    }
    public UploadStatus uploadAttachment(File file, Item item)
    {
        try
        {
            Response response = getUploadAuthorization(file,item);
            switch (response.StatusCode)
            {
                case HttpStatus.SC_OK:
                {
                    UploadParameters parameters = decodeUploadParameters(response);

                    if(parameters.exists)
                    {
                        return UploadStatus.SUCCESS; // already uploaded;
                    }
                    else
                    {
                        assert parameters.url != null;
                        uploadFile(item, file, parameters.url, parameters.parameters);
                        if(HttpStatus.SC_NO_CONTENT == registerUpload( item, parameters.uploadKey))
                        {
                            return UploadStatus.SUCCESS;
                        }
                        else return UploadStatus.UPDATE_CONFLICTS;
                    }
                }
                case HttpStatus.SC_PRECONDITION_FAILED: return UploadStatus.UPDATE_CONFLICTS;
                case HttpStatus.SC_FORBIDDEN: return UploadStatus.NOT_AUTHORIZED;
                case HttpStatus.SC_REQUEST_TOO_LONG: return UploadStatus.STORAGE_EXCEEDED;
                default: return UploadStatus.NETWORK_ERROR;
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return UploadStatus.NETWORK_ERROR;
        }
    }

    private UploadParameters decodeUploadParameters(Response response) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        UploadParameters parameters = new UploadParameters();
        Iterator<Map.Entry<String,JsonNode>> fields
                = mapper.readTree(response.ResponseString).fields();

        while( fields.hasNext())
        {
            Map.Entry<String,JsonNode> node = fields.next();
            if("exists".equals(node.getKey()))
            {
                parameters.exists = true;
                break;
            }
            else if("url".equals(node.getKey()))
            {
                parameters.url = node.getValue().asText();
            }
            else if("uploadKey".equals(node.getKey()))
            {
                parameters.uploadKey = node.getValue().asText();
            }
            else if("params".equals(node.getKey()))
            {
                Iterator<Map.Entry<String,JsonNode>> paramFields
                        = node.getValue().fields();
                while(paramFields.hasNext())
                {
                    Map.Entry<String,JsonNode> fieldNode = paramFields.next();
                    parameters.parameters.put(fieldNode.getKey(), fieldNode.getValue().asText());
                }
            }

        }
        return parameters;
    }

    private int registerUpload(Item item, String uploadKey) throws IOException
    {
        String oldHash = item.getField(ItemField.MD5).getValue();
        URI uri;
        try
        {
            uri = new URIBuilder(API_BASE + getCurrentUserUriPrefix() + "/items/" + item.getKey() + "/file")
            .addParameter("key", accessToken)
            .build();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        HttpPost request = new HttpPost(uri);
        request.addHeader(ZOTERO_API_VERSION_HEADER, ZOTERO_API_VERSION_VALUE);
        request.addHeader( IF_MATCH, oldHash);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("upload", uploadKey));

        request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        return response.getStatusLine().getStatusCode();
    }

    private Response uploadFile(Item item, File file, String url, Map<String, String> params) throws IOException
    {

        MultipartEntity entity = new MultipartEntity();
        for( Map.Entry<String,String> param : params.entrySet())
        {
            entity.addPart(param.getKey(), new StringBody(param.getValue()));
        }
        FileBody fileBody = new FileBody(file, item.getField(ItemField.CONTENT_TYPE).getValue());
        entity.addPart("file", fileBody);

        HttpPost request = new HttpPost(url);
        request.setEntity(entity);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        return decodeResponse(response);
    }

    private Response getUploadAuthorization(File file, Item item) throws IOException
    {
        String md5 = calculateFileHash(file);
        Field oldHashField = item.getField(ItemField.DOWNLOAD_MD5);
        if(oldHashField == null)
        {
            oldHashField = item.getField(ItemField.MD5);
        }
        String oldHash;
        if(oldHashField != null)
        {
            oldHash = oldHashField.getValue();
        }
        else return new Response(HttpStatus.SC_PRECONDITION_FAILED,"");

        URI uri;
        try
        {
            uri = new URIBuilder(API_BASE + getCurrentUserUriPrefix() + "/items/" + item.getKey() + "/file")
            .addParameter("key", accessToken)
            .build();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        Field field = item.getField(ItemField.FILE_NAME);
        if(field == null) return new Response(HttpStatus.SC_PRECONDITION_FAILED,"");
        String filename = field.getValue();

        field = item.getField(ItemField.CONTENT_TYPE);
        if(field == null) return new Response(HttpStatus.SC_PRECONDITION_FAILED,"");
        String contentType = field.getValue();

        HttpPost request = new HttpPost(uri);
        request.addHeader(ZOTERO_API_VERSION_HEADER, ZOTERO_API_VERSION_VALUE);
        request.addHeader( IF_MATCH, oldHash);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("md5",md5));
        nameValuePairs.add(new BasicNameValuePair("filename",filename));
        nameValuePairs.add(new BasicNameValuePair("filesize",Long.toString(file.length())));
        nameValuePairs.add(new BasicNameValuePair("mtime",Long.toString(file.lastModified())));
        nameValuePairs.add(new BasicNameValuePair("contentType",contentType));
        nameValuePairs.add(new BasicNameValuePair("params","1"));

        request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        return decodeResponse(response);

    }

    private Response decodeResponse(HttpResponse response) throws IOException
    {
        response.getStatusLine();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpEntity entity = response.getEntity();
        if(entity != null)
        {
            response.getEntity().writeTo(out);
        }

        out.close();
        return  new Response(response.getStatusLine().getStatusCode(), out.toString());
    }

    public String calculateFileHash(File file) {

        try
        {
            MessageDigest  digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            is = new DigestInputStream(is, digest);
            byte[] buffer = new byte[8192];
            //noinspection StatementWithEmptyBody
            while (is.read(buffer) > 0) {}
            return new BigInteger(1, digest.digest()).toString(16);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Response uploadEntities(String json, String entity, int sinceVersion) throws IOException
    {
        URI uri;
        try
        {
            uri = new URIBuilder(API_BASE + getCurrentUserUriPrefix() + "/" + entity)
            .addParameter("key", accessToken)
            .build();
        } catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        HttpPost request = new HttpPost(uri);
            request.addHeader(ZOTERO_API_VERSION_HEADER, ZOTERO_API_VERSION_VALUE);
            request.addHeader(CONTENT_TYPE, MIME_APPLICATION_JSON);
            request.addHeader(IF_UNMODIFIED_SINCE_VERSION, Integer.toString(sinceVersion ));
            request.setEntity(new StringEntity(json));

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(request);
            storeLastModifiedVersion(response, sinceVersion);
            return decodeResponse(response);

    }
    public Response deleteEntities(String keys, String entity, String entityKey, int sinceVersion)
            throws IOException
    {
        URI uri;
        try
        {
            uri = new URIBuilder(API_BASE + getCurrentUserUriPrefix()
                    + "/" + entity)
                    .addParameter(entityKey,keys)
                    .addParameter("key", accessToken)
                    .build();
        } catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        HttpDelete request = new HttpDelete(uri);
        request.addHeader(ZOTERO_API_VERSION_HEADER, ZOTERO_API_VERSION_VALUE);
        request.addHeader(IF_UNMODIFIED_SINCE_VERSION, Integer.toString(sinceVersion ));



        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        storeLastModifiedVersion(response, sinceVersion);
        return decodeResponse(response);

    }


}

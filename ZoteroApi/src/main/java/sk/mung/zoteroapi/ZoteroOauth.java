package sk.mung.zoteroapi;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.scribe.builder.ServiceBuilder;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ZoteroOauth 
{
	private final String apikey;
    private final String apisecret;
    private final String callback;
    private OAuthService oauthService;
    
    private Token requestToken;
    private Token accessToken;
    private String userId;
    private String userName;
    
   
    
    public Token getAccessToken() { return accessToken; }
    public String getUserId() { return userId; }        
    public String getUserName() { return userName; }
    
    public ZoteroOauth( String apikey, String apisecret, String callbackUrl)
    {
    	this.apikey = apikey;
    	this.apisecret = apisecret;
    	this.callback = callbackUrl;
    }
	
    /** retrieves request tokens and provides correct authorization url - connects to networks 
     * 
     * @return URL to navigate user for authorization
     */
    public String getAuthorizationUrl()
    {        
        requestToken = getOAuthService().getRequestToken();
        String uri =  getOAuthService().getAuthorizationUrl(requestToken);

        HttpParams params = new BasicHttpParams();
        params.setParameter("library_access", "1");
        params.setParameter("notes_access", "1");
        params.setParameter("write_access", "1");

        HttpPost request = new HttpPost(uri);
        request.setParams(params);
        return request.getURI().toString();
    }
    
    /** Processes authorization callback URL which includes verifier parameters and retrieves access 
     *  token from Zotero API. Connects to the network. If succeeded, then access token, request token, userId, 
     *  and userName can be requested from this instance and stored for future usages.
     * 
     * @param callbackUrl a callback url to decode
     */
    public void processAuthorizationCallbackUrl(String callbackUrl)
    {
        HttpPost uri = new HttpPost(callbackUrl);

        String verifier = uri.getParams().getParameter("oauth_verifier").toString();
        Verifier v = new Verifier(verifier);

        //save this token for practical use.
        accessToken = getOAuthService().getAccessToken(requestToken, v);
        extractUser(accessToken);
    }
    
    /** builds or returns scribe oauth service
     *  
     * @return Zotero service for Oauth
     */
    private OAuthService getOAuthService()
    {
        if( oauthService == null)
        {
             oauthService = new ServiceBuilder()
                .provider(ZoteroOauthApi.class)
                .apiKey(apikey)
                .apiSecret(apisecret)
                .signatureType(SignatureType.QueryString)
                .callback(callback)
                .build();
        }
        return oauthService;
    }
    
    /** extracts user data from authorization response
     * 
     * @param accessToken accessToken containing relevant information
     */
    private void extractUser (Token accessToken)
    {
          Pattern p = Pattern.compile("userID=([^&]*)");
          Matcher matcher = p.matcher(accessToken.getRawResponse());
          if (matcher.find() && matcher.groupCount() >= 1)
          {
            userId = OAuthEncoder.decode(matcher.group(1));
          }
          else
          {
            throw new OAuthException("Response body is incorrect. Can't extract userId from this: '" + accessToken.getRawResponse() + "'", null);
          }
          p = Pattern.compile("username=([^&]*)");
          matcher = p.matcher(accessToken.getRawResponse());
          if (matcher.find() && matcher.groupCount() >= 1)
          {
            userName = OAuthEncoder.decode(matcher.group(1));
          }
          else
          {
            throw new OAuthException("Response body is incorrect. Can't extract username from this: '" + accessToken.getRawResponse() + "'", null);
          }
    }
    
    
}
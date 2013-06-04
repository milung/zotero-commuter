package sk.mung.sentience.zoteroapi;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public final class ZoteroOauthApi extends DefaultApi10a
{

    @Override
    public String getAccessTokenEndpoint()
    {
        return "https://www.zotero.org/oauth/access";
    }

    @Override
    public String getAuthorizationUrl(Token requestToken)
    {
        return String.format("https://www.zotero.org/oauth/authorize?oauth_token=%s", requestToken.getToken());        
    }

    @Override
    public String getRequestTokenEndpoint()
    {        
        return "https://www.zotero.org/oauth/request";
    }

}

package sk.mung.sentience.zoterosentience;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import sk.mung.sentience.zoteroapi.Zotero;
import sk.mung.sentience.zoteroapi.ZoteroOauth;
import sk.mung.sentience.zoteroapi.ZoteroRestful;
import sk.mung.sentience.zoterosentience.storage.QueryDictionary;
import sk.mung.sentience.zoterosentience.storage.ZoteroStorage;
import sk.mung.sentience.zoterosentience.storage.ZoteroSync;

final public class GlobalState extends Application
{
    private static final String USERNAME = "username";
    private static final String USER_ID = "userId";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String PREFERENCES_SPACE = "sk.mung.sentience.zoterosentience";
    
    private Zotero zotero;
    private ZoteroStorage storage;
    private ZoteroSync zoteroSync;
        
    @Override
    public void onCreate()
    {
        super.onCreate();                
        restoreZoteroState();
    }    
    
    synchronized Zotero getZotero() 
    { 
        if( zotero == null)
        {
            restoreZoteroState();
        }
        return zotero; 
    } 
    
    synchronized ZoteroStorage getStorage()
    {
        if( storage == null)
        {
            storage = new ZoteroStorage(getApplicationContext(), new QueryDictionary(getApplicationContext()));
        }
        return storage;
    }
    
    synchronized ZoteroSync getZoteroSync()
    {
        if(zoteroSync == null)
        {
            zoteroSync = new ZoteroSync(getStorage(), getZotero());
        }
        return zoteroSync;
    }
    
    synchronized ZoteroOauth getZoteroOauth()
    {
    	return new ZoteroOauth(
    		getResources().getString(R.string.zotero_api_key) ,
    		getResources().getString(R.string.zotero_api_secret),
    		getResources().getString(R.string.callback));
    }
    
    void saveZoteroState( ZoteroOauth oauth)
    {
        SharedPreferences settings = getSharedPreferences(PREFERENCES_SPACE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(ACCESS_TOKEN, oauth.getAccessToken().getToken());
        editor.putString(USER_ID, oauth.getUserId());
        editor.putString(USERNAME, oauth.getUserName());

        editor.commit();
    }
    
    void restoreZoteroState()
    {
        SharedPreferences settings = getSharedPreferences(PREFERENCES_SPACE, Context.MODE_PRIVATE);
        String accessToken = null;
        String userId = null;
        if( settings.contains(ACCESS_TOKEN))
        {
        	accessToken = settings.getString(ACCESS_TOKEN, null);
        }
        
        if( settings.contains(USER_ID))
        {
        	userId = settings.getString(USER_ID, null);
        }
        
    	ZoteroRestful restful = new ZoteroRestful(userId, accessToken);
    	
        zotero = new Zotero();
        zotero.setRestfull(restful);
    }
    
    
    
}

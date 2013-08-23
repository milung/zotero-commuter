package sk.mung.sentience.zoterosentience;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;

import sk.mung.sentience.zoterosentience.storage.ZoteroStorageImpl;
import sk.mung.zoteroapi.Zotero;
import sk.mung.zoteroapi.ZoteroOauth;
import sk.mung.zoteroapi.ZoteroRestful;
import sk.mung.sentience.zoterosentience.storage.QueryDictionary;
import sk.mung.zoteroapi.ZoteroSync;

final public class GlobalState extends Application
{
    private static final String USERNAME = "username";
    private static final String USER_ID = "userId";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String PREFERENCES_SPACE = "sk.mung.sentience.zoterosentience";
    
    private Zotero zotero;
    private ZoteroStorageImpl storage;
    private ZoteroSync zoteroSync;
    private File downloadDir;

    private boolean isUserLogged = false;
    @Override
    public void onCreate()
    {
        super.onCreate();                
        restoreZoteroState();
    }    
    
    public synchronized Zotero getZotero()
    { 
        if( zotero == null)
        {
            restoreZoteroState();
        }
        return zotero; 
    } 
    
    public synchronized ZoteroStorageImpl getStorage()
    {
        if( storage == null)
        {
            storage = new ZoteroStorageImpl(getApplicationContext(), new QueryDictionary(getApplicationContext()));
        }
        return storage;
    }
    
    public synchronized ZoteroSync getZoteroSync()
    {
        if(zoteroSync == null)
        {

            zoteroSync = new ZoteroSync(getStorage(), getZotero(), getDownloadDirectory());
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

    public SharedPreferences getPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    void saveZoteroState( ZoteroOauth oauth)
    {
        SharedPreferences settings = getPreferences();
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(ACCESS_TOKEN, oauth.getAccessToken().getToken());
        editor.putString(USER_ID, oauth.getUserId());
        editor.putString(USERNAME, oauth.getUserName());

        editor.commit();

        if(zotero != null)
        {
            ZoteroRestful restful = new ZoteroRestful(oauth.getUserId(), oauth.getAccessToken().getToken());
            zotero.setRestfull(restful);
        }

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
        isUserLogged = accessToken != null;

    	ZoteroRestful restful = new ZoteroRestful(userId, accessToken);

        zotero = new Zotero();
        zotero.setRestfull(restful);
    }


    public File getDownloadDirectory()
    {
        if(downloadDir == null)
        {
            downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        }
        return downloadDir;
    }

    public boolean isUserLogged()
    {
        return isUserLogged;
    }
}

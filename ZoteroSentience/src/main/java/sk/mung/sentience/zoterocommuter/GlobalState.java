package sk.mung.sentience.zoterocommuter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.mung.sentience.zoterocommuter.storage.QueryDictionary;
import sk.mung.sentience.zoterocommuter.storage.ZoteroStorageImpl;
import sk.mung.zoteroapi.Zotero;
import sk.mung.zoteroapi.ZoteroOauth;
import sk.mung.zoteroapi.ZoteroRestful;
import sk.mung.zoteroapi.ZoteroSync;

final public class GlobalState extends Application
{
    private static final String USERNAME = "username";
    private static final String USER_ID = "userId";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String LOCAL_ITEM_KEY_COUNTER = "localKeyCounter";
    public static final int PROCESS_TEXT_EXTRACTION = 1;

    private Zotero zotero;
    private ZoteroStorageImpl storage;
    private ZoteroSync zoteroSync;
    private File downloadDir;
    private String userName;

    private class ProcessingItemPair
    {
        public final String key;
        public final int processCode;

        public ProcessingItemPair(String key, int code)
        {
            this.key = key;
            this.processCode = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProcessingItemPair that = (ProcessingItemPair) o;

            if (processCode != that.processCode) return false;
            if (key != null ? !key.equals(that.key) : that.key != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + processCode;
            return result;
        }
    }
    private final List<ProcessingItemPair> processingItems = new ArrayList<ProcessingItemPair>();

    public static GlobalState getInstance(Context context)
    {
        return (GlobalState)context.getApplicationContext();
    }
    public boolean isSyncRunning() {
        return isSyncRunning;
    }

    public void setSyncRunning(boolean syncRunning) {
        isSyncRunning = syncRunning;
    }

    private boolean isSyncRunning = false;

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
    		getResources().getString(R.string.callback),
            null);
    }

    public SharedPreferences getPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public long getKeyCounter()
    {
        SharedPreferences settings = getPreferences();
        long count = settings.getLong(LOCAL_ITEM_KEY_COUNTER,1000);

        SharedPreferences.Editor editor = settings.edit();

        editor.putLong(LOCAL_ITEM_KEY_COUNTER, count + 1);
        editor.commit();
        return count;
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
        isUserLogged = true;
        userName = oauth.getUserName();
    }
    
    void restoreZoteroState()
    {
        SharedPreferences settings = getPreferences();
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

        if( settings.contains(USERNAME))
        {
            userName = settings.getString(USERNAME, null);
        }

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

    public String getUserName() {
        return userName;
    }

    public void addProcessedItem(String key, int processCode)
    {
        processingItems.remove(new ProcessingItemPair(key,processCode));
        processingItems.add(new ProcessingItemPair(key,processCode));
    }

    public void removeProcessedItem(String key, int processCode)
    {
        processingItems.remove(new ProcessingItemPair(key,processCode));
    }

    public boolean isItemProcessed(String key, int processCode)
    {
        return processingItems.contains(new ProcessingItemPair(key,processCode));
    }
}

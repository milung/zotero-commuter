package sk.mung.sentience.zoterosentience;

import sk.mung.sentience.zoteroapi.ZoteroOauth;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v4.app.NavUtils;

public class LoginActivity extends Activity
{       
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Show the Up button in the action bar.
        setupActionBar();
        
        final ZoteroOauth zotero = ((GlobalState) getApplication()).getZoteroOauth();
        
        final WebView webview = (WebView) findViewById(R.id.webView);

        //attach WebViewClient to intercept the callback url
        webview.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {

                //check for our custom callback protocol
                //otherwise use default behavior
                if(url.startsWith( getString(R.string.callback) ))
                {
                    //authorization complete hide webview for now.
                    webview.setVisibility(View.GONE);                                        
                    new ProcessCallbackTask(url).execute(zotero);                   
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
       });

       //send user to authorization page
       new AuthorizationUrlTask(webview).execute(zotero); 
    }
    
    private class AuthorizationUrlTask extends AsyncTask<ZoteroOauth,Void, String > 
    {
        private WebView webview;
        
        public AuthorizationUrlTask( WebView webview)
        {
            this.webview = webview;
        }
        @Override
        protected String doInBackground(ZoteroOauth... zotero)
        {
            // TODO Auto-generated method stub
            return zotero[0].getAuthorizationUrl();
        }
        
        protected void onPostExecute(String authorizationUrl) {
            webview.loadUrl(authorizationUrl);
        }   
    }
    private class ProcessCallbackTask extends AsyncTask<ZoteroOauth,Void,ZoteroOauth>
    {
        private final String callbackUrl;
        ProcessCallbackTask(String callbackUrl)
        {
            this.callbackUrl = callbackUrl;
        }
        @Override
        protected ZoteroOauth doInBackground(ZoteroOauth... zotero)
        {
            zotero[0].processAuthorizationCallbackUrl(callbackUrl); 
            return zotero[0];
        }
        
        protected void onPostExecute(ZoteroOauth oauth) {
            GlobalState state = (GlobalState)getApplication();
            state.saveZoteroState(oauth);            
        } 
        
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar()
    {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

package sk.mung.sentience.zoterosentience;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import sk.mung.sentience.zoterosentience.navigation.DrawerFragment;
import sk.mung.zoteroapi.ZoteroOauth;

public class LoginFragment extends Fragment
{
    private DrawerFragment.Callbacks callback = DrawerFragment.DummyCallbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return  inflater.inflate(R.layout.activity_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final WebView webview = (WebView) getView().findViewById(R.id.webView);
        final ZoteroOauth zotero = GlobalState.getInstance(getActivity()).getZoteroOauth();

        getActivity().getActionBar().setTitle(getString(R.string.title_activity_login));
        getActivity().getActionBar().setSubtitle(null);

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

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof DrawerFragment.Callbacks)) { throw new IllegalStateException(
                "Activity must implement fragment's callbacks."); }

        callback = (DrawerFragment.Callbacks) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        callback = DrawerFragment.DummyCallbacks;
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
        
        protected void onPostExecute(ZoteroOauth oauth)
        {
            GlobalState.getInstance(getActivity()).saveZoteroState(oauth);
            callback.onAllItemsSelected();
        }
    }

}

package sk.mung.sentience.zoterosentience.navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;

import sk.mung.sentience.zoterosentience.BootSchedulerReceiver;
import sk.mung.sentience.zoterosentience.GlobalState;
import sk.mung.sentience.zoterosentience.R;
import sk.mung.zoteroapi.ZoteroSync;

/**
 * Activity with build in NavigationDrawer; navigation drawer is identified by id R.id.drawer_layout;
 * typically this is a drawer fragment.
 */
public abstract class ActivityWithDrawer extends FragmentActivity
{
    private static final String FIRST_START_KEY = "FIRST_START";
    private static final String FRAGMENT_STATE = "fragment_state";
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Fragment currentFragment;
    private boolean isFirstStart = false;
    private boolean currentAddToBackStack = false;

    abstract protected int getContentLayoutId();
    abstract protected Fragment createInitialFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayoutId());

        long id = 0L;
        if(savedInstanceState != null)
        {
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_STATE);
        }
        else
        {
            currentFragment = createInitialFragment();
        }
        drawerLayout =
                (DrawerLayout)findViewById(R.id.drawer_layout);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer,
                R.string.open, R.string.close)
        {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
                updateContent();
                invalidateOptionsMenu();
                if(isFirstStart)
                {
                    isFirstStart = false;

                    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                    if(prefs != null)
                    {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(FIRST_START_KEY, false);
                        editor.apply();
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(getResources().getString(R.string.app_name));
                getActionBar().setSubtitle(null);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        currentFragment = getSupportFragmentManager().findFragmentById(R.id.main);
                    }
                });

        updateContent();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                isFirstStart = prefs.getBoolean(FIRST_START_KEY, true);
                if(isFirstStart)
                {
                    drawerLayout.openDrawer(findViewById(R.id.drawer));
                }
            }
        }).start();
    }

    private void updateContent()
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if(currentAddToBackStack)
        {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.main, currentFragment)
        .commit();
        currentAddToBackStack = false;
    }

    public void navigateTo(Fragment fragment, boolean addToBackStack)
    {
        currentFragment = fragment;
        currentAddToBackStack = addToBackStack;

        View drawer = findViewById(R.id.drawer);
        if(drawerLayout.isDrawerOpen(drawer))
        {
            drawerLayout.closeDrawer(drawer);
        }
        else updateContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRefreshOptionSelected(MenuItem menuItem)
    {
        final Context context = this;
        new AsyncTask<Void, Void, Integer>(){

            @Override
            protected Integer doInBackground(Void... arg0)
            {
                try
                {
                    ZoteroSync zoteroSync = ((GlobalState) getApplication()).getZoteroSync();
                    zoteroSync.fullSync();
                    return 0;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return 1;
                }
                catch (XmlPullParserException e)
                {
                    e.printStackTrace();
                    return 2;
                } catch (URISyntaxException e)
                {
                    e.printStackTrace();
                    return 3;
                }
            }

            @Override
            protected void onPostExecute(Integer result)
            {
                if( !result.equals( 0))
                {
                    Toast.makeText(
                            context,
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                }
            }


        }.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_STATE, currentFragment);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
}

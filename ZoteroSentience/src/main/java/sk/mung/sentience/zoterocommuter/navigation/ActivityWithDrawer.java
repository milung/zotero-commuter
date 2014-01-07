package sk.mung.sentience.zoterocommuter.navigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import sk.mung.sentience.zoterocommuter.R;

/**
 * Activity with build in NavigationDrawer; navigation drawer is identified by id R.id.drawer_layout;
 * typically this is a drawer fragment.
 */
public abstract class ActivityWithDrawer extends ActionBarActivity
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
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);

        final ActionBarActivity thisActivity = this;
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout,
                R.drawable.ic_drawer,
                R.string.open, R.string.close)
        {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
                updateContent();
                ActivityCompat.invalidateOptionsMenu(thisActivity);
                if(isFirstStart)
                {
                    isFirstStart = false;

                    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                    if(prefs != null)
                    {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(FIRST_START_KEY, false);
                        editor.commit();
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle(getResources().getString(R.string.app_name));
                actionBar.setSubtitle(null);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

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
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
       currentFragment.onActivityResult(requestCode,resultCode,data);
    }
}

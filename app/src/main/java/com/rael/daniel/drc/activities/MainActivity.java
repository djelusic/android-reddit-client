package com.rael.daniel.drc.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.dialogs.ReplyDialog;
import com.rael.daniel.drc.dialogs.SubredditSearchDialog;
import com.rael.daniel.drc.fragments.IFragmentCallback;
import com.rael.daniel.drc.fragments.ListFragment;
import com.rael.daniel.drc.fragments.PostsFragment;
import com.rael.daniel.drc.fragments.PostsRecyclerFragment;
import com.rael.daniel.drc.fragments.RecyclerFragment;
import com.rael.daniel.drc.fragments.SubredditsFragment;
import com.rael.daniel.drc.fragments.SubredditsRecyclerFragment;
import com.rael.daniel.drc.reddit_login.RedditLogin;

/**
 * Main activity, mostly acts as a container for fragments and
 * global UI elements.
 */
public class MainActivity extends AppCompatActivity implements IFragmentCallback {
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navDrawer;
    private DrawerLayout drawerLayout;
    private boolean stateChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup toolbar and navigation drawer
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout)
                findViewById(R.id.drawer_layout);
        navDrawer = (NavigationView) findViewById(R.id.navigation);
        setupDrawerContent(navDrawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        addFragment(savedInstanceState);

        //Set a backstack listener to correctly change fragment names
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        int backStackCount = getSupportFragmentManager()
                                .getBackStackEntryCount();
                        if (backStackCount == 0) return;
                        FragmentManager.BackStackEntry backEntry =
                                getSupportFragmentManager()
                                        .getBackStackEntryAt(backStackCount - 1);
                        setTitle(backEntry.getName());
                    }
                });
    }


    //set onclick listener for nav drawer
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    //attempts to refresh top fragment on backstack
    public void refreshTopFragment() {
        FragmentManager.BackStackEntry backEntry =
                getSupportFragmentManager().getBackStackEntryAt(
                        getSupportFragmentManager().getBackStackEntryCount() - 1);
        String str = backEntry.getName();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(str);
        if (fragment instanceof RecyclerFragment) {
            ((RecyclerFragment) fragment).myRefresh();
        }
    }

    public FloatingActionButton getFAB() {
        return (FloatingActionButton) findViewById(R.id.main_fab);
    }

    //Handles navigation drawer item selection
    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        String name = null;

        switch (menuItem.getItemId()) {
            case R.id.drawer_frontpage:
                fragment = PostsRecyclerFragment.newInstance(this, null, null, null, true);
                name = "Front";
                break;
            case R.id.drawer_subreddits:
                fragment = SubredditsRecyclerFragment.newInstance(this);
                name = "Subreddits";
                break;
            case R.id.drawer_all:
                fragment = PostsRecyclerFragment.newInstance(this, "all", null, null, true);
                name = "all";
                break;
            case R.id.drawer_subreddit:
                SubredditSearchDialog sd = new SubredditSearchDialog(this);
                sd.show();
                break;
            case R.id.drawer_login:
                Intent i = new Intent(this, LoginActivity.class);
                startActivityForResult(i, 1);
                break;
            case R.id.drawer_logout:
                new RedditLogin(getApplicationContext()).logout();
                refreshTopFragment();
                invalidateOptionsMenu();
                break;
            default:
                break;
        }

        // Replace current fragment
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragments_container, fragment, name)
                    .addToBackStack(name).commit();
        }

        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Toggle visibility of login/logout buttons in nav drawer
        if (new RedditLogin(getApplicationContext()).isLoggedIn()) {
            navDrawer.getMenu().findItem(R.id.drawer_login).setVisible(false);
            navDrawer.getMenu().findItem(R.id.drawer_logout).setVisible(true);
        } else {
            navDrawer.getMenu().findItem(R.id.drawer_login).setVisible(true);
            navDrawer.getMenu().findItem(R.id.drawer_logout).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && new RedditLogin(getApplicationContext()).isLoggedIn()) {
            navDrawer.getMenu().findItem(R.id.drawer_login).setVisible(false);
            navDrawer.getMenu().findItem(R.id.drawer_logout).setVisible(true);
            refreshTopFragment();
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Used to add initial fragment when app is launched
    void addFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragments_container
                            , SubredditsRecyclerFragment.newInstance(getApplicationContext()), "Subreddits")
                    .addToBackStack("Subreddits") //The default fragment
                    .commit();
        }
    }

    public boolean isStateChanged() {
        return stateChanged;
    }

    //Set to true when fragment on backstack needs to reload
    public void setStateChanged(boolean stateChangedFlag) {
        this.stateChanged = stateChangedFlag;
    }

    @Override
    public FloatingActionButton getSubFAB(int position) {
        if (position == 0) {
            return (FloatingActionButton) findViewById(R.id.sub_fab_first);
        }
        if (position == 1) {
            return (FloatingActionButton) findViewById(R.id.sub_fab_second);
        }
        if (position == 2) {
            return (FloatingActionButton) findViewById(R.id.sub_fab_third);
        }
        return null;
    }
}

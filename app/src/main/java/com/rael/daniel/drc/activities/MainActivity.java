package com.rael.daniel.drc.activities;

import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.fragments.ListFragment;
import com.rael.daniel.drc.fragments.PostsFragment;
import com.rael.daniel.drc.fragments.SubredditsFragment;
import com.rael.daniel.drc.fragments.SubredditsRecyclerFragment;

/**
* Main activity, mostly acts as a container for fragments and
* the navigation drawer.
* */
public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navDrawer;
    private DrawerLayout drawerLayout;
    private boolean stateChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout)
                findViewById(R.id.drawer_layout);
        navDrawer =  (NavigationView) findViewById(R.id.navigation);
        setupDrawerContent(navDrawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.drawer_open,  R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        addFragment(savedInstanceState);

        //Set a backstack listener to correctly change fragment names
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        int backStackCount =  getSupportFragmentManager()
                                .getBackStackEntryCount();
                        if(backStackCount == 0) return;
                        FragmentManager.BackStackEntry backEntry =
                                getSupportFragmentManager()
                                        .getBackStackEntryAt(backStackCount-1);
                        setTitle(backEntry.getName());
                    }
                });
    }

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

    //Handles navigation drawer item selection
    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        String name = null;

        switch(menuItem.getItemId()) {
            case R.id.drawer_frontpage:
                fragment = PostsFragment.newInstance(this, null, null, null, true);
                name = "Front";
                break;
            case R.id.drawer_subreddits:
                fragment = SubredditsFragment.newInstance(this);
                name = "Subreddits";
                break;
            case R.id.drawer_all:
                fragment = PostsFragment.newInstance(this, "all", null, null, true);
                name = "all";
                break;
            default:
                break;
        }

        // Replace current fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragments_container, fragment)
                .addToBackStack(name).commit();

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
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

    void addFragment(Bundle savedInstanceState){
        if(savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragments_container
                            , SubredditsRecyclerFragment.newInstance(getApplicationContext()))
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
}

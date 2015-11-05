package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.activities.SubmitActivity;
import com.rael.daniel.drc.adapters.PostsRecyclerAdapter;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;
import com.rael.daniel.drc.reddit_fetchers.PostFetcher;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_objects.RedditPost;
import com.rael.daniel.drc.util.Consts;

/**
 * Created by Daniel on 05/11/2015.
 */
public class PostsRecyclerFragment extends  RecyclerFragment<RedditPost> {
    private String subreddit;
    private String sortingType;
    private String requestBody;
    private boolean showSubreddit;

    public PostsRecyclerFragment() {
        this.item_layout_id = R.layout.post_item_layout;
    }

    public String createUrl() {
        if(subreddit == null) { //Frontpage
            if(sortingType == null) return Consts.REDDIT_URL;
            return Consts.REDDIT_URL + "/" + sortingType;
        }
        if(requestBody == null) { //Regular subreddit post listing
            if(sortingType == null) return Consts.REDDIT_URL +
                    "/r/" + subreddit;
            return Consts.REDDIT_URL + "/r/" +
                    subreddit + "/" + sortingType;
        }
        //This is for search requests
        requestBody = requestBody.replaceAll("sort=.*&",
                "sort=" + sortingType + "&");
        return Consts.REDDIT_URL + "/r/" +
                subreddit + "/" + "search.json?" + requestBody;
    }

    @Override
    public void myRefresh() {
        initialized = false;
        getList().clear();
        lFetcher = new PostFetcher(getActivity()
                .getApplicationContext(), createUrl());
        initialize(false);
    }

    public static Fragment newInstance(Context applicationContext,
                                       String subreddit, String requestBody,
                                       String sortingType, boolean showSubreddit){
        PostsRecyclerFragment pf=new PostsRecyclerFragment();
        pf.subreddit =subreddit;
        pf.requestBody =requestBody;
        pf.sortingType =sortingType;
        pf.lFetcher =new PostFetcher(applicationContext, pf.createUrl());
        pf.showSubreddit = showSubreddit;
        return pf;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final SearchView sv = (SearchView)menu.findItem(R.id.action_search)
                .getActionView();
        final MenuItem sortSubmenuContainer = menu.findItem(R.id.sort_submenu);
        sortSubmenuContainer.setVisible(true);
        final MenuItem submit = menu.findItem(R.id.submit_menu_item);
        submit.setVisible(true);
        final Menu sortSubmenu = sortSubmenuContainer.getSubMenu();

        sv.setQueryHint("Search this subreddit");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.replace(" ", "+");
                Fragment sf = PostsFragment.newInstance(getActivity()
                                .getApplicationContext(),
                        subreddit, "q=" + query + "&restrict_sr=on&sort=relevance&t=all",
                        "relevance", false);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, sf)
                        .addToBackStack("Search")
                        .commit();
                sv.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    //Sets visibility of submit menu item
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(new RedditLogin(getActivity().getApplicationContext())
                .isLoggedIn()) {
            menu.findItem(R.id.submit_menu_item).setVisible(true);
        }
        else {
            menu.findItem(R.id.submit_menu_item).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(super.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.sort_hot:
                sortingType = "hot";
                myRefresh();
                return true;
            case R.id.sort_new:
                sortingType = "new";
                myRefresh();
                return true;
            case R.id.sort_top:
                sortingType = "top";
                myRefresh();
                return true;
            case R.id.submit_menu_item:
                Intent i = new Intent(getContext(), SubmitActivity.class);
                i.putExtra("subreddit", subreddit);
                startActivityForResult(i, 0);
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        myRefresh();
    }

    @Override
    protected void createAndBindAdapter() {
        adapter = new PostsRecyclerAdapter(getContext(),
                getList(), item_layout_id, rView, showSubreddit);
        rView.setAdapter(adapter);
    }

    @Override
    protected ListFetcher<RedditPost> getFetcher() {
        return new PostFetcher(getContext(), createUrl());
    }
}

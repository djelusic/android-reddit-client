package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.activities.SubmitActivity;
import com.rael.daniel.drc.adapters.PostsRecyclerAdapter;
import com.rael.daniel.drc.adapters.RedditRecyclerAdapter;
import com.rael.daniel.drc.reddit_api.RedditAPICommon;
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
                getList(), item_layout_id, rView, showSubreddit,
                new RedditRecyclerAdapter.ViewHolder.IViewHolderClick() {
            @Override
            public void onClick(View v, int position) {
                PostsRecyclerAdapter.PostViewHolder holder = (PostsRecyclerAdapter.PostViewHolder)v.getTag();
                switch(v.getId()) {
                    case R.id.upvote_arrow:
                        if(!new RedditLogin(getContext()).isLoggedIn()) {
                            Toast.makeText(getContext(), "Need to be logged in to vote.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        new RedditAPICommon(getContext())
                                .vote(getList().get(position).getName(), 1);
                        holder.upvoteArrow.setColorFilter(ContextCompat
                                .getColor(getContext(), R.color.upvoteOrange));
                        holder.downvoteArrow.setColorFilter(ContextCompat
                                .getColor(getContext(), android.R.color.darker_gray));
                        holder.postScore.setTextColor(ContextCompat
                                .getColor(getContext(), R.color.upvoteOrange));
                    case R.id.downvote_arrow:
                        if(!new RedditLogin(getContext()).isLoggedIn()) {
                            Toast.makeText(getContext(), "Need to be logged in to vote.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        new RedditAPICommon(getContext())
                                .vote(getList().get(position).getName(), -1);
                        holder.downvoteArrow.setColorFilter(ContextCompat
                                .getColor(getContext(), R.color.downvoteBlue));
                        holder.upvoteArrow.setColorFilter(ContextCompat
                                .getColor(getContext(), android.R.color.darker_gray));
                        holder.postScore.setTextColor(ContextCompat
                                .getColor(getContext(), R.color.downvoteBlue));
                    case R.id.link_thumbnail:
                        if(getList().get(position).getUrl().endsWith("jpg")) {
                            ImageFragment imf =
                                    (ImageFragment) ImageFragment.newInstance(
                                            getContext(), getList().get(position).getUrl());
                            ((AppCompatActivity) getContext()).getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragments_container, imf)
                                    .addToBackStack("Image")
                                    .commit();
                        }
                    case R.id.browser_image:
                        Fragment wvf =
                                ImprovedWebViewFragment.newInstance(
                                        getList().get(position).getUrl());
                        ((AppCompatActivity) getContext()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragments_container, wvf)
                                .addToBackStack("Web")
                                .commit();
                    default:
                        RedditLogin rl = new RedditLogin(getContext());
                        if(rl.isLoggedIn()) {
                            SharedPreferences.Editor edit = getContext()
                                    .getSharedPreferences(Consts.SPREFS_READ_POSTS + rl.getCurrentUser(),
                                            Context.MODE_PRIVATE).edit();
                            edit.putString(getList().get(position).getName(), "true").apply();
                        }
                        setSharedElementReturnTransition(TransitionInflater
                                .from(getActivity()).inflateTransition(R.transition.test_transition));
                        setExitTransition(TransitionInflater.from(getActivity())
                                .inflateTransition(android.R.transition.explode));
                        String postUrl = Consts.REDDIT_URL + getList().get(position).getPermalink() + ".json";
                        Fragment cf = CommentsRecyclerFragment.newInstance(getContext(),
                                postUrl, getList().get(position));
                        cf.setSharedElementEnterTransition(TransitionInflater
                                .from(getActivity()).inflateTransition(R.transition.test_transition));
                        cf.setEnterTransition(TransitionInflater.from(getActivity())
                                .inflateTransition(android.R.transition.explode));

                        v.findViewById(R.id.post_item_layout).setTransitionName("post_comment");
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragments_container, cf)
                                .addToBackStack("Comments")
                                .addSharedElement(v.findViewById(R.id.post_item_layout),
                                        "post_comment")
                                .commit();
                }
            }
        });
        rView.setAdapter(adapter);
    }

    @Override
    protected ListFetcher<RedditPost> getFetcher() {
        return new PostFetcher(getContext(), createUrl());
    }
}

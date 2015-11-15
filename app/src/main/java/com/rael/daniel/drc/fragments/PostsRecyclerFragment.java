package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
 * Fragment that displays fetched posts
 */
public class PostsRecyclerFragment extends RecyclerFragment<RedditPost> {
    private String subreddit;
    private String sortingType;
    private String requestBody;
    private boolean showSubreddit;

    public PostsRecyclerFragment() {
        this.layout_id = R.layout.post_rlist_layout;
        this.item_layout_id = R.layout.post_item_layout;
        fabVisibility = true;
    }

    public String createUrl() {
        if (subreddit == null) { //Frontpage
            if (sortingType == null) return Consts.REDDIT_URL;
            return Consts.REDDIT_URL + "/" + sortingType;
        }
        if (requestBody == null) { //Regular subreddit post listing
            if (sortingType == null) return Consts.REDDIT_URL +
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
                                       String sortingType, boolean showSubreddit) {
        PostsRecyclerFragment pf = new PostsRecyclerFragment();
        pf.subreddit = subreddit;
        pf.requestBody = requestBody;
        pf.sortingType = sortingType;
        pf.lFetcher = new PostFetcher(applicationContext, pf.createUrl());
        pf.showSubreddit = showSubreddit;
        return pf;
    }

    @Override
    protected void FABOnClick() {
        super.FABOnClick();
        if (!new RedditLogin(getContext()).isLoggedIn()) {
            Toast.makeText(getContext(), "Need to be logged in to submit.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(getContext(), SubmitActivity.class);
        i.putExtra("subreddit", subreddit);
        startActivityForResult(i, 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.setGroupVisible(R.id.sort_posts, true);
        menu.setGroupVisible(R.id.sort_comments, false);
        final SearchView sv = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        sv.setQueryHint("Search this subreddit");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.replace(" ", "+");
                Fragment sf = PostsRecyclerFragment.newInstance(getActivity()
                                .getApplicationContext(),
                        subreddit, "q=" + query + "&restrict_sr=on&sort=relevance&t=all",
                        "relevance", false);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, sf, "Search")
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

    private int getSortTypeId(String sortingType) {
        //default sorting type is hot
        if (sortingType == null) return 0;
        switch (sortingType) {
            case "hot":
                return 0;
            case "new":
                return 1;
            case "rising":
                return 2;
            case "controversial":
                return 3;
            case "top":
                return 4;
            default:
                return 0;
        }
    }

    //Sets visibility of submit menu item
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.getItem(getSortTypeId(sortingType) + 1);
        item.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.sort_posts_hot:
                sortingType = "hot";
                myRefresh();
                return true;
            case R.id.sort_posts_new:
                sortingType = "new";
                myRefresh();
                return true;
            case R.id.sort_posts_rising:
                sortingType = "rising";
                myRefresh();
                return true;
            case R.id.sort_posts_controversial:
                sortingType = "controversial";
                myRefresh();
                return true;
            case R.id.sort_posts_top:
                sortingType = "top";
                myRefresh();
                return true;
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
                        PostsRecyclerAdapter.PostViewHolder holder = (PostsRecyclerAdapter.PostViewHolder) v.getTag();
                        switch (v.getId()) {
                            case R.id.upvote_arrow:
                                if (!new RedditLogin(getContext()).isLoggedIn()) {
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
                                if (!new RedditLogin(getContext()).isLoggedIn()) {
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
                                if (getList().get(position).getUrl().endsWith("jpg")) {
                                    ImageFragment imf =
                                            (ImageFragment) ImageFragment.newInstance(
                                                    getContext(), getList().get(position).getUrl());
                                    ((AppCompatActivity) getContext()).getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragments_container, imf, "Image")
                                            .addToBackStack("Image")
                                            .commit();
                                } else {
                                    Fragment wvf =
                                            ImprovedWebViewFragment.newInstance(
                                                    getList().get(position).getUrl());
                                    ((AppCompatActivity) getContext()).getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragments_container, wvf, "Web")
                                            .addToBackStack("Web")
                                            .commit();
                                }
                                return;
                            case R.id.browser_image:
                                Fragment wvf =
                                        ImprovedWebViewFragment.newInstance(
                                                getList().get(position).getUrl());
                                ((AppCompatActivity) getContext()).getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragments_container, wvf, "Web")
                                        .addToBackStack("Web")
                                        .commit();
                                return;
                            default:
                                RedditLogin rl = new RedditLogin(getContext());
                                if (rl.isLoggedIn()) {
                                    SharedPreferences.Editor edit = getContext()
                                            .getSharedPreferences(Consts.SPREFS_READ_POSTS + rl.getCurrentUser(),
                                                    Context.MODE_PRIVATE).edit();
                                    edit.putString(getList().get(position).getName(), "true").apply();
                                }
                                setSharedElementReturnTransition(TransitionInflater
                                        .from(getActivity()).inflateTransition(R.transition.test_transition));
                        /*setExitTransition(TransitionInflater.from(getActivity())
                                .inflateTransition(android.R.transition.explode));*/
                                String postUrl = Consts.REDDIT_URL + getList().get(position).getPermalink() + ".json";
                                Fragment cf = CommentsRecyclerFragment.newInstance(getContext(),
                                        postUrl, getList().get(position));
                                cf.setSharedElementEnterTransition(TransitionInflater
                                        .from(getActivity()).inflateTransition(R.transition.test_transition));
                        /*cf.setEnterTransition(TransitionInflater.from(getActivity())
                                .inflateTransition(android.R.transition.explode));*/

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

package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.adapters.CommentsRecyclerAdapter;
import com.rael.daniel.drc.reddit_fetchers.CommentFetcher;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_objects.RedditComment;
import com.rael.daniel.drc.reddit_objects.RedditPost;

/**
 * Created by Daniel on 05/11/2015.
 */
public class CommentsRecyclerFragment extends RecyclerFragment<RedditComment> {
    String url;
    String sortingType = null;
    RedditPost link;

    public CommentsRecyclerFragment(){
        super();
        item_layout_id = R.layout.comment_item_layout;
        layout_id = R.layout.comments_rlist_layout;
        loadMoreOnScroll = false;
        this.fab_icon = R.drawable.ic_navigation_white_24dp;
        this.fabVisibility = true;
        this.numSubFabsVisible = 2;
    }

    @Override
    public void myRefresh() {
        initialized = false;
        getList().clear();
        lFetcher = new CommentFetcher(getContext(), url, 0);
        initialize(true);
    }

    public static Fragment newInstance(Context applicationContext, String url,
                                       RedditPost link){
        CommentsRecyclerFragment cf=new CommentsRecyclerFragment();
        cf.url=url;
        cf.lFetcher =new CommentFetcher(applicationContext, cf.url, 0);
        cf.link = link;
        return cf;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setFABIcon(fragmentCallback.getSubFAB(1),
                R.drawable.ic_keyboard_arrow_down_black_24dp);
        setSubFABOnClickListener(1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Find next root comment
                int firstVisible = ((LinearLayoutManager)rView.getLayoutManager())
                        .findFirstVisibleItemPosition();
                for (int i = firstVisible + 1; i < getList().size(); i++) {
                    if (getList().get(i - 1).getDepth() == 0) {
                        rView.smoothScrollToPosition(i);
                        break;
                    }
                }
            }
        });
        setFABIcon(fragmentCallback.getSubFAB(2),
                R.drawable.ic_keyboard_arrow_up_black_24dp);
        setSubFABOnClickListener(2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int firstVisible = ((LinearLayoutManager)rView.getLayoutManager())
                        .findFirstVisibleItemPosition();
                //Find previous root comment
                for (int i = firstVisible - 1; i > 0; i--) {
                    if (getList().get(i - 1).getDepth() == 0) {
                        rView.smoothScrollToPosition(i);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(new RedditLogin(getContext()).isLoggedIn()) {
            menu.add("Upvote");
            menu.add("Downvote");
            menu.add("Reply");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.setGroupVisible(R.id.sort_posts, false);
        menu.setGroupVisible(R.id.sort_comments, true);
    }

    private int getSortTypeId(String sortingType) {
        //default sorting type is best
        if(sortingType == null) return 0;
        switch(sortingType) {
            case "best" : return 0;
            case "top" : return 1;
            case "new" : return 2;
            case "controversial" : return 3;
            case "old" : return 4;
            default : return 0;
        }
    }

    //Sets visibility of submit menu item
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.getItem(getSortTypeId(sortingType) + 6);
        item.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(super.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.sort_comments_best:
                sortingType = "best";
                break;
            case R.id.sort_comments_top:
                sortingType = "top";
                break;
            case R.id.sort_comments_new:
                sortingType = "new";
                break;
            case R.id.sort_comments_controversial:
                sortingType = "controversial";
                break;
            case R.id.sort_comments_old:
                sortingType = "old";
                break;
            default:
                break;
        }
        if(sortingType == null) {
            return true;
        }
        else {
            url = url.replaceAll("json.*", "json?sort=" + sortingType);
            myRefresh();
            return true;
        }
    }

    //Upvote/downvote functionality
    /*@Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        RedditComment clickedComment;
        if(info.position == 0) {
            clickedComment = link;
            ((MainActivity)getActivity()).setStateChanged(true);
        }
        else clickedComment = getList().get(info.position);
        if(item.getTitle() == "Upvote") {
            new RedditAPICommon(getActivity().getApplicationContext())
                    .vote(clickedComment.getName(), 1);
            //Change color of score to orange after upvoting
            //TODO: integrate this into VoteTask
            View scoreView = rView.getChildAt(info.position -
                    rView.getFirstVisiblePosition())
                    .findViewById(R.id.comment_score);
            ((TextView)scoreView).setTextColor(ContextCompat
                    .getColor(getContext(), R.color.upvoteOrange));
            ((TextView)scoreView).setText(String.valueOf(Integer
                    .valueOf(clickedComment.getScore()) + 1));
        }
        else if(item.getTitle() == "Downvote") {
            new RedditAPICommon(getActivity().getApplicationContext())
                    .vote(clickedComment.getName(), -1);
            //Change color of score to blue after downvoting
            //TODO: integrate this into VoteTask
            View scoreView = rView.getChildAt(info.position -
                    rView.getFirstVisiblePosition())
                    .findViewById(R.id.comment_score);
            ((TextView)scoreView).setTextColor(ContextCompat
                    .getColor(getContext(), R.color.downvoteBlue));
            ((TextView)scoreView).setText(String.valueOf(Integer
                    .valueOf(clickedComment.getScore()) - 1));
        }
        else if(item.getTitle() == "Reply") {
            Bundle params = new Bundle();
            params.putString("parent_id", clickedComment.getName());
            ReplyDialog rd = new ReplyDialog(getActivity(), params);
            rd.show();
        }
        return true;
    }*/

    @Override
    protected void createAndBindAdapter() {
        adapter = new CommentsRecyclerAdapter(getContext(), getList(),
                item_layout_id, rView, link,this);
        rView.setAdapter(adapter);
    }

    @Override
    protected ListFetcher<RedditComment> getFetcher() {
        return new CommentFetcher(getContext(), url, 0);
    }
}

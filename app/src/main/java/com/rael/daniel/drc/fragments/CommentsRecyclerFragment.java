package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.activities.SubmitActivity;
import com.rael.daniel.drc.adapters.CommentsRecyclerAdapter;
import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.reddit_fetchers.CommentFetcher;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_objects.RedditComment;
import com.rael.daniel.drc.reddit_objects.RedditPost;
import com.rael.daniel.drc.util.TimeSpan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by Daniel on 05/11/2015.
 */
public class CommentsRecyclerFragment extends RecyclerFragment<RedditComment> {
    String url;
    String sortingType = null;
    RedditComment link;
    RedditPost newLink;
    View separatorView;
    boolean spinnerInit;

    public CommentsRecyclerFragment(){
        super();
        item_layout_id = R.layout.comment_item_layout;
        layout_id = R.layout.comments_rlist_layout;
        loadMoreOnScroll = false;
    }

    /*@Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = super.onCreateView(inflater,
                container, savedInstanceState);
        if(contentView != null) {
            ((TextView)contentView.findViewById(R.id.link_title))
                    .setText(newLink.getTitle());
            ((TextView)contentView.findViewById(R.id.link_additional))
                    .setText("by " + newLink.getAuthor() + ", " +
                            newLink.getDate());
            if(newLink.isSelfPost()) {
                Spanned spannedText = Html.fromHtml(newLink.getSelftext());
                if (spannedText.length() > 2 && !spannedText.toString().equals("null"))
                    ((TextView)contentView.findViewById(R.id.link_selftext))
                            .setText(spannedText.subSequence(0, spannedText.length() - 2));
                contentView.findViewById(R.id.browser_image).setVisibility(View.GONE);
            }
        }
        return contentView;
    }*/

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
        cf.newLink = link;
        return cf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    /*@Override
    protected void getAdditionalItems() {
        //Fill the first two main list elements so that the adapter knows
        //that there are supposed to be two headers
        //TODO: make it less hacky
        getList().add(0, new RedditComment());
        getList().add(1, new RedditComment());
        try {
            RedditConnectionManager conn =
                    new RedditConnectionManager(getContext());
            String rawData = conn.readContents(url.replaceAll("\\?ref.*", ".json"));
            JSONObject selfPostData = new JSONArray(rawData).getJSONObject(0)
                    .getJSONObject("data")
                    .getJSONArray("children")
                    .getJSONObject(0)
                    .getJSONObject("data");
            link = new RedditComment();
            link.setText(Html.fromHtml(selfPostData
                    .getString("selftext_html")).toString());
            link.setDomain(selfPostData.getString("domain"));
            link.setUser(selfPostData.getString("author"));
            link.setScore(selfPostData.getString("score"));
            link.setName(selfPostData.getString("name"));
            link.setLikes(selfPostData.getString("likes"));
            link.setNumComments(selfPostData.getString("num_comments"));
            link.setTitle(selfPostData.getString("title"));
            link.setUrl(selfPostData.getString("url"));
            link.setDate(TimeSpan
                    .calculateTimeSpan(new BigDecimal(selfPostData.getString("created_utc"))
                            .longValue(), System.currentTimeMillis() / 1000l));
        }
        catch(Exception e){ e.printStackTrace(); }
    }*/

    public View setupSpinner() {
        /*separatorView = LayoutInflater.from(getContext())
                .inflate(R.layout.comments_separator_layout, null);
        *//*if(link.isSelfPost())
            ((TextView)separatorView.findViewById(R.id.num_comments))
                    .setText(link.getNumComments() + " comments");*//*
        final Spinner spinner = (Spinner)separatorView.findViewById(R.id.sortSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerInit) {
                    String selected = spinner.getSelectedItem().toString();
                    if (selected.equals("contr.")) selected = "controversial";
                    url = url.replaceAll("json.*", "json?sort=" + selected);
                    myRefresh();
                } else spinnerInit = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/
        return new View(getContext());
    }

    @Override
    protected void createAndBindAdapter() {
        adapter = new CommentsRecyclerAdapter(getContext(), getList(),
                item_layout_id, rView, newLink,this);
        rView.setAdapter(adapter);
    }

    @Override
    protected ListFetcher<RedditComment> getFetcher() {
        return new CommentFetcher(getContext(), url, 0);
    }
}

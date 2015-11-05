package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.activities.MainActivity;
import com.rael.daniel.drc.adapters.CommentsRecyclerAdapter;
import com.rael.daniel.drc.adapters.SubredditsRecyclerAdapter;
import com.rael.daniel.drc.dialogs.ReplyDialog;
import com.rael.daniel.drc.reddit_api.RedditAPICommon;
import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.reddit_fetchers.CommentFetcher;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_objects.RedditComment;
import com.rael.daniel.drc.util.TimeSpan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by Daniel on 05/11/2015.
 */
public class CommentsRecyclerFragment extends RecyclerFragment<RedditComment> {
    String url;
    RedditComment link;
    View separatorView;
    boolean spinnerInit;

    public CommentsRecyclerFragment(){
        super();
        item_layout_id = R.layout.comment_item_layout;
        loadMoreOnScroll = false;
    }

    @Override
    public void myRefresh() {
        initialized = false;
        getList().clear();
        lFetcher = new CommentFetcher(getContext(), url, 0);
        initialize(true);
    }

    public static Fragment newInstance(Context applicationContext, String url){
        CommentsRecyclerFragment cf=new CommentsRecyclerFragment();
        cf.url=url;
        cf.lFetcher =new CommentFetcher(applicationContext, cf.url, 0);
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
    }

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
                item_layout_id, rView, link,this);
        rView.setAdapter(adapter);
    }

    @Override
    protected ListFetcher<RedditComment> getFetcher() {
        return new CommentFetcher(getContext(), url, 0);
    }
}

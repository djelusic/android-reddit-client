package com.rael.daniel.drc.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.reddit_api.GetMoreCommentsTask;
import com.rael.daniel.drc.reddit_api.RedditAPICommon;
import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.reddit_fetchers.CommentFetcher;
import com.rael.daniel.drc.reddit_objects.RedditComment;
import com.rael.daniel.drc.reddit_objects.RedditLink;
import com.rael.daniel.drc.util.TimeSpan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;

/**
* Fragment that displays fetched comments
* */
public class CommentsFragment extends ListFragment<RedditComment>{

    String url;
    RedditComment link;
    private boolean spinnerInit = false;
    View separatorView;

    private final int VIEW_TYPE_COUNT = 4;
    private final int VIEW_TYPE_OP_COMMENT = 0;
    private final int VIEW_TYPE_COMMENT_SEPARATOR = 1;
    private final int VIEW_TYPE_REGULAR_COMMENT = 2;
    private final int VIEW_TYPE_MORE_COMMENTS_STUB = 3;

    public CommentsFragment(){
        super();
        layout_id = R.layout.comment_list_layout;
        item_layout_id = R.layout.comment_item_layout;
        list_id = R.id.comment_list;
        loadMoreOnScroll = false;
    }

    @Override
    protected int mGetViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    protected int mGetItemViewType(int position) {
        if(position == 0)
            return VIEW_TYPE_OP_COMMENT;
        if(position == 1)
            return VIEW_TYPE_COMMENT_SEPARATOR;

        if(getList().get(position - 2).isMoreCommentsStub())
            return VIEW_TYPE_MORE_COMMENTS_STUB;
        return VIEW_TYPE_REGULAR_COMMENT;
    }

    @Override
    public void myRefresh() {
        getList().clear();
        lFetcher = new CommentFetcher(getActivity()
                .getApplicationContext(), url, 0);
        initialize(true);
    }

    public static Fragment newInstance(Context applicationContext, String url){
        CommentsFragment cf=new CommentsFragment();
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
        menu.add("Upvote");
        menu.add("Downvote");
    }

    //Upvote/downvote functionality
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(item.getTitle() == "Upvote") {
            String clickedCommentId = getList().get(info.position - 2).getName();
                new RedditAPICommon(getActivity().getApplicationContext())
                        .vote(clickedCommentId, 1);
            //Change color of score to orange after upvoting
            //TODO: integrate this into VoteTask
            View scoreView = lView.getChildAt(info.position -
                    lView.getFirstVisiblePosition())
                    .findViewById(R.id.comment_score);
            ((TextView)scoreView).setTextColor(ContextCompat
                    .getColor(getContext(), R.color.upvoteOrange));
            ((TextView)scoreView).setText(String.valueOf(Integer
                    .valueOf(getList().get(info.position - 2).getScore()) + 1));
        }
        else if(item.getTitle() == "Downvote") {
            String clickedCommentId = getList().get(info.position - 2).getName();
            new RedditAPICommon(getActivity().getApplicationContext())
                    .vote(clickedCommentId, -1);
            //Change color of score to blue after downvoting
            //TODO: integrate this into VoteTask
            View scoreView = lView.getChildAt(info.position -
                    lView.getFirstVisiblePosition())
                    .findViewById(R.id.comment_score);
            ((TextView)scoreView).setTextColor(ContextCompat
                    .getColor(getContext(), R.color.downvoteBlue));
            ((TextView)scoreView).setText(String.valueOf(Integer
                    .valueOf(getList().get(info.position - 2).getScore()) - 1));
        }
        return true;
    }

    //Get selfpost
    @Override
    void getAdditionalItems() {
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
            link.setDate(TimeSpan
                    .calculateTimeSpan(new BigDecimal(selfPostData.getString("created_utc"))
                            .longValue(), System.currentTimeMillis() / 1000l));
        }
        catch(Exception e){ e.printStackTrace(); }
    }

    private void setChildTextView(View parent, int childId, CharSequence text) {
        ((TextView)parent.findViewById(childId)).setText(text);
    }

    private void fillCommentView(View commentView, RedditComment comment) {
        setChildTextView(commentView, R.id.comment_user, comment.getUser());
        setChildTextView(commentView, R.id.comment_score, comment.getScore());
        Spanned spannedText = Html.fromHtml(comment.getText());
        if (spannedText.length() > 2)
            setChildTextView(commentView, R.id.comment_text,
                    spannedText.subSequence(0, spannedText.length() - 2));
        else setChildTextView(commentView, R.id.comment_text, "");
        setChildTextView(commentView, R.id.comment_time, " points, " + comment.getDate());

        TextView commentScore = (TextView)commentView.findViewById(R.id.comment_score);
        if(comment.isUpvoted())
            commentScore.setTextColor(ContextCompat.getColor(
                    getContext(), R.color.upvoteOrange));
        else if(comment.isDownvoted())
            commentScore.setTextColor(ContextCompat.getColor(
                    getContext(), R.color.downvoteBlue));
        ((TextView)commentView.findViewById(R.id.comment_text))
                .setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    void getAdditionalViews() {
    }

    private void addRedditCommentStyle(ViewGroup outerLayout, ViewGroup innerLayout, int position) {
        innerLayout.setBackgroundResource(getList().get(position - 2).getDepth() % 2 == 0 ?
                        R.drawable.borders_white : R.drawable.borders_grey);
        for (int i = 0; i < getList().get(position - 2).getDepth(); i++) {
            View v = new View(getContext());
            v.setBackgroundResource(i % 2 == 0 ?
                    R.drawable.borders_white : R.drawable.borders_grey);
            //v.setBackgroundColor(i%2 == 0 ? Color.WHITE : Color.parseColor("#F2F2F2"));
            v.setLayoutParams(new LinearLayout.LayoutParams(20, LinearLayout.
                    LayoutParams.MATCH_PARENT));
            outerLayout.addView(v, i);
        }
    }

    private void setupSpinner() {
        separatorView = getActivity()
                .getLayoutInflater()
                .inflate(R.layout.comments_separator_layout, lView, false);
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
        });
    }

    //Renders comments with alternating white/grey borders to improve visibility
    View fillItems(List<RedditComment> comments, View convertView, int position) {

        if(mGetItemViewType(position) == VIEW_TYPE_OP_COMMENT &&
                link != null && link.isSelfPost()) {
            if(convertView == null)
                convertView = getActivity()
                        .getLayoutInflater()
                        .inflate(R.layout.comment_item_layout, lView, false);
            fillCommentView(convertView, link);
            return convertView;
        }

        //Handle "more comments" stubs
        if(mGetItemViewType(position) == VIEW_TYPE_MORE_COMMENTS_STUB) {
            //Can't recycle view here because it will mess up the indentation
            //TODO: find a way to do it :)
            convertView = getActivity()
                    .getLayoutInflater()
                    .inflate(R.layout.more_comments_layout, lView, false);
            LinearLayout moreLayout = (LinearLayout) convertView.findViewById(
                    R.id.more_comments_layout);
            addRedditCommentStyle(moreLayout, moreLayout, position);
            return convertView;
        }

        if(mGetItemViewType(position) == VIEW_TYPE_COMMENT_SEPARATOR) {
            if(separatorView == null) setupSpinner();
            return separatorView;
        }

        //Regular comments
        if(position > 1) {
            //Can't recycle view here because it will mess up the indentation
            //TODO: find a way to do it :)
            convertView = getActivity()
                    .getLayoutInflater()
                    .inflate(item_layout_id, null);
            ViewGroup outerLayout = (ViewGroup)convertView.
                    findViewById(R.id.comment_outer_layout);
            ViewGroup innerLayout = (ViewGroup)convertView.
                    findViewById(R.id.comment_inner_layout);
            addRedditCommentStyle(outerLayout, innerLayout, position);
            fillCommentView(convertView, getList().get(position - 2));
            registerForContextMenu(lView);
            return convertView;
        }
        return convertView;
    }

    @Override
    void setOnClick(final List<RedditComment> comments, ListView lView, int position) {
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                //Load more comments if user touches a "more comments" stub
                if(comments.get(position - 2).getUser().equals("more")) {
                    GetMoreCommentsTask tsk = new GetMoreCommentsTask(CommentsFragment.this,
                            link.getName(), position - 2);
                    tsk.execute((Void)null);
                }
            }
        });
    }
}

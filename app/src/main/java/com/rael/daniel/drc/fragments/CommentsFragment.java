package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.reddit_api.GetMoreCommentsTask;
import com.rael.daniel.drc.reddit_api.RedditAPICommon;
import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.reddit_fetchers.CommentFetcher;
import com.rael.daniel.drc.reddit_objects.RedditComment;
import com.rael.daniel.drc.reddit_objects.RedditSelfPost;
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
    RedditSelfPost selfPost;
    private boolean spinnerInit = false;

    /*private final int VIEW_TYPE_COUNT = 4;
    private final int VIEW_TYPE_OP_COMMENT = 0;
    private final int VIEW_TYPE_COMMENT_SEPARATOR = 1;
    private final int VIEW_TYPE_REGULAR_COMMENT = 2;
    private final int VIEW_TYPE_MORE_COMMENTS_PLACEHOLDER = 3;*/

    public CommentsFragment(){
        super();
        layout_id = R.layout.comment_list_layout;
        item_layout_id = R.layout.comment_item_layout;
        list_id = R.id.comment_list;
    }

    @Override
    public void myRefresh() {
        getList().clear();
        lFetcher = new CommentFetcher(getActivity()
                .getApplicationContext(), url, 0);
        initialize();
    }

    public static Fragment newInstance(Context applicationContext, String url){
        CommentsFragment cf=new CommentsFragment();
        cf.url=url;
        cf.lFetcher =new CommentFetcher(applicationContext, cf.url, 0);
        return cf;
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
            selfPost = new RedditSelfPost();
            selfPost.setSelftext(Html.fromHtml(selfPostData
                    .getString("selftext_html")).toString());
            selfPost.setDomain(selfPostData.getString("domain"));
            selfPost.setAuthor(selfPostData.getString("author"));
            selfPost.setScore(selfPostData.getString("score"));
            selfPost.setName(selfPostData.getString("name"));
            selfPost.setLikes(selfPostData.getString("likes"));
            selfPost.setDate(TimeSpan
                    .calculateTimeSpan(new BigDecimal(selfPostData.getString("created_utc"))
                            .longValue(), System.currentTimeMillis() / 1000l));
        }
        catch(Exception e){ e.printStackTrace(); }
    }

    @Override
    void getAdditionalViews() {
        //Create view for selfpost
        if(selfPost != null && selfPost.getDomain().startsWith("self")) {
            View selfPostView = getActivity()
                    .getLayoutInflater()
                    .inflate(R.layout.comment_item_layout, lView, false);

            TextView commentText = (TextView)selfPostView
                    .findViewById(R.id.comment_text);
            TextView commentScore = (TextView)selfPostView
                    .findViewById(R.id.comment_score);

            ((TextView)selfPostView.findViewById(R.id.comment_user))
                    .setText(selfPost.getAuthor());
            commentScore.setText(selfPost.getScore());
            ((TextView)selfPostView.findViewById(R.id.comment_time))
                    .setText(" points, " + selfPost.getDate());

            if(selfPost.getLikes().equals("true")) {
                commentScore.setTextColor(ContextCompat.getColor(
                        getContext(), R.color.upvoteOrange));
            }
            else if(selfPost.getLikes().equals("false")) {
                commentScore.setTextColor(ContextCompat.getColor(
                        getContext(), R.color.downvoteBlue));
            }

            Spanned spannedText = Html.fromHtml(selfPost.getSelftext());
            if (spannedText.length() > 2)
                commentText.setText(spannedText.subSequence(0, spannedText.length() - 2));
            else commentText.setText("");
            //commentText.setText(Html.fromHtml(comments.get(position).text));
            commentText.setMovementMethod(LinkMovementMethod.getInstance());
            lView.addHeaderView(selfPostView);
        }

        //Create view for separator between selfpost and comments
        View separatorView=getActivity()
                .getLayoutInflater()
                .inflate(R.layout.comments_separator_layout, lView, false);
        //Configure spinner for sorting options
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
        lView.addHeaderView(separatorView);
    }

    //Renders comments with alternating white/grey borders to improve visibility
    View fillItems(List<RedditComment> comments, View convertView, int position) {

        //Handle "more comments" stubs
        if(comments.get(position).getUser().equals("more")) {
            convertView = getActivity()
                    .getLayoutInflater()
                    .inflate(R.layout.more_comments_layout, null);
            LinearLayout moreLayout = (LinearLayout) convertView.findViewById(
                    R.id.more_comments_layout);
            moreLayout
                    .setBackgroundResource(comments.get(position).getDepth() % 2 == 0 ?
                            R.drawable.borders_white : R.drawable.borders_grey);
            for (int i = 0; i < comments.get(position).getDepth(); i++) {
                View v = new View(getContext());
                v.setBackgroundResource(i % 2 == 0 ?
                        R.drawable.borders_white : R.drawable.borders_grey);
                //v.setBackgroundColor(i%2 == 0 ? Color.WHITE : Color.parseColor("#F2F2F2"));
                v.setLayoutParams(new LinearLayout.LayoutParams(20, LinearLayout.
                        LayoutParams.MATCH_PARENT));
                moreLayout.addView(v, i);
            }
            return convertView;
        }

        LinearLayout outerLayout = (LinearLayout) convertView.findViewById(
                R.id.comment_outer_layout);
        convertView.findViewById(R.id.comment_inner_layout)
                .setBackgroundResource(comments.get(position).getDepth() % 2 == 0 ?
                        R.drawable.borders_white : R.drawable.borders_grey);
        for (int i = 0; i < comments.get(position).getDepth(); i++) {
            View v = new View(getContext());
            v.setBackgroundResource(i % 2 == 0 ?
                    R.drawable.borders_white : R.drawable.borders_grey);
            //v.setBackgroundColor(i%2 == 0 ? Color.WHITE : Color.parseColor("#F2F2F2"));
            v.setLayoutParams(new LinearLayout.LayoutParams(20, LinearLayout.
                    LayoutParams.MATCH_PARENT));
            outerLayout.addView(v, i);
        }

        TextView commentScore = (TextView) convertView
                .findViewById(R.id.comment_score);
        TextView commentText = (TextView) convertView
                .findViewById(R.id.comment_text);

        ((TextView) convertView.findViewById(R.id.comment_user))
                .setText(comments.get(position).getUser());
        commentScore.setText(comments.get(position).getScore());
        ((TextView) convertView.findViewById(R.id.comment_time))
                .setText(" points, " + comments.get(position).getDate());

        if(comments.get(position).getLikes().equals("true")) {
            commentScore.setTextColor(ContextCompat
                    .getColor(getContext(), R.color.upvoteOrange));
        }
        else if(comments.get(position).getLikes().equals("false")) {
            commentScore.setTextColor(ContextCompat
                    .getColor(getContext(), R.color.downvoteBlue));
        }

        Spanned spannedText = Html.fromHtml(comments.get(position).getText());
        if (spannedText.length() > 2)
            commentText.setText(spannedText.subSequence(0, spannedText.length() - 2));
        else commentText.setText("");
        commentText.setMovementMethod(LinkMovementMethod.getInstance());
        registerForContextMenu(lView);
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
                            selfPost.getName(), position - 2);
                    tsk.execute((Void)null);
                }
            }
        });
    }
}

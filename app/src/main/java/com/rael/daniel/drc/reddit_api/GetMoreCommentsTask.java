package com.rael.daniel.drc.reddit_api;

import android.content.Context;
import android.os.AsyncTask;

import com.rael.daniel.drc.fragments.CommentsFragment;
import com.rael.daniel.drc.reddit_fetchers.CommentFetcher;
import com.rael.daniel.drc.reddit_objects.RedditComment;
import com.rael.daniel.drc.util.Consts;

import java.util.List;

/**
 * Fetches more comments in background and adds them to the main list
 */
public class GetMoreCommentsTask extends AsyncTask<Void, Void, List<RedditComment> > {

    private final String API_URL =
            Consts.REDDIT_URL + "/api/morechildren.json";

    private Context applicationContext;
    private List<RedditComment> comments;
    private CommentsFragment commentsFragment;
    private String link_id;
    private String[] children;
    private int startingDepth;
    private int position;

    public GetMoreCommentsTask(CommentsFragment commentsFragment,
                        String link_id, int position) {
        this.applicationContext = commentsFragment.getContext();
        this.commentsFragment = commentsFragment;
        this.comments = commentsFragment.getList();
        this.link_id = link_id;
        this.children = comments.get(position).getMoreChildren();
        this.startingDepth = comments.get(position).getDepth();
        this.position = position;
    }

    @Override
    protected List<RedditComment> doInBackground(Void... params) {
        RedditConnectionManager rcm = new RedditConnectionManager(applicationContext);
        StringBuilder url = new StringBuilder(API_URL);
        url.append("?api_type=json&link_id=");
        url.append(link_id);
        url.append("&children=");
        for (int i = 0; i < children.length; i++) {
            url.append(children[i]);
            if(i < children.length - 1)
                url.append(",");
        }
        String rawResponse = rcm.readContents(url.toString());
        List<RedditComment> moreComments = new CommentFetcher(applicationContext, startingDepth)
                .getMoreCommentsFromString(rawResponse);
        return moreComments;
    }

    @Override
    protected void onPostExecute(List<RedditComment> moreComments) {
        super.onPostExecute(moreComments);
        //Remove the "more comments" stub
        comments.remove(position);
        //Add fetches comments to comments list
        comments.addAll(position, moreComments);
        commentsFragment.notifyChange();
    }
}

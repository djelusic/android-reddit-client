package com.rael.daniel.drc.reddit_fetchers;

import android.content.Context;
import android.text.Html;

import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.util.JSONArrayConverter;
import com.rael.daniel.drc.util.TimeSpan;
import com.rael.daniel.drc.reddit_objects.RedditComment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CommentFetcher extends ListFetcher<RedditComment> {
    private String url;
    private int startingDepth;

    public CommentFetcher(Context applicationContext, int startingDepth) {
        super(applicationContext);
        this.startingDepth = startingDepth;
    }

    public CommentFetcher(Context applicationContext, String url, int startingDepth) {
        super(applicationContext);
        this.url = url.replaceAll("\\?ref.*", ".json");
        this.startingDepth = startingDepth;
    }

    public List<RedditComment> getItems() {
        List<RedditComment> comments = new ArrayList<RedditComment>();

        RedditConnectionManager conn =
                new RedditConnectionManager(applicationContext);
        String rawData = conn.readContents(url);
        return getItemsFromString(rawData);
    }

    public List<RedditComment> getItemsFromString(String rawData) {
        List<RedditComment> comments = new ArrayList<RedditComment>();

        try {
            JSONArray children = new JSONArray(rawData).getJSONObject(1)
                    .getJSONObject("data")
                    .getJSONArray("children");
            getCommentsRecursive(comments, children, startingDepth);
        }
        catch(Exception e){ e.printStackTrace(); }
        return comments;
    }

    public List<RedditComment> getMoreCommentsFromString(String rawData) {
        List<RedditComment> moreComments = new ArrayList<RedditComment>();

        try {
            JSONArray things = new JSONObject(rawData).getJSONObject("json")
                    .getJSONObject("data").getJSONArray("things");
            //getCommentsRecursive(moreComments, things, startingDepth);
            for(int i = 0; i < things.length(); i++) {
                JSONObject commentData = things.getJSONObject(i)
                        .getJSONObject("data");
                RedditComment currentComment = new RedditComment();

                currentComment.setParentId(commentData.getString("parent_id"));
                //calculate depth
                currentComment.setDepth(startingDepth);
                for(RedditComment rc : moreComments) {
                    if(rc.getName().equals(currentComment.getParentId())){
                        currentComment.setDepth(rc.getDepth() + 1);
                    }
                }

                if(things.getJSONObject(i).optString("kind").equals("more")) {
                    currentComment.setUser("more");
                    currentComment.setMoreChildren(JSONArrayConverter
                            .convert(commentData.getJSONArray("children")));
                    moreComments.add(currentComment);
                    continue;
                }
                currentComment.setText(Html.fromHtml(commentData
                        .getString("body_html")).toString());
                currentComment.setUser(commentData.getString("author"));
                currentComment.setScore(commentData.getString("score"));
                currentComment.setDate(TimeSpan
                        .calculateTimeSpan(new BigDecimal(commentData.getString("created_utc"))
                                .longValue(), System.currentTimeMillis() / 1000l));
                currentComment.setId(commentData.getString("id"));
                currentComment.setName(commentData.getString("name"));
                currentComment.setLikes(commentData.getString("likes"));

                moreComments.add(currentComment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moreComments;
    }

    private void getCommentsRecursive(List<RedditComment> comments, JSONArray children, int depth) {
        try {
            for(int i = 0; i < children.length(); i++) {
                JSONObject commentData = children.getJSONObject(i)
                        .getJSONObject("data");
                RedditComment currentComment = new RedditComment();
                if(children.getJSONObject(i).optString("kind").equals("more")) {
                    currentComment.setUser("more");
                    currentComment.setMoreChildren(JSONArrayConverter
                            .convert(commentData.getJSONArray("children")));
                    currentComment.setDepth(startingDepth + depth);
                    comments.add(currentComment);
                    continue;
                }
                currentComment.setText(Html.fromHtml(commentData
                        .getString("body_html")).toString());
                currentComment.setUser(commentData.getString("author"));
                currentComment.setScore(commentData.getString("score"));
                currentComment.setDate(TimeSpan
                        .calculateTimeSpan(new BigDecimal(commentData.getString("created_utc"))
                                .longValue(), System.currentTimeMillis() / 1000l));
                currentComment.setId(commentData.getString("id"));
                currentComment.setName(commentData.getString("name"));
                currentComment.setLikes(commentData.getString("likes"));

                currentComment.setDepth(startingDepth + depth);

                if(currentComment.getUser() == null) continue;
                comments.add(currentComment);

                if(commentData.get("replies").equals("")) continue;
                JSONArray replies = commentData.getJSONObject("replies")
                        .getJSONObject("data").getJSONArray("children");
                getCommentsRecursive(comments, replies, startingDepth + depth + 1);
            }
        }
        catch(Exception e) {}
    }
}

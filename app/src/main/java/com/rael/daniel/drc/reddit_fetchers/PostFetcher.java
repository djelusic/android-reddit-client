package com.rael.daniel.drc.reddit_fetchers;

import android.content.Context;
import android.util.Log;

import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.reddit_objects.RedditPost;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Gets all posts in a given subreddit
 */
public class PostFetcher extends ListFetcher<RedditPost> {

    String url;

    public PostFetcher(Context applicationContext, String url) {
        super(applicationContext);
        this.url = url + "/.json";
        after = "";
    }

    public List<RedditPost> getItems() {
        RedditConnectionManager conn =
                new RedditConnectionManager(applicationContext);
        String rawData = conn.readContents(url);
        List<RedditPost> postList = new ArrayList<>();

        try {
            JSONObject data=new JSONObject(rawData)
                    .getJSONObject("data");
            JSONArray children=data.getJSONArray("children");
            after=data.getString("after");

            for(int i=0;i<children.length();i++){
                JSONObject cur=children.getJSONObject(i)
                        .getJSONObject("data");
                RedditPost post=new RedditPost();
                post.setTitle(cur.optString("title"));
                post.setUrl(cur.optString("url"));
                post.setNumComments(cur.optInt("num_comments"));
                post.setPoints(cur.optInt("score"));
                post.setAuthor(cur.optString("author"));
                post.setSubreddit(cur.optString("subreddit"));
                post.setPermalink(cur.optString("permalink"));
                post.setDomain(cur.optString("domain"));
                post.setId(cur.optString("id"));
                if(post.getTitle() !=null)
                    postList.add(post);
            }
        }
        catch(Exception e){
        Log.e("fetchPosts()", e.toString());
        }
        return postList;
    }

    @Override
    public List<RedditPost> getMoreItems() {
        url = url.replace(".json", ".json?after=" + after);
        return getItems();
    }
}

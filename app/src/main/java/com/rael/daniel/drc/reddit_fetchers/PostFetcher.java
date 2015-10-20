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
 * Fetches data from reddit API and turns it into a list of RedditPost objects.
 */
public class PostFetcher extends ListFetcher<RedditPost> {

    String url;
    String after;

    public PostFetcher(Context applicationContext, String url) {
        super(applicationContext);
        this.url = url + "/.json";
        after = "";
    }

    public List<RedditPost> getItems() {
        RedditConnectionManager conn =
                new RedditConnectionManager(applicationContext);
        String rawData = conn.readContents(url);
        List<RedditPost> postList = new ArrayList<RedditPost>();

        try {
            JSONObject data=new JSONObject(rawData)
                    .getJSONObject("data");
            JSONArray children=data.getJSONArray("children");
            after=data.getString("after");

            for(int i=0;i<children.length();i++){
                JSONObject cur=children.getJSONObject(i)
                        .getJSONObject("data");
                RedditPost th=new RedditPost();
                th.setTitle(cur.optString("title"));
                th.setUrl(cur.optString("url"));
                th.setNumComments(cur.optInt("num_comments"));
                th.setPoints(cur.optInt("score"));
                th.setAuthor(cur.optString("author"));
                th.setSubreddit(cur.optString("subreddit"));
                th.setPermalink(cur.optString("permalink"));
                th.setDomain(cur.optString("domain"));
                th.setId(cur.optString("id"));
                if(th.getTitle() !=null)
                    postList.add(th);
            }
        }
        catch(Exception e){
        Log.e("fetchPosts()", e.toString());
        }
        return postList;
    }

    List<RedditPost> getMorePosts() {
        url +=  "/"
                +".json"
                +"?after=" + after;

        return getItems();
    }
}

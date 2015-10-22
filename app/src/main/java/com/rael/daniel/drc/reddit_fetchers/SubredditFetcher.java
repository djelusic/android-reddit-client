package com.rael.daniel.drc.reddit_fetchers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_objects.RedditSubreddit;
import com.rael.daniel.drc.util.Consts;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Fetches a list of subreddits (subscribed subreddits if the user
 * is logged in, defaults subs otherwise).
 */
public class SubredditFetcher extends ListFetcher<RedditSubreddit> {

    String url;
    String after;

    public SubredditFetcher(Context applicationContext) {
        super(applicationContext);
        if(new RedditLogin(applicationContext).isLoggedIn())
            url = Consts.REDDIT_URL + "/subreddits/mine/.json";
        else url = Consts.REDDIT_URL + "/subreddits/default/.json";
        after = "";
    }

    public List<RedditSubreddit> getItems() {
        RedditConnectionManager conn =
                new RedditConnectionManager(applicationContext);
        String rawData = conn.readContents(url);
        List<RedditSubreddit> subredditList = new ArrayList<>();

        try {
            JSONObject data=new JSONObject(rawData)
                    .getJSONObject("data");
            JSONArray children=data.getJSONArray("children");
            after=data.getString("after");

            for(int i=0;i<children.length();i++){
                JSONObject cur=children.getJSONObject(i)
                        .getJSONObject("data");
                RedditSubreddit th=new RedditSubreddit();
                th.setUrl(cur.optString("url"));
                StringTokenizer st = new StringTokenizer(th.getUrl(), "/");
                st.nextToken();
                th.setUrl(st.nextToken());
                th.setTitle(cur.optString("title"));
                if(th.getTitle() !=null)
                    subredditList.add(th);
            }
        }
        catch(Exception e){
            Log.e("fetchSubreddits()", e.toString());
        }
        return subredditList;
    }

    List<RedditSubreddit> getMoreSubreddits() {
        url = Consts.REDDIT_URL + "/subreddits/default/.json"
                +"?after=" + after;

        return getItems();
    }
}

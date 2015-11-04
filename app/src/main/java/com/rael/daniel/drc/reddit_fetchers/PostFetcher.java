package com.rael.daniel.drc.reddit_fetchers;

import android.content.Context;
import android.util.Log;

import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.reddit_objects.RedditPost;
import com.rael.daniel.drc.util.TimeSpan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import imgur.ImgurGalleryFetcher;
import imgur.ImgurImage;

/**
 * Gets all posts in a given subreddit
 */
public class PostFetcher extends ListFetcher<RedditPost> {

    public PostFetcher(Context applicationContext, String url) {
        super(applicationContext);
        this.url = url + "/.json";
        after = "";
    }

    public List<RedditPost> getItems() {
        List<RedditPost> postList = new ArrayList<>();
        RedditConnectionManager conn =
                new RedditConnectionManager(applicationContext);
        rawData = conn.readContents(url);
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
                post.setName(cur.optString("name"));
                post.setLikes(cur.optString("likes"));
                post.setClicked(cur.optString("clicked"));
                post.setDate(TimeSpan
                        .calculateTimeSpan(new BigDecimal(cur.getString("created_utc"))
                                .longValue(), System.currentTimeMillis() / 1000l));
                /*if(post.getDomain().contains("imgur")) {
                    if (!post.getUrl().contains("/gallery/")
                            && !post.getUrl().contains("/a/")) {
                        post.setPicassoUrl("https://i.imgur.com/" +
                                ImgurGalleryFetcher.getGalleryId(post
                                        .getUrl()) + "s.jpg");
                    } else {
                        List<ImgurImage> imageList = null;
                        if(post.getUrl().contains("/gallery/")) {
                            imageList = ImgurGalleryFetcher
                                    .getImagesFromGallery(ImgurGalleryFetcher
                                            .getGalleryId(post.getUrl()), true, false);
                        }
                        if(post.getUrl().contains("/a/")) {
                            imageList = ImgurGalleryFetcher
                                    .getImagesFromGallery(ImgurGalleryFetcher
                                            .getGalleryId(post.getUrl()), true, true);
                        }
                        if(imageList != null) {
                            post.setPicassoUrl(imageList.get(0).getLink());
                        }
                    }
                }
                else post.setPicassoUrl(null);*/
                post.setThumbnailUrl(cur.getString("thumbnail"));
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

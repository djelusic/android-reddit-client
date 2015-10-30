package com.rael.daniel.drc.reddit_objects;

/**
 * Represents a post on Reddit.
 */
public class RedditPost {
    private String subreddit;
    private String title;
    private String author;
    private int points;
    private int numComments;
    private String permalink;
    private String url;
    private String domain;
    private String id;
    private String name;
    private String likes;
    private String date;
    private String clicked;

    public boolean isUpvoted() {
        return likes.equals("true");
    }

    public boolean isDownvoted() {
        return likes.equals("false");
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPoints() {
        return points;
    }

    public int getNumComments() {
        return numComments;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain() {
        return domain;
    }

    public String getId() {
        return id;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClicked() {
        return clicked;
    }

    public void setClicked(String clicked) {
        this.clicked = clicked;
    }
}

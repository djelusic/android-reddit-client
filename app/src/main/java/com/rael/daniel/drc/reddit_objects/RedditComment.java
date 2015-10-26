package com.rael.daniel.drc.reddit_objects;

/**
 * Represents a comment on Reddit.
 */
public class RedditComment {
    private String user;
    private String text;
    private String score;
    private String date;
    private String id;
    private String name;
    private String likes;
    private String parentId;
    private String domain;
    private String url;
    private String numComments;
    private String title;
    private String[] moreChildren;
    private int depth;

    public boolean isMoreCommentsStub() {
        return user.equals("more");
    }

    public boolean isSelfPost() {
        return getDomain().startsWith("self");
    }

    public boolean isUpvoted() {
        return likes.equals("true");
    }

    public boolean isDownvoted() {
        return likes.equals("false");
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
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

    public String[] getMoreChildren() {
        return moreChildren;
    }

    public void setMoreChildren(String[] moreChildren) {
        this.moreChildren = moreChildren;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNumComments() {
        return numComments;
    }

    public void setNumComments(String numComments) {
        this.numComments = numComments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

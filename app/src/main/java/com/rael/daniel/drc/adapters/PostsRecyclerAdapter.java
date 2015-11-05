package com.rael.daniel.drc.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.fragments.CommentsFragment;
import com.rael.daniel.drc.fragments.CommentsRecyclerFragment;
import com.rael.daniel.drc.fragments.ImageFragment;
import com.rael.daniel.drc.fragments.ImprovedWebViewFragment;
import com.rael.daniel.drc.reddit_api.RedditAPICommon;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_objects.RedditPost;
import com.rael.daniel.drc.util.Consts;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Daniel on 05/11/2015.
 */
public class PostsRecyclerAdapter extends RedditRecyclerAdapter<RedditPost> {
    private boolean showSubreddit;

    public static class PostViewHolder extends RedditRecyclerAdapter.ViewHolder {
        ImageView upvoteArrow;
        TextView postScore;
        ImageView downvoteArrow;
        TextView postTitle;
        TextView postDetails;
        TextView postComments;
        ImageView browserImage;
        ImageView linkThumbnail;
        TextView postSubreddit;

        public PostViewHolder( View itemView, IViewHolderClick listener) {
            super (itemView, listener);
            upvoteArrow = (ImageView)itemView.findViewById(
                    R.id.upvote_arrow);
            postScore = (TextView)itemView.findViewById(
                    R.id.post_score);
            downvoteArrow = (ImageView)itemView.findViewById(
                    R.id.downvote_arrow);
            postTitle = (TextView)itemView.findViewById(
                    R.id.post_title);
            postDetails = (TextView)itemView.findViewById(
                    R.id.post_details);
            postComments = (TextView)itemView.findViewById(
                    R.id.post_comments);
            browserImage = (ImageView)itemView.findViewById(
                    R.id.browser_image);
            linkThumbnail = (ImageView)itemView.findViewById(
                    R.id.link_thumbnail);
            postSubreddit = (TextView)itemView.findViewById(
                    R.id.post_subreddit);

            upvoteArrow.setTag(this);
            downvoteArrow.setTag(this);

            upvoteArrow.setOnClickListener(this);
            downvoteArrow.setOnClickListener(this);
            browserImage.setOnClickListener(this);
            linkThumbnail.setOnClickListener(this);
        }
    }

    public PostsRecyclerAdapter(Context applicationContext, List<RedditPost> list,
                                int item_layout_id, RecyclerView recyclerView,
                                boolean showSubreddit) {
        super(applicationContext, list, item_layout_id, recyclerView);
        this.showSubreddit = showSubreddit;
    }

    @Override
    protected void bindItem(RecyclerView.ViewHolder holder, int position) {
        PostViewHolder postViewHolder = (PostViewHolder)holder;
        postViewHolder.postTitle.setText(getList().get(position).getTitle());
        postViewHolder.postDetails.setText("submitted " +
                getList().get(position).getDate() + " ago by "
                + getList().get(position).getAuthor() + " (" +
                getList().get(position).getDomain() + ")");
        postViewHolder.postComments.setText(getList().get(position)
                .getNumComments() + " comments");
        postViewHolder.postScore.setText(String.valueOf(getList()
                .get(position).getPoints()));

        //Change color if the post was previously visited.
        //Note that reddit doesn't actually save this information so there is no way to sync this
        //across devices/installs.
        RedditLogin rl = new RedditLogin(applicationContext);
        if(rl.isLoggedIn()) {
            SharedPreferences sprefs = applicationContext
                    .getSharedPreferences(Consts.SPREFS_READ_POSTS + rl.getCurrentUser(),
                            Context.MODE_PRIVATE);
            if(sprefs.getString(getList().get(position).getName(), "false").equals("true"))
                postViewHolder.postTitle.setTextColor(ContextCompat
                        .getColor(applicationContext, R.color.visited));
            else //Get default color in case the view is recycled, don't really know a better way to do this
                postViewHolder.postTitle.setTextColor(new TextView(applicationContext)
                        .getTextColors().getDefaultColor());
        }

        //Make sure to reset colors in case view is recycled
        postViewHolder.upvoteArrow.setColorFilter(ContextCompat
                .getColor(applicationContext, android.R.color.darker_gray));
        postViewHolder.downvoteArrow.setColorFilter(ContextCompat
                .getColor(applicationContext, android.R.color.darker_gray));
        postViewHolder.postScore.setTextColor(ContextCompat
                .getColor(applicationContext, android.R.color.darker_gray));

        if(getList().get(position).isUpvoted()) {
            postViewHolder.upvoteArrow.setColorFilter(ContextCompat
                    .getColor(applicationContext, R.color.upvoteOrange));
            postViewHolder.postScore.setTextColor(ContextCompat
                    .getColor(applicationContext, R.color.upvoteOrange));
        }
        else if(getList().get(position).isDownvoted()) {
            postViewHolder.downvoteArrow.setColorFilter(ContextCompat
                    .getColor(applicationContext, R.color.downvoteBlue));
            postViewHolder.postScore.setTextColor(ContextCompat
                    .getColor(applicationContext, R.color.downvoteBlue));
        }
        //Adds subreddit name to the view (eg when displaying posts from /r/all)
        if(showSubreddit) {
            postViewHolder.postSubreddit.setText(getList().get(position).getSubreddit());
        }

        //Selfposts don't contain external links
        if(getList().get(position).getDomain().startsWith("self")) {
            postViewHolder.browserImage.setVisibility(View.GONE);
            postViewHolder.linkThumbnail.setVisibility(View.GONE);
        }

        else if(!getList().get(position).getThumbnailUrl().equals("default")
                && !getList().get(position).getThumbnailUrl().equals("")) {
            //image link, load thumbnail
            postViewHolder.browserImage.setVisibility(View.GONE);
            postViewHolder.linkThumbnail
                    .setVisibility(View.VISIBLE);
            Picasso.with(applicationContext).load(getList().get(position)
                    .getThumbnailUrl()).fit().into(postViewHolder.linkThumbnail);
        }
        else {
            postViewHolder.browserImage.setVisibility(View.VISIBLE);
            postViewHolder.linkThumbnail.setVisibility(View.GONE);
        }
    }

    @Override
    protected ViewHolder getHolder(View view) {
        return new PostViewHolder(view, new ViewHolder.IViewHolderClick() {
            @Override
            public void onClick(View v, int position) {
                PostViewHolder holder = (PostViewHolder)v.getTag();
                switch(v.getId()) {
                    case R.id.upvote_arrow:
                        if(!new RedditLogin(applicationContext).isLoggedIn()) {
                            Toast.makeText(applicationContext, "Need to be logged in to vote.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        new RedditAPICommon(applicationContext)
                                .vote(getList().get(position).getName(), 1);
                        holder.upvoteArrow.setColorFilter(ContextCompat
                                .getColor(applicationContext, R.color.upvoteOrange));
                        holder.downvoteArrow.setColorFilter(ContextCompat
                                .getColor(applicationContext, android.R.color.darker_gray));
                        holder.postScore.setTextColor(ContextCompat
                                .getColor(applicationContext, R.color.upvoteOrange));
                    case R.id.downvote_arrow:
                        if(!new RedditLogin(applicationContext).isLoggedIn()) {
                            Toast.makeText(applicationContext, "Need to be logged in to vote.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        new RedditAPICommon(applicationContext)
                                .vote(getList().get(position).getName(), -1);
                        holder.downvoteArrow.setColorFilter(ContextCompat
                                .getColor(applicationContext, R.color.downvoteBlue));
                        holder.upvoteArrow.setColorFilter(ContextCompat
                                .getColor(applicationContext, android.R.color.darker_gray));
                        holder.postScore.setTextColor(ContextCompat
                                .getColor(applicationContext, R.color.downvoteBlue));
                    case R.id.link_thumbnail:
                        if(getList().get(position).getUrl().endsWith("jpg")) {
                            ImageFragment imf =
                                    (ImageFragment) ImageFragment.newInstance(
                                            applicationContext, getList().get(position).getUrl());
                            ((AppCompatActivity) applicationContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragments_container, imf)
                                    .addToBackStack("Image")
                                    .commit();
                        }
                    case R.id.browser_image:
                        Fragment wvf =
                                ImprovedWebViewFragment.newInstance(
                                        getList().get(position).getUrl());
                        ((AppCompatActivity) applicationContext).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragments_container, wvf)
                                .addToBackStack("Web")
                                .commit();
                    default:
                        RedditLogin rl = new RedditLogin(applicationContext);
                        if(rl.isLoggedIn()) {
                            SharedPreferences.Editor edit = applicationContext
                                    .getSharedPreferences(Consts.SPREFS_READ_POSTS + rl.getCurrentUser(),
                                            Context.MODE_PRIVATE).edit();
                            edit.putString(getList().get(position).getName(), "true").apply();
                        }


                        String postUrl = Consts.REDDIT_URL + getList().get(position).getPermalink() + ".json";
                        Fragment pf = CommentsRecyclerFragment.newInstance(applicationContext
                                .getApplicationContext(), postUrl);

                        ((AppCompatActivity)applicationContext).getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragments_container, pf)
                                .addToBackStack("Comments")
                                .commit();
                }
            }
        });
    }
}

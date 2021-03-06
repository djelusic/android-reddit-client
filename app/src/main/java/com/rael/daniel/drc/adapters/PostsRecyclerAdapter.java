package com.rael.daniel.drc.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rael.daniel.drc.R;

import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_objects.RedditPost;
import com.rael.daniel.drc.util.Consts;
import com.rael.daniel.drc.util.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for posts RecyclerView
 */
public class PostsRecyclerAdapter extends RedditRecyclerAdapter<RedditPost> {
    private boolean showSubreddit;
    ViewHolder.IViewHolderClick listener;

    public static class PostViewHolder extends RedditRecyclerAdapter.ViewHolder {
        public ImageView upvoteArrow;
        public TextView postScore;
        public ImageView downvoteArrow;
        public TextView postTitle;
        public TextView postDetails;
        public TextView postComments;
        public ImageView browserImage;
        public ImageView linkThumbnail;
        public TextView postSubreddit;

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
                                boolean showSubreddit, ViewHolder.IViewHolderClick listener) {
        super(applicationContext, list, item_layout_id, recyclerView);
        this.showSubreddit = showSubreddit;
        this.listener = listener;
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
        RedditLogin rl = new RedditLogin(getApplicationContext());
        if(rl.isLoggedIn()) {
            SharedPreferences sprefs = getApplicationContext()
                    .getSharedPreferences(Consts.SPREFS_READ_POSTS + rl.getCurrentUser(),
                            Context.MODE_PRIVATE);
            if(sprefs.getString(getList().get(position).getName(), "false").equals("true"))
                postViewHolder.postTitle.setTextColor(ContextCompat
                        .getColor(getApplicationContext(), R.color.visited));
            else //Get default color in case the view is recycled, don't really know a better way to do this
                postViewHolder.postTitle.setTextColor(new TextView(getApplicationContext())
                        .getTextColors().getDefaultColor());
        }

        //Make sure to reset colors in case view is recycled
        postViewHolder.upvoteArrow.setColorFilter(ContextCompat
                .getColor(getApplicationContext(), android.R.color.darker_gray));
        postViewHolder.downvoteArrow.setColorFilter(ContextCompat
                .getColor(getApplicationContext(), android.R.color.darker_gray));
        postViewHolder.postScore.setTextColor(ContextCompat
                .getColor(getApplicationContext(), android.R.color.darker_gray));

        if(getList().get(position).isUpvoted()) {
            postViewHolder.upvoteArrow.setColorFilter(ContextCompat
                    .getColor(getApplicationContext(), R.color.upvoteOrange));
            postViewHolder.postScore.setTextColor(ContextCompat
                    .getColor(getApplicationContext(), R.color.upvoteOrange));
        }
        else if(getList().get(position).isDownvoted()) {
            postViewHolder.downvoteArrow.setColorFilter(ContextCompat
                    .getColor(getApplicationContext(), R.color.downvoteBlue));
            postViewHolder.postScore.setTextColor(ContextCompat
                    .getColor(getApplicationContext(), R.color.downvoteBlue));
        }
        //Adds subreddit name to the view (eg when displaying posts from /r/all)
        if(showSubreddit) {
            postViewHolder.postSubreddit.setText(getList().get(position).getSubreddit());
        }

        //Selfposts don't contain external links
        if(getList().get(position).getDomain().startsWith("self")) {
            postViewHolder.browserImage.setVisibility(View.VISIBLE);
            postViewHolder.browserImage.setImageDrawable(getApplicationContext()
                    .getDrawable(R.drawable.ic_play_circle_outline_white_24dp));
            postViewHolder.linkThumbnail.setVisibility(View.GONE);
        }

        else if(!getList().get(position).getThumbnailUrl().equals("default")
                && !getList().get(position).getThumbnailUrl().equals("")) {
            //image link, load thumbnail
            postViewHolder.browserImage.setVisibility(View.GONE);
            postViewHolder.linkThumbnail
                    .setVisibility(View.VISIBLE);
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    35, getApplicationContext().getResources().getDisplayMetrics());
            Picasso.with(getApplicationContext()).load(getList().get(position)
                    .getThumbnailUrl()).transform(new RoundedTransformation(10, 0))
                    .resize((int) px, (int) px).centerCrop().into(postViewHolder.linkThumbnail);
        }
        else {
            postViewHolder.browserImage.setVisibility(View.VISIBLE);
            postViewHolder.browserImage.setImageDrawable(getApplicationContext()
                    .getDrawable(R.drawable.ic_language_black_24dp));
            postViewHolder.linkThumbnail.setVisibility(View.GONE);
        }
    }

    @Override
    protected ViewHolder getHolder(View view) {
        return new PostViewHolder(view, listener);
    }
}

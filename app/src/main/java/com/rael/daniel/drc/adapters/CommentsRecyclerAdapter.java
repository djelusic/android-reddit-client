package com.rael.daniel.drc.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.fragments.CommentsRecyclerFragment;
import com.rael.daniel.drc.reddit_objects.RedditComment;

import java.util.List;

/**
 * Created by Daniel on 05/11/2015.
 */
public class CommentsRecyclerAdapter extends RedditRecyclerAdapter<RedditComment> {
    RedditComment link;
    CommentsRecyclerFragment fragment;
    boolean spinnerSetup = false;
    View separatorView;

    private final int VIEW_TYPE_OP_COMMENT = 0;
    private final int VIEW_TYPE_COMMENT_SEPARATOR = 1;
    private final int VIEW_TYPE_REGULAR_COMMENT = 2;
    private final int VIEW_TYPE_MORE_COMMENTS_STUB = 3;
    private final int VIEW_TYPE_OP_LINK = 4;
    private final int VIEW_TYPE_PROGRESS_BAR = 5;

    public static class CommentViewHolder extends RedditRecyclerAdapter.ViewHolder {
        TextView commentUser;
        TextView commentScore;
        TextView commentText;
        TextView commentTime;
        ViewGroup outerLayout;
        ViewGroup innerLayout;

        public CommentViewHolder( View itemView, IViewHolderClick listener) {
            super (itemView, listener);
            commentUser = (TextView)itemView.findViewById(
                    R.id.comment_user);
            commentScore = (TextView)itemView.findViewById(
                    R.id.comment_score);
            commentText = (TextView)itemView.findViewById(
                    R.id.comment_text);
            commentTime = (TextView) itemView.findViewById(
                    R.id.comment_time);
            outerLayout = (ViewGroup)itemView.findViewById(
                    R.id.comment_outer_layout);
            innerLayout = (ViewGroup)itemView.findViewById(
                    R.id.comment_inner_layout);
        }
    }
    public static class LinkViewHolder extends RedditRecyclerAdapter.ViewHolder {
        TextView linkTitle;
        TextView linkAdditional;

        public LinkViewHolder(View itemView, IViewHolderClick listener) {
            super(itemView, listener);
            linkTitle = (TextView)itemView.findViewById(
                    R.id.link_title);
            linkAdditional = (TextView)itemView.findViewById(
                    R.id.link_additional);
        }
    }

    public static class MoreCommentsStubHolder extends RedditRecyclerAdapter.ViewHolder {
        ViewGroup layout;

        public MoreCommentsStubHolder(View itemView, IViewHolderClick listener) {
            super(itemView, listener);
            layout = (LinearLayout)itemView.findViewById(
                    R.id.more_comments_layout);
        }
    }

    public CommentsRecyclerAdapter(Context applicationContext, List<RedditComment> list,
                                   int item_layout_id, RecyclerView recyclerView,
                                   RedditComment link, CommentsRecyclerFragment fragment) {
        super(applicationContext, list, item_layout_id, recyclerView);
        this.link = link;
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 && link.isSelfPost())
            return VIEW_TYPE_OP_COMMENT;
        if(position == 0 && !link.isSelfPost())
            return VIEW_TYPE_OP_LINK;
        if(position == 1)
            return VIEW_TYPE_COMMENT_SEPARATOR;

        if(getList().get(position).isMoreCommentsStub())
            return VIEW_TYPE_MORE_COMMENTS_STUB;
        if(getList().get(position) == null) {
            return VIEW_TYPE_PROGRESS_BAR;
        }
        return VIEW_TYPE_REGULAR_COMMENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == VIEW_TYPE_OP_COMMENT && link != null) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.comment_item_layout, viewGroup, false);
            return getHolder(view);
        }
        if(viewType == VIEW_TYPE_OP_LINK && link != null){
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.link_layout, viewGroup, false);
            return new LinkViewHolder(view, null);
        }
        if(viewType == VIEW_TYPE_MORE_COMMENTS_STUB){
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.more_comments_layout, viewGroup, false);
            return new MoreCommentsStubHolder(view, null);
        }
        if(viewType == VIEW_TYPE_COMMENT_SEPARATOR){
            if(!spinnerSetup) {
                separatorView = fragment.setupSpinner();
                spinnerSetup = true;
            }
            return new RecyclerView.ViewHolder(separatorView) {
                @Override
                public String toString() {
                    return super.toString();
                }
            };
        }
        else {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(item_layout_id, viewGroup, false);
            return getHolder(view);
        }
    }

    private void addRedditCommentStyle(ViewGroup outerLayout, ViewGroup innerLayout, int position) {
        innerLayout.setBackgroundResource(getList().get(position).getDepth() % 2 == 0 ?
                R.drawable.borders_white : R.drawable.borders_grey);
        for (int i = 0; i < getList().get(position).getDepth(); i++) {
            View v = new View(applicationContext);
            v.setBackgroundResource(i % 2 == 0 ?
                    R.drawable.borders_white : R.drawable.borders_grey);
            //v.setBackgroundColor(i%2 == 0 ? Color.WHITE : Color.parseColor("#F2F2F2"));
            v.setLayoutParams(new LinearLayout.LayoutParams(20, LinearLayout.
                    LayoutParams.MATCH_PARENT));
            outerLayout.addView(v, i);
        }
    }

    @Override
    protected void bindItem(RecyclerView.ViewHolder holder, int position) {
        if((getItemViewType(position) == VIEW_TYPE_OP_COMMENT && link != null)
                || getItemViewType(position) == VIEW_TYPE_REGULAR_COMMENT) {
            RedditComment comment;
            if(getItemViewType(position) == VIEW_TYPE_OP_COMMENT)
                comment = link;
            else comment = getList().get(position);
            CommentViewHolder commentHolder = (CommentViewHolder) holder;
            commentHolder.commentUser.setText(comment.getUser());
            commentHolder.commentScore.setText(comment.getScore());
            Spanned spannedText = Html.fromHtml(comment.getText());

            if (spannedText.length() > 2 && !spannedText.toString().equals("null"))
                commentHolder.commentText.setText(spannedText
                        .subSequence(0, spannedText.length() - 2));
            else commentHolder.commentText.setText("");

            commentHolder.commentTime.setText(" points, " + comment.getDate());
            if(comment.isUpvoted())
                commentHolder.commentScore.setTextColor(ContextCompat.getColor(
                        applicationContext, R.color.upvoteOrange));
            else if(comment.isDownvoted())
                commentHolder.commentScore.setTextColor(ContextCompat.getColor(
                        applicationContext, R.color.downvoteBlue));
            commentHolder.commentText
                    .setMovementMethod(LinkMovementMethod.getInstance());

            if(getItemViewType(position) == VIEW_TYPE_REGULAR_COMMENT) {
                addRedditCommentStyle(commentHolder.outerLayout,
                        commentHolder.innerLayout, position);
            }
        }
        if(getItemViewType(position) == VIEW_TYPE_OP_LINK && link != null) {
            LinkViewHolder linkHolder = (LinkViewHolder) holder;
            linkHolder.linkTitle.setText(link.getTitle());
            linkHolder.linkAdditional.setText("by " +
                    link.getUser() + " (" + link.getDomain() + ")");
        }

        //Handle "more comments" stubs
        if(getItemViewType(position) == VIEW_TYPE_MORE_COMMENTS_STUB) {
            MoreCommentsStubHolder moreHolder = (MoreCommentsStubHolder) holder;
            addRedditCommentStyle(moreHolder.layout, moreHolder.layout, position);
        }

    }

    @Override
    protected ViewHolder getHolder(View view) {
        return new CommentViewHolder(view, null);
    }
}

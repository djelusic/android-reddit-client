package com.rael.daniel.drc.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.fragments.CommentsRecyclerFragment;
import com.rael.daniel.drc.reddit_objects.RedditComment;
import com.rael.daniel.drc.reddit_objects.RedditPost;

import java.util.List;

/**
 * Created by Daniel on 05/11/2015.
 */
public class CommentsRecyclerAdapter extends RedditRecyclerAdapter<RedditComment> {
    RedditPost link;
    CommentsRecyclerFragment fragment;
    boolean spinnerSetup = false;
    View separatorView;

    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_REGULAR_COMMENT = 1;
    private final int VIEW_TYPE_MORE_COMMENTS_STUB = 2;
    private final int VIEW_TYPE_PROGRESS_BAR = 3;
    private final int VIEW_TYPE_COLLAPSED_ITEM = 4;

    public static class CommentViewHolder extends RedditRecyclerAdapter.ViewHolder {
        TextView commentUser;
        TextView commentScore;
        TextView commentText;
        TextView commentTime;
        ViewGroup indentHolder;
        ViewGroup innerLayout;
        View divider;

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
            indentHolder = (ViewGroup)itemView.findViewById(
                    R.id.indent_holder);
            innerLayout = (ViewGroup)itemView.findViewById(
                    R.id.comment_inner_layout);
            divider = itemView.findViewById(R.id.divider);
        }
    }
    public static class OPCommentViewHolder extends RedditRecyclerAdapter.ViewHolder {
        ImageView browserImage;
        TextView linkTitle;
        TextView linkAdditional;
        TextView linkSelftext;

        public OPCommentViewHolder(View itemView, IViewHolderClick listener) {
            super(itemView, listener);
            browserImage = (ImageView)itemView.findViewById(
                    R.id.browser_image);
            linkTitle = (TextView)itemView.findViewById(
                    R.id.link_title);
            linkAdditional = (TextView)itemView.findViewById(
                    R.id.link_additional);
            linkSelftext = (TextView)itemView.findViewById(
                    R.id.link_selftext);
        }
    }

    public static class MoreCommentsStubHolder extends RedditRecyclerAdapter.ViewHolder {
        ViewGroup layout;
        ViewGroup indentHolder;

        public MoreCommentsStubHolder(View itemView, IViewHolderClick listener) {
            super(itemView, listener);
            layout = (LinearLayout)itemView.findViewById(
                    R.id.more_comments_layout);
            indentHolder = (LinearLayout)itemView.findViewById(
                    R.id.indent_holder);
        }
    }

    public CommentsRecyclerAdapter(Context applicationContext, List<RedditComment> list,
                                   int item_layout_id, RecyclerView recyclerView,
                                   RedditPost link, CommentsRecyclerFragment fragment) {
        super(applicationContext, list, item_layout_id, recyclerView);
        this.link = link;
        this.fragment = fragment;
    }

    private RedditComment getItem(int position) {
        return getList().get(position - 1);
    }

    @Override
    public int getItemCount() {
        return (null != getList() ? getList().size() + 1: 1);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return VIEW_TYPE_HEADER;
        if(getItem(position) == null)
            return VIEW_TYPE_PROGRESS_BAR;
        if(getItem(position).isHidden())
            return VIEW_TYPE_COLLAPSED_ITEM;
        if(getItem(position).isMoreCommentsStub())
            return VIEW_TYPE_MORE_COMMENTS_STUB;
        return VIEW_TYPE_REGULAR_COMMENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == VIEW_TYPE_PROGRESS_BAR) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.progress_item, viewGroup, false);
            return new ProgressViewHolder(view);
        }
        if(viewType == VIEW_TYPE_HEADER && link != null) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.op_comment_layout, viewGroup, false);
            return new OPCommentViewHolder(view, null);
        }
        if(viewType == VIEW_TYPE_MORE_COMMENTS_STUB){
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.more_comments_layout, viewGroup, false);
            return new MoreCommentsStubHolder(view, null);
        }
        if(viewType == VIEW_TYPE_COLLAPSED_ITEM) {
            return new RecyclerView.ViewHolder(new View(applicationContext)) {
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

    //Renders a comment with proper indentation
    private void addRedditCommentStyle(ViewGroup outerLayout, ViewGroup innerLayout, int position) {
        //get primary color
        TypedValue typedValue = new TypedValue();
        applicationContext.getTheme()
                .resolveAttribute(R.attr.commentBackgroundPrimary, typedValue, true);
        //we don't want top level comments to have a divider on the left side
        if(getItem(position).getDepth() == 0) {
            innerLayout.setBackgroundColor(typedValue.data);
        }
        else {
            innerLayout.setBackgroundResource(getItem(position).getDepth() % 2 == 0 ?
                    R.drawable.borders_primary : R.drawable.borders_secondary);
        }
        outerLayout.removeAllViews();
        for (int i = 0; i < getItem(position).getDepth(); i++) {
            View v = new View(applicationContext);
            if(i == 0) {
                v.setBackgroundColor(typedValue.data);
            }
            else {
                v.setBackgroundResource(i % 2 == 0 ?
                        R.drawable.borders_primary : R.drawable.borders_secondary);
            }
            //v.setBackgroundColor(i%2 == 0 ? Color.WHITE : Color.parseColor("#F2F2F2"));
            v.setLayoutParams(new LinearLayout.LayoutParams(20, LinearLayout.
                    LayoutParams.MATCH_PARENT));
            outerLayout.addView(v, i);
        }
    }

    @Override
    protected void bindItem(RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_HEADER && link != null) {
            OPCommentViewHolder opHolder = (OPCommentViewHolder) holder;
            opHolder.linkTitle.setText(link.getTitle());
            opHolder.linkAdditional.setText("by " + link.getAuthor()
                    + ", " + link.getDate());
            if(link.isSelfPost()) {
                Spanned spannedText = Html.fromHtml(link.getSelftext());
                if (spannedText.length() > 2 && !spannedText.toString().equals("null"))
                    opHolder.linkSelftext.setText(spannedText
                            .subSequence(0, spannedText.length() - 2));
                opHolder.browserImage.setVisibility(View.GONE);
                opHolder.linkSelftext.setVisibility(View.VISIBLE);
            }
        }
        if(getItemViewType(position) == VIEW_TYPE_REGULAR_COMMENT) {
            RedditComment comment = getItem(position);
            CommentViewHolder commentHolder = (CommentViewHolder) holder;
            if(position == 1)
                commentHolder.divider.setVisibility(View.GONE);
            else
                commentHolder.divider.setVisibility(View.VISIBLE);
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

            addRedditCommentStyle(commentHolder.indentHolder,
                    commentHolder.innerLayout, position);
        }

        //Handle "more comments" stubs
        if(getItemViewType(position) == VIEW_TYPE_MORE_COMMENTS_STUB) {
            MoreCommentsStubHolder moreHolder = (MoreCommentsStubHolder) holder;
            addRedditCommentStyle(moreHolder.indentHolder, moreHolder.layout, position);
        }

    }

    @Override
    protected ViewHolder getHolder(View view) {
        return new CommentViewHolder(view, new ViewHolder.IViewHolderClick() {
            @Override
            public void onClick(View v, int position) {
                int i = position + 1;
                boolean nextHidden = getItem(i).isHidden();
                while(getItem(position).getDepth() <
                        getItem(i).getDepth()) {
                    getItem(i).setHidden(!nextHidden);
                    i++;
                }
                notifyItemRangeChanged(position + 1, i - position - 1);
            }
        });
    }
}

package com.rael.daniel.drc.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.fragments.PostsFragment;
import com.rael.daniel.drc.fragments.PostsRecyclerFragment;
import com.rael.daniel.drc.reddit_objects.RedditSubreddit;

import java.util.List;

/**
 * Created by Daniel on 05/11/2015.
 */
public class SubredditsRecyclerAdapter extends RedditRecyclerAdapter<RedditSubreddit> {
    public static class SubredditViewHolder extends RedditRecyclerAdapter.ViewHolder {
        TextView title;
        TextView description;

        public SubredditViewHolder( View itemView, IViewHolderClick listener) {
            super (itemView, listener);
            title = (TextView)itemView.findViewById(
                    R.id.subreddit_title);
            description = (TextView) itemView.findViewById(
                    R.id.subreddit_description);
        }
    }

    public SubredditsRecyclerAdapter(Context applicationContext,
                                     List<RedditSubreddit> list, int item_layout_id, RecyclerView recyclerView) {
        super(applicationContext, list, item_layout_id, recyclerView);
    }

    @Override
    protected RedditRecyclerAdapter.ViewHolder getHolder(View view) {
        return new SubredditViewHolder(view, new ViewHolder.IViewHolderClick() {
            @Override
            public void onClick(View v, int position) {
                String clickedSubreddit = getList().get(position).getUrl();
                Fragment sf = PostsRecyclerFragment.newInstance(applicationContext,
                        clickedSubreddit, null, null, false);

                ((AppCompatActivity)applicationContext).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragments_container, sf)
                        .addToBackStack(clickedSubreddit)
                        .commit();
            }
        });
    }

    @Override
    protected void bindItem(RecyclerView.ViewHolder holder, int position) {
        SubredditViewHolder sHolder = (SubredditViewHolder)holder;
        sHolder.title.setText(getList().get(position).getUrl());
        sHolder.description.setText(getList().get(position).getTitle());
    }
}

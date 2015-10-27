package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.misc.RedditRecyclerAdapter;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;
import com.rael.daniel.drc.reddit_fetchers.SubredditFetcher;
import com.rael.daniel.drc.reddit_objects.RedditSubreddit;

/**
 * Created by Daniel on 12/10/2015.
 */
public class SubredditsRecyclerFragment extends RecyclerFragment<RedditSubreddit> {

    public class SubredditViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        public SubredditViewHolder(View itemView) {
            super (itemView);
            title = (TextView)itemView.findViewById(
                    R.id.subreddit_title);
            description = (TextView) itemView.findViewById(
                    R.id.subreddit_description);
        }
    }

    public SubredditsRecyclerFragment() {
        this.layout_id = R.layout.subreddit_rlist_layout;
        this.rlist_id = R.id.subreddit_rlist;
    }

    public static Fragment newInstance(Context applicationContext){
        SubredditsRecyclerFragment sf=new SubredditsRecyclerFragment();
        sf.lFetcher =new SubredditFetcher(applicationContext);
        return sf;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final SearchView sv = (SearchView)menu.findItem(R.id.action_search)
                .getActionView();
        sv.setQueryHint("Enter a subreddit manually");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Fragment sf = PostsFragment.newInstance(getActivity()
                        .getApplicationContext(), query, null, "", false);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, sf)
                        .addToBackStack(null)
                        .commit();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    @Override
    protected void createAndBindAdapter() {
        adapter =
                new RedditRecyclerAdapter<RedditSubreddit>(getContext(), list,
                        R.layout.subreddit_item_layout) {
                    @Override
                    protected RecyclerView.ViewHolder getHolder(View view) {
                        return new SubredditViewHolder(view);
                    }

                    @Override
                    protected void bindItem(RecyclerView.ViewHolder holder, int position) {
                        SubredditViewHolder sHolder = (SubredditViewHolder)holder;
                        sHolder.title.setText(getList().get(position).getUrl());
                        sHolder.description.setText(getList().get(position).getTitle());
                    }
                };
        rView.setAdapter(adapter);
    }

    @Override
    protected ListFetcher<RedditSubreddit> getFetcher() {
        return new SubredditFetcher(getContext());
    }
}

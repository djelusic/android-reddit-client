package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.adapters.RedditRecyclerAdapter;
import com.rael.daniel.drc.adapters.SubredditsRecyclerAdapter;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;
import com.rael.daniel.drc.reddit_fetchers.SubredditFetcher;
import com.rael.daniel.drc.reddit_objects.RedditSubreddit;

/**
 * Created by Daniel on 12/10/2015.
 */
public class SubredditsRecyclerFragment extends RecyclerFragment<RedditSubreddit> {

    public SubredditsRecyclerFragment() {
        this.item_layout_id = R.layout.subreddit_item_layout;
    }

    @Override
    public void myRefresh() {
        initialized = false;
        getList().clear();
        lFetcher = new SubredditFetcher(getActivity()
                .getApplicationContext());
        initialize(false);
    }

    public static Fragment newInstance(Context applicationContext){
        SubredditsRecyclerFragment sf=new SubredditsRecyclerFragment();
        sf.lFetcher =new SubredditFetcher(applicationContext);
        return sf;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity().setTitle("Subreddits");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.setGroupVisible(R.id.sort_posts, false);
        final SearchView sv = (SearchView)menu.findItem(R.id.action_search)
                .getActionView();
        sv.setQueryHint("Enter a subreddit manually");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Fragment sf = PostsRecyclerFragment.newInstance(getActivity()
                        .getApplicationContext(), query, null, "", false);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, sf, query)
                        .addToBackStack(query)
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
        adapter = new SubredditsRecyclerAdapter(getContext(), getList(), item_layout_id, rView,
                new RedditRecyclerAdapter.ViewHolder.IViewHolderClick() {
                    @Override
                    public void onClick(View v, int position) {
                        /*setSharedElementReturnTransition(TransitionInflater
                                .from(getActivity()).inflateTransition(R.transition.test_transition));*/
                        setExitTransition(TransitionInflater.from(getActivity())
                                .inflateTransition(android.R.transition.explode));
                        String clickedSubreddit = getList().get(position).getUrl();
                        Fragment sf = PostsRecyclerFragment.newInstance(getContext(),
                                clickedSubreddit, null, null, false);
                        /*sf.setSharedElementEnterTransition(TransitionInflater
                                .from(getActivity()).inflateTransition(R.transition.test_transition));*/
                        sf.setEnterTransition(TransitionInflater.from(getActivity())
                                .inflateTransition(android.R.transition.explode));

                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragments_container, sf, clickedSubreddit)
                                .addToBackStack(clickedSubreddit)
                                //.addSharedElement(contentView.findViewById(R.id.subreddit_item), "sub_post_transition")
                                .commit();
                    }
                });
        rView.setAdapter(adapter);
    }

    @Override
    protected ListFetcher<RedditSubreddit> getFetcher() {
        return new SubredditFetcher(getContext());
    }
}

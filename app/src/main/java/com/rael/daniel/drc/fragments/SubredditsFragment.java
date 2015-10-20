package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.reddit_fetchers.SubredditFetcher;
import com.rael.daniel.drc.reddit_objects.RedditSubreddit;

import java.util.List;

public class SubredditsFragment extends ListFragment<RedditSubreddit> {
    ListView subredditsListView;
    ArrayAdapter<RedditSubreddit> adapter;
    Handler handler;

    public SubredditsFragment(){
        super();
        layout_id = R.layout.subreddit_list_layout;
        item_layout_id = R.layout.subreddit_item_layout;
        list_id = R.id.subreddit_list;
    }

    public static Fragment newInstance(Context applicationContext){
        SubredditsFragment sf=new SubredditsFragment();
        sf.lFetcher =new SubredditFetcher(applicationContext);
        return sf;
    }

    @Override
    public void myRefresh() {
        getList().clear();
        lFetcher = new SubredditFetcher(getActivity()
                .getApplicationContext());
        initialize();
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
        final SearchView sv = (SearchView)menu.findItem(R.id.action_search)
                .getActionView();
        sv.setQueryHint("Enter a subreddit manually");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Fragment sf = PostsFragment.newInstance(getActivity()
                        .getApplicationContext(),
                        "http://www.reddit.com/r/" + query, false);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, sf)
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
    void fetchAdditionalItems() {

    }

    @Override
    void getAdditionalViews() {

    }

    View fillItems(List<RedditSubreddit> subreddits, View convertView, int position) {
        TextView subredditUrl;
        subredditUrl=(TextView)convertView
                .findViewById(R.id.subreddit_title);

        TextView subredditTitle;
        subredditTitle=(TextView)convertView
                .findViewById(R.id.subreddit_description);

        subredditUrl.setText(subreddits.get(position).getUrl());
        subredditTitle.setText(subreddits.get(position).getTitle());
        return convertView;
    }

    void setOnClick(final List<RedditSubreddit> subreddits, ListView lView, int position) {
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String clickedSubreddit = subreddits.get(position).getUrl();
                Fragment sf = PostsFragment.newInstance(getActivity()
                        .getApplicationContext(), "http://www.reddit.com/r/" +
                        clickedSubreddit, false);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, sf)
                        .addToBackStack(clickedSubreddit)
                        .commit();
            }
        });
    }
}

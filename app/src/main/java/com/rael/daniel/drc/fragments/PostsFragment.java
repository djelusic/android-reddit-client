package com.rael.daniel.drc.fragments;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.reddit_api.RedditAPICommon;
import com.rael.daniel.drc.reddit_fetchers.PostFetcher;
import com.rael.daniel.drc.reddit_objects.RedditPost;
import com.rael.daniel.drc.util.Consts;

import java.util.List;

/**
 * Fragment that displays fetched posts
 */
public class PostsFragment extends ListFragment<RedditPost> {

    String url;
    private boolean showSubreddit;

    public PostsFragment(){
        super();
        layout_id = R.layout.post_list_layout;
        item_layout_id = R.layout.post_item_layout;
        list_id = R.id.post_list;
        loadMoreOnScroll = true;
    }

    @Override
    public void myRefresh() {
        getList().clear();
        lFetcher = new PostFetcher(getActivity()
                .getApplicationContext(), url);
        initialize(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final SearchView sv = (SearchView)menu.findItem(R.id.action_search)
                .getActionView();
        final MenuItem sortSubmenuContainer = menu.findItem(R.id.sort_submenu);
        sortSubmenuContainer.setVisible(true);
        final Menu sortSubmenu = sortSubmenuContainer.getSubMenu();

        sv.setQueryHint("Search this subreddit");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.replace(" ", "+");
                Fragment sf = PostsFragment.newInstance(getActivity()
                                .getApplicationContext(),
                        url + "search.json?q=" + query
                                + "&restrict_sr=on&sort=relevance&t=all", false);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, sf)
                        .addToBackStack("Search")
                        .commit();
                sv.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    public void refreshFromUrl(String url) {
        getList().clear();
        lFetcher = new PostFetcher(getActivity()
                .getApplicationContext(), url);
        initialize(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(super.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.sort_hot:
                refreshFromUrl(url + "hot");
                return true;
            case R.id.sort_new:
                refreshFromUrl(url + "new");
                return true;
            case R.id.sort_top:
                refreshFromUrl(url + "top");
                return true;
            default:
                return false;
        }
    }

    public static Fragment newInstance(Context applicationContext,
                                       String url, boolean showSubreddit){
        PostsFragment pf=new PostsFragment();
        pf.url =url;
        pf.lFetcher =new PostFetcher(applicationContext, pf.url);
        pf.showSubreddit = showSubreddit;
        return pf;
    }

    @Override
    View fillItems(final List<RedditPost> posts, View convertView, final int position) {

        if(convertView == null)
            convertView=getActivity()
                    .getLayoutInflater()
                    .inflate(item_layout_id, null);

        ((TextView)convertView.findViewById(R.id.post_title))
                .setText(posts.get(position).getTitle());
        ((TextView)convertView.findViewById(R.id.post_details))
                .setText("submitted " + posts.get(position).getDate() + " ago by "
                + posts.get(position).getAuthor() + " (" + posts.get(position).getDomain() + ")");
        ((TextView)convertView.findViewById(R.id.post_comments))
                .setText(posts.get(position).getNumComments() + " comments");
        ((TextView)convertView.findViewById(R.id.post_score))
                .setText(String.valueOf(posts.get(position).getPoints()));

        final TextView score = (TextView)convertView.findViewById(R.id.post_score);
        final ImageView upvoteArrow = (ImageView)convertView.findViewById(R.id.upvote_arrow);
        final ImageView downvoteArrow = (ImageView)convertView.findViewById(R.id.downvote_arrow);

        //Make sure to reset colors in case view is recycled
        upvoteArrow.setColorFilter(ContextCompat
                .getColor(getContext(), android.R.color.darker_gray));
        downvoteArrow.setColorFilter(ContextCompat
                .getColor(getContext(), android.R.color.darker_gray));
        score.setTextColor(ContextCompat
                .getColor(getContext(), android.R.color.darker_gray));

        if(posts.get(position).isUpvoted()) {
            upvoteArrow.setColorFilter(ContextCompat
                    .getColor(getContext(), R.color.upvoteOrange));
            score.setTextColor(ContextCompat
                    .getColor(getContext(), R.color.upvoteOrange));
        }
        else if(posts.get(position).isDownvoted()) {
            downvoteArrow.setColorFilter(ContextCompat
                    .getColor(getContext(), R.color.downvoteBlue));
            score.setTextColor(ContextCompat
                    .getColor(getContext(), R.color.downvoteBlue));
        }
        //Adds subreddit name to the view (eg when displaying posts from /r/all)
        if(showSubreddit) {
            ((TextView) convertView.findViewById(R.id.post_subreddit))
                    .setText(posts.get(position).getSubreddit());
        }

        //Selfposts don't contain external links
        if(posts.get(position).getDomain().startsWith("self")) {
            convertView.findViewById(R.id.browser_image)
                    .setVisibility(View.GONE);
        }
        else { //Open link in a WebView fragment
            convertView.findViewById(R.id.browser_image)
                    .setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.browser_image)
                    .setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Fragment wvf =
                                            ImprovedWebViewFragment.newInstance(
                                                    posts.get(position).getUrl()
                                            );
                                    getFragmentManager().beginTransaction()
                                            .replace(R.id.fragments_container, wvf)
                                            .addToBackStack("Web")
                                            .commit();
                                }
                            }
                    );
        }

        //Set onclick listeners for upvote/downvote arrows
        upvoteArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RedditAPICommon(getActivity().getApplicationContext())
                        .vote(getList().get(position).getName(), 1);
                upvoteArrow.setColorFilter(ContextCompat
                        .getColor(getContext(), R.color.upvoteOrange));
                downvoteArrow.setColorFilter(ContextCompat
                        .getColor(getContext(), android.R.color.darker_gray));
                score.setTextColor(ContextCompat
                        .getColor(getContext(), R.color.upvoteOrange));
            }
        });
        downvoteArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RedditAPICommon(getActivity().getApplicationContext())
                        .vote(getList().get(position).getName(), -1);
                downvoteArrow.setColorFilter(ContextCompat
                        .getColor(getContext(), R.color.downvoteBlue));
                upvoteArrow.setColorFilter(ContextCompat
                        .getColor(getContext(), android.R.color.darker_gray));
                score.setTextColor(ContextCompat
                        .getColor(getContext(), R.color.downvoteBlue));
            }
        });
        return convertView;
    }

    @Override
    void setOnClick(final List<RedditPost> posts, ListView lView, int position) {
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String postUrl = Consts.REDDIT_URL + posts.get(position).getPermalink() + ".json";
                Fragment pf = CommentsFragment.newInstance(getActivity()
                        .getApplicationContext(), postUrl);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, pf)
                        .addToBackStack("Comments")
                        .commit();
            }
        });
    }
}

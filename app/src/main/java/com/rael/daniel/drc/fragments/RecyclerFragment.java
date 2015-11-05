package com.rael.daniel.drc.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rael.daniel.drc.activities.LoginActivity;
import com.rael.daniel.drc.R;
import com.rael.daniel.drc.activities.MainActivity;
import com.rael.daniel.drc.adapters.RedditRecyclerAdapter;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;
import com.rael.daniel.drc.reddit_login.RedditLogin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 12/10/2015.
 */
public abstract class RecyclerFragment<T> extends Fragment {
    private List<T> list;
    RecyclerView rView;
    View contentView;
    RedditRecyclerAdapter<T> adapter;
    ListFetcher<T> lFetcher;
    int layout_id, rlist_id, item_layout_id;
    boolean initialized = false;
    boolean loadMoreOnScroll = true;

    public RecyclerFragment() {
        this.layout_id = R.layout.rlist_layout;
        this.rlist_id = R.id.rlist;
        contentView = null;
        list = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static Fragment newInstance(){
        return null; //implement later
    }

    public abstract void myRefresh();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        contentView=inflater.inflate(layout_id
                , container
                , false);
        rView =(RecyclerView)contentView.findViewById(rlist_id);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
        initialize(false);
        initialized = true;
        return contentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_action_bar, menu);
    }

    //Sets visibility of login/logout menu items
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(new RedditLogin(getActivity().getApplicationContext())
                .isLoggedIn()) {
            menu.findItem(R.id.login).setVisible(false);
            menu.findItem(R.id.logout).setVisible(true);
        }
        else {
            menu.findItem(R.id.login).setVisible(true);
            menu.findItem(R.id.logout).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(i, 1);
                return true;
            case R.id.logout:
                new RedditLogin(getContext()).logout();
                myRefresh();
                return true;
            case R.id.refresh_button:
                myRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setLoadMoreListener() {
        if(!loadMoreOnScroll) return;
        adapter.setOnLoadMoreListener(new RedditRecyclerAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (lFetcher.hasMoreItems()) {
                    new AsyncTask<Void, Void, Void>() {
                        List<T> newList;
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            //add a null element so that the progressbar shows
                            list.add(null);
                            adapter.notifyItemInserted(list.size() - 1);
                        }
                        @Override
                        protected Void doInBackground(Void... params) {
                            newList = lFetcher.getMoreItems();
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void aVoid) {
                            //remove the null element
                            list.remove(list.size() - 1);
                            adapter.notifyItemRemoved(list.size() - 1);
                            adapter.setLoaded();
                            list.addAll(newList);
                            adapter.notifyDataSetChanged();
                        }
                    }.execute();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(((MainActivity)getActivity()).isStateChanged()) {
            myRefresh();
            ((MainActivity)getActivity()).setStateChanged(false);
        }
    }

    // Refresh current fragment after logging in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
            myRefresh();
    }

    protected void initialize(final boolean isUpdate){

        if(!initialized){
            new AsyncTask<Void, Void, String>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    // Show progress bar
                    if(contentView != null) { //Make sure the fragment is still visible
                        contentView.findViewById(R.id.list_progress)
                                .setVisibility(View.VISIBLE);
                        contentView.findViewById(R.id.errors)
                                .setVisibility(View.GONE);
                        rView.setVisibility(View.GONE);
                    }
                }

                @Override
                protected String doInBackground(Void... params) {
                    // populate the main list
                    getList().addAll(lFetcher.getItems());
                    // get any additional items (such as selfposts)
                    getAdditionalItems();
                    return lFetcher.getErrors();
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    //Handle potential errors returned by the API
                    if(result != null && contentView != null) {
                        TextView errors = (TextView)contentView
                                .findViewById(R.id.errors);
                        errors.setText(lFetcher.getErrors());
                        errors.setVisibility(View.VISIBLE);
                        contentView.findViewById(R.id.list_progress)
                                .setVisibility(View.GONE);
                        return;
                    }
                    // Hide progress bar
                    else if(contentView != null) {
                        contentView.findViewById(R.id.errors)
                                .setVisibility(View.GONE);
                        contentView.findViewById(R.id.list_progress)
                                .setVisibility(View.GONE);
                        rView.setVisibility(View.VISIBLE);
                    }
                    if(isUpdate)
                        adapter.notifyDataSetChanged();
                    else {
                        createAndBindAdapter();
                        setLoadMoreListener();
                    }
                }
            }.execute((Void)null);
        }else{
            createAndBindAdapter();
            setLoadMoreListener();
        }
    }

    public List<T> getList() {
        return list;
    }

    protected abstract ListFetcher<T> getFetcher();
    protected abstract void createAndBindAdapter();

    protected void getAdditionalItems() {}
}

package com.rael.daniel.drc.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rael.daniel.drc.reddit_login.LoginActivity;
import com.rael.daniel.drc.R;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic fragment for displaying content on Reddit
* */
public abstract class ListFragment<T> extends Fragment {
    ListView lView;
    ArrayAdapter<T> adapter;
    private List<T> list;
    ListFetcher<T> lFetcher;
    int layout_id, list_id, item_layout_id;
    protected boolean loadMoreOnScroll;

    public ListFragment(){
        setList(new ArrayList<T>());
        setHasOptionsMenu(true);
    }

    public abstract void myRefresh();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate basic layout
        View v=inflater.inflate(layout_id
                , container
                , false);
        lView =(ListView)v.findViewById(list_id);
        return v;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize(false);
    }

    // Refresh current fragment after logging in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
            myRefresh();
    }

    protected void initialize(final boolean isUpdate){

        if(getList().size()==0){

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    // Show progress bar
                    if(getView() != null) { //Make sure the fragment is still visible
                        getView().findViewById(R.id.list_progress)
                                .setVisibility(View.VISIBLE);
                    }
                }

                @Override
                protected Void doInBackground(Void... params) {
                    // populate the main list
                    getList().addAll(lFetcher.getItems());
                    // get any additional items (such as selfposts)
                    getAdditionalItems();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    // Hide progress bar
                    if(getView() != null) {
                        getView().findViewById(R.id.list_progress)
                                .setVisibility(View.GONE);
                    }
                    if(isUpdate)
                        notifyChange();
                    else createAdapter();
                }
            }.execute((Void)null);
        }else{
            createAdapter();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    protected int mGetViewTypeCount() {
        return 1;
    }

    protected int mGetItemViewType(int position) {
        return 0;
    }

    private void createAdapter(){

        // Make sure this fragment is still a part of the activity.
        if(getActivity()==null) return;


        adapter=new ArrayAdapter<T>(getActivity()
                ,item_layout_id
                , getList()){
            @Override
            public View getView(int position,
                                View convertView,
                                ViewGroup parent) {

                //inflate list items
                /*if(convertView == null)
                    convertView=getActivity()
                            .getLayoutInflater()
                            .inflate(item_layout_id, null);*/

                //Actual adapter implementation is delegated to superclass
                convertView = fillItems(getList(), convertView, position);
                setOnClick(getList(), lView, position);
                return convertView;
            }

            @Override
            public int getViewTypeCount() {
                return mGetViewTypeCount();
            }

            @Override
            public int getItemViewType(int position) {
                return mGetItemViewType(position);
            }
        };
        lView.setAdapter(adapter);
        if(loadMoreOnScroll) {
            lView.setOnScrollListener(new AbsListView.OnScrollListener() {
                private boolean isLoading = false;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {}

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    int lastIndex = firstVisibleItem + visibleItemCount;
                    if (lastIndex == getList().size() &&
                            lFetcher.hasMoreItems() && !isLoading) {
                        new AsyncTask<Void, Void, Void>() {
                            View progressBar;
                            @Override
                            protected void onPreExecute() {
                                progressBar = getActivity().getLayoutInflater()
                                        .inflate(R.layout.small_progress_bar, null);
                                lView.addFooterView(progressBar);
                                super.onPreExecute();
                                isLoading = true;
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                list.addAll(lFetcher.getMoreItems());
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                notifyChange();
                                isLoading = false;
                                lView.removeFooterView(progressBar);
                            }
                        }.execute((Void)null);
                    }
                }
            });
        }
    }

    void getAdditionalItems() {} //Implement in superclass
    void getAdditionalViews() {} //Implement in superclass
    abstract View fillItems(List<T> list, View convertView, int position);
    abstract void setOnClick(List<T> list, ListView lView, int position);

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void notifyChange() {
        adapter.notifyDataSetChanged();
    }
}

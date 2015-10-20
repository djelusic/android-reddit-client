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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rael.daniel.drc.reddit_login.LoginActivity;
import com.rael.daniel.drc.R;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;

import java.util.ArrayList;
import java.util.List;

public abstract class ListFragment<T> extends Fragment {
    ListView lView;
    ArrayAdapter<T> adapter;
    private List<T> list;
    ListFetcher<T> lFetcher;
    int layout_id, list_id, item_layout_id;
    boolean headersInitiated = false;

    public ListFragment(){
        setList(new ArrayList<T>());
        setHasOptionsMenu(true);
    }

    public static Fragment newInstance(){
        return null; //implement later
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
        initialize();
    }

    // Refresh current fragment after logging in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
            myRefresh();
    }

    protected void initialize(){

        if(getList().size()==0){

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    // Show progress bar
                    getView().findViewById(R.id.list_progress).
                            setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    // populate the main list
                    getList().addAll(lFetcher.getItems());
                    // get any additional items (such as selfposts)
                    fetchAdditionalItems();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    // Hide progress bar
                    getView().findViewById(R.id.list_progress)
                            .setVisibility(View.GONE);
                    createAdapter();
                }
            }.execute((Void)null);
        }else{
            createAdapter();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        headersInitiated = false;
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
                convertView=getActivity()
                        .getLayoutInflater()
                        .inflate(item_layout_id, null);

                //inflate any header views (such as selfposts)
                if(!headersInitiated) {
                    getAdditionalViews();
                    headersInitiated = true;
                }

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
    }

    abstract void fetchAdditionalItems();
    abstract void getAdditionalViews();
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

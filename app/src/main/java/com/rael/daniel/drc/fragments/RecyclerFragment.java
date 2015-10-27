package com.rael.daniel.drc.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.rael.daniel.drc.activities.LoginActivity;
import com.rael.daniel.drc.R;
import com.rael.daniel.drc.misc.RedditRecyclerAdapter;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 12/10/2015.
 */
public abstract class RecyclerFragment<T> extends Fragment {
    protected List<T> list;
    RecyclerView rView;
    RedditRecyclerAdapter<T> adapter;
    ListFetcher<T> lFetcher;
    int layout_id, rlist_id, item_layout_id;

    public RecyclerFragment() {
        list = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    public static Fragment newInstance(){
        return null; //implement later
    }

    public void myRefresh() {
        list.clear();
        new RedditFetchTask().execute((Void)null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v=inflater.inflate(layout_id
                , container
                , false);
        rView =(RecyclerView)v.findViewById(rlist_id);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
        new RedditFetchTask().execute((Void) null);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /*inflater.inflate(R.menu.search_action_bar, menu);
        if(new RedditLogin(getActivity().getApplicationContext())
                .isLoggedIn())
            menu.findItem(R.id.login).setVisible(false);
        else menu.findItem(R.id.logout).setVisible(false);

        MenuItem item = menu.findItem(R.id.sortSpinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_types, R.layout.sort_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.sort_spinner_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(i, 1);
                return true;
            case R.id.logout:
                SharedPreferences.Editor edit = getActivity()
                        .getApplicationContext()
                        .getSharedPreferences("com.rael.daniel.drc.SPREFS",
                                Context.MODE_PRIVATE).edit();
                edit.remove("RedditCookie").commit();
                myRefresh();
                return true;
            case R.id.refresh_button:
                myRefresh();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class RedditFetchTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            list.addAll(getFetcher().getItems());
            return list != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) { //create and bind adapter
                createAndBindAdapter();
            } else {
                Toast.makeText(getContext(),
                        "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected abstract ListFetcher<T> getFetcher();
    protected abstract void createAndBindAdapter();
}

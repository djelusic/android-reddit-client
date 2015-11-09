package com.rael.daniel.drc.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.adapters.RedditRecyclerAdapter;
import com.rael.daniel.drc.reddit_fetchers.ListFetcher;
import com.rael.daniel.drc.util.LinearLayoutManagerWithSmoothScroller;
import com.rael.daniel.drc.util.ScrollAwareFABBehavior;

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
    int fab_icon;
    boolean initialized = false;
    boolean loadMoreOnScroll = true;
    boolean fabVisibility = false;
    boolean subFabVisibility = false;
    int numSubFabsVisible = 0;
    IFragmentCallback fragmentCallback;

    public RecyclerFragment() {
        this.layout_id = R.layout.rlist_layout;
        this.rlist_id = R.id.rlist;
        this.fab_icon = R.drawable.ic_create_white_24dp;
        contentView = null;
        list = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    protected void FABOnClick() {
        //default behavior, main FAB just opens a submenu
        if(numSubFabsVisible > 0 && !subFabVisibility) {
            setSubFABsVisible(numSubFabsVisible);
            subFabVisibility = true;
        }
        else hideSubFABs();
    }
    private void setFABOnClickListener() {
        if(fragmentCallback != null) {
            fragmentCallback.getFAB().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FABOnClick();
                }
            });
        }
    }
    protected void setSubFABOnClickListener(int subFabId, View.OnClickListener listener) {
        if(fragmentCallback != null && listener != null) {
            fragmentCallback.getSubFAB(subFabId).setOnClickListener(listener);
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        contentView=inflater.inflate(layout_id
                , container
                , false);
        rView =(RecyclerView)contentView.findViewById(rlist_id);
        rView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(getContext()));
        initialize(false);
        initialized = true;
        return contentView;
    }

    protected void setFABVisibility(FloatingActionButton fab, boolean visibility) {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        params.setBehavior(new ScrollAwareFABBehavior(!visibility));
        fab.setLayoutParams(params);
        if(visibility) fab.show();
        else fab.hide();
    }

    protected void setSubFABsVisible(int numSubFabsVisible) {
        for(int i = 0; i < 3; i++) {
            if(i < numSubFabsVisible) {
                setFABVisibility(fragmentCallback.getSubFAB(2 - i), true);
            }
            else setFABVisibility(fragmentCallback.getSubFAB(2 - i), false);
        }
    }

    protected void hideSubFABs() {
        for(int i = 0; i < 3; i++) {
            setFABVisibility(fragmentCallback.getSubFAB(i), false);
        }
        subFabVisibility = false;
    }

    protected void setFABIcon(FloatingActionButton fab, int drawable) {
        Drawable mDrawable = getContext().getDrawable(drawable);
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.drawableColor, typedValue, true);
        if(mDrawable != null) mDrawable.mutate().setTint(typedValue.data);
        fab.setImageDrawable(mDrawable);
    }

    protected void setSubFABIcon(int position, int drawable) {
        if(fragmentCallback != null) {
            fragmentCallback.getSubFAB(position).setImageDrawable(
                    getContext().getDrawable(drawable));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof IFragmentCallback) {
            fragmentCallback = (IFragmentCallback) context;
            setFABVisibility(fragmentCallback.getFAB(), fabVisibility);
            setFABIcon(fragmentCallback.getFAB(), fab_icon);
            setFABOnClickListener();
            hideSubFABs();
            subFabVisibility = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_action_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        if(fragmentCallback.isStateChanged()) {
            myRefresh();
            fragmentCallback.setStateChanged(false);
        }
        if(getActivity() instanceof IFragmentCallback) {
            fragmentCallback = (IFragmentCallback) getActivity();
            setFABVisibility(fragmentCallback.getFAB(), fabVisibility);
            setFABIcon(fragmentCallback.getFAB(), fab_icon);
            setFABOnClickListener();
            hideSubFABs();
            subFabVisibility = false;
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
                        contentView.findViewById(R.id.errors)
                                .setVisibility(View.GONE);
                        list.add(0, null);
                        createAndBindAdapter();
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
                    list.remove(0);
                    adapter.notifyItemRemoved(0);
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
                    }
                    if(isUpdate)
                        adapter.notifyDataSetChanged();
                    else {
                        setLoadMoreListener();
                        adapter.notifyDataSetChanged();
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

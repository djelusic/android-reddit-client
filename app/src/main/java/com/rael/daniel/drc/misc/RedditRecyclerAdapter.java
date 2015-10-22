package com.rael.daniel.drc.misc;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Adapter for recycler fragment
 */
public abstract class RedditRecyclerAdapter<T> extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<T> list;
    Context applicationContext;
    int item_layout_id;


    public RedditRecyclerAdapter(Context applicationContext,
                                 List<T> list, int item_layout_id) {
        this.applicationContext = applicationContext;
        this.setList(list);
        this.item_layout_id = item_layout_id;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(item_layout_id, null);
        return getHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindItem(holder, position);
    }

    @Override
    public int getItemCount() {
        return (null != getList() ? getList().size() : 0);
    }

    protected abstract RecyclerView.ViewHolder getHolder(View view);
    protected abstract void bindItem(RecyclerView.ViewHolder holder, int position);

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}

package com.rael.daniel.drc.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.rael.daniel.drc.R;

import java.util.List;

/**
 * Adapter for recycler fragment
 * Credit to Vilen Melkumyan @StackOverflow for the endless scrolling implementation
 */
public abstract class RedditRecyclerAdapter<T> extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<T> list;
    Context applicationContext;
    int item_layout_id;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading = false;
    private OnLoadMoreListener onLoadMoreListener;

    //Template ViewHolder for list items with an OnClickListener
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected IViewHolderClick listener;
        public ViewHolder(View itemView, IViewHolderClick listener) {
            super(itemView);
            this.listener = listener;
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
        public interface IViewHolderClick {
            void onClick(View v, int position);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        }
    }

    public RedditRecyclerAdapter(Context applicationContext,
                                 List<T> list, int item_layout_id, RecyclerView recyclerView) {
        this.applicationContext = applicationContext;
        this.setList(list);
        this.item_layout_id = item_layout_id;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position)!=null? VIEW_ITEM: VIEW_PROG;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == VIEW_ITEM) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(item_layout_id, null);
            return getHolder(view);
        }
        else {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.progress_item, viewGroup, false);
            return new ProgressViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolder) {
            bindItem(holder, position);
        }
    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    @Override
    public int getItemCount() {
        return (null != getList() ? getList().size(): 0);
    }

    protected abstract RedditRecyclerAdapter.ViewHolder getHolder(View view);
    protected abstract void bindItem(RecyclerView.ViewHolder holder, int position);

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}

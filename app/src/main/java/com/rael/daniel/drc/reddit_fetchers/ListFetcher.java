package com.rael.daniel.drc.reddit_fetchers;

import android.content.Context;

import java.util.List;


public abstract class ListFetcher<T> {

    Context applicationContext;
    String after = null;

    ListFetcher(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public boolean hasMoreItems() { return !after.equals("null"); }

    public abstract List<T> getItems();
    public abstract List<T> getMoreItems();
}

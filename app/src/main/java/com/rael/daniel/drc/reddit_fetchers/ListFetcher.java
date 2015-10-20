package com.rael.daniel.drc.reddit_fetchers;

import android.content.Context;

import java.util.List;


public abstract class ListFetcher<T> {

    Context applicationContext;

    ListFetcher(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public abstract List<T> getItems();
}

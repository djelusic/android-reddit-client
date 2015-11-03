package com.rael.daniel.drc.reddit_fetchers;

import android.content.Context;
import android.widget.Toast;

import com.rael.daniel.drc.reddit_api.RedditConnectionManager;

import java.util.List;


public abstract class ListFetcher<T> {

    Context applicationContext;
    String after = null;
    String rawData;
    String url;

    ListFetcher(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public String showErrors() {
        if(!RedditConnectionManager.isConnected(applicationContext)) {
            return "No internet connection detected";
        }
        else if(rawData.startsWith("HTTP error") || rawData.startsWith("READ FAILED"))
            /*Toast.makeText(applicationContext, rawData, Toast.LENGTH_LONG).show();*/
            return rawData;
        else return null;
    }

    public boolean hasMoreItems() { return !after.equals("null"); }

    public abstract List<T> getItems();
    public abstract List<T> getMoreItems();
}

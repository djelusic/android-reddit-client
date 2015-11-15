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

    public String getErrors() {
        if (!RedditConnectionManager.isConnected(applicationContext)) {
            return "No internet connection detected";
        }
        if (rawData.startsWith("HTTP error") || rawData.startsWith("READ FAILED")) {
            /*Toast.makeText(applicationContext, rawData, Toast.LENGTH_LONG).show();*/
            return rawData;
        }
        if (rawData.contains("USER_REQUIRED")) { //TODO: note, potentially unsafe
            return "Login has expired. Please log out and log back in.";
        }
        return null;
    }

    public boolean hasMoreItems() {
        return !after.equals("null");
    }

    public abstract List<T> getItems();

    public abstract List<T> getMoreItems();
}

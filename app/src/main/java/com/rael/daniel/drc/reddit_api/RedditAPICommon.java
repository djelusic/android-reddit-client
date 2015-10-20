package com.rael.daniel.drc.reddit_api;

import android.content.Context;

/**
 * Created by Daniel on 10/10/2015.
 */
public class RedditAPICommon extends RedditAPIFunction {

    public RedditAPICommon(Context applicationContext) {
        super(applicationContext);
    }

    public void vote(String id, int dir) {
        RedditAPITask tsk = new RedditAPITask("vote",
                "id=" + id + "&dir=" + String.valueOf(dir));
        tsk.execute();
    }
}

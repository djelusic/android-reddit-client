package com.rael.daniel.drc.reddit_api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.util.Consts;

/**
 * Common API functions (vote, ...)
 */
public class RedditAPICommon {

    private Context applicationContext;

    public RedditAPICommon(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    private class VoteTask extends AsyncTask<Void, Void, Boolean> {
        private final String body;

        VoteTask(String body) {
            this.body = body;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            RedditLogin rl = new RedditLogin(applicationContext);
            if(rl.isLoggedIn()) {
                RedditConnectionManager rcm = new RedditConnectionManager(applicationContext);
                if(rcm.writeContents(rcm.getConnection(Consts.REDDIT_URL + "/api/vote"),
                        body))
                    return true;
                else return false;
            }
            else return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result)
                Log.d("APIFunction", "success");
        }
    }

    public void vote(String id, int dir) {
        VoteTask tsk = new VoteTask("id=" + id + "&dir="
                + String.valueOf(dir));
        tsk.execute();
    }
}

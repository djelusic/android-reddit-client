package com.rael.daniel.drc.reddit_api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rael.daniel.drc.reddit_login.RedditLogin;

/**
 * Created by Daniel on 10/10/2015.
 */
public class RedditAPIFunction {
    protected final String REDDIT_API_URL =
            "http://www.reddit.com/api/";
    Context applicationContext;

    RedditAPIFunction() {}

    RedditAPIFunction(Context applicationContext)  {
        this.applicationContext = applicationContext;
    }

    protected class RedditAPITask extends AsyncTask<Void, Void, Boolean> {

        private final String taskType;
        private final String body;

        RedditAPITask(String taskType, String body) {
            this.taskType = taskType;
            this.body = body;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            RedditLogin rl = new RedditLogin(applicationContext);
            if(rl.isLoggedIn()) {
                RedditConnectionManager rcm = new RedditConnectionManager(applicationContext);
                if(rcm.writeContents(rcm.getConnection(REDDIT_API_URL + taskType),
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
}

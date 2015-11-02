package com.rael.daniel.drc.reddit_api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.activities.SubmitActivity;
import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.util.Consts;
import com.rael.daniel.drc.util.DownloadImageTask;

import org.json.JSONObject;

/**
 * Common API functions (vote, submit, reply ...)
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
                if(rcm.postRequest(rcm.getConnection(Consts.REDDIT_URL + "/api/vote"),
                        body) != null)
                    return true;
                else return false;
            }
            else return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result)
                Log.d("Vote", "success");
        }
    }

    private class SubmitTask extends AsyncTask<Void, Void, String> {
        private final String body;
        private final SubmitActivity sa;

        SubmitTask(String body, SubmitActivity sa) {
            this.body = body;
            this.sa = sa;
        }

        @Override
        protected String doInBackground(Void... params) {
            RedditLogin rl = new RedditLogin(applicationContext);
            if(rl.isLoggedIn()) {
                RedditConnectionManager rcm = new RedditConnectionManager(applicationContext);
                String response = rcm.postRequest(rcm.getConnection(Consts.REDDIT_URL + "/api/submit"),
                        body);
                if(response != null)
                    return response;
                else return null;
            }
            else return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result == null)
                Log.d("Submit", "Submit failed");
            else if(result.contains("BAD_CAPTCHA")) {
                Log.d("Submit", "Need captcha");
                try {
                    JSONObject response = new JSONObject(result)
                            .getJSONObject("json");
                    String captchaId = response.getString("captcha");
                    ImageView captchaImg = (ImageView)sa
                            .findViewById(R.id.captcha_img);
                    sa.findViewById(R.id.captcha_img).setTag(captchaId);
                    new DownloadImageTask(captchaImg)
                            .execute(Consts.REDDIT_URL + "/captcha/" + captchaId + ".png");
                    captchaImg.setVisibility(View.VISIBLE);
                    sa.findViewById(R.id.captcha_input).setVisibility(View.VISIBLE);
                }
                catch(Exception e) { e.printStackTrace(); }
            }
            else {
                Log.d("Submit", "success");
                sa.finish();
            }
        }
    }

    private class ReplyTask extends AsyncTask<Void, Void, Boolean> {
        String body;

        public ReplyTask(String body) {
            this.body = body;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            RedditLogin rl = new RedditLogin(applicationContext);
            if(rl.isLoggedIn()) {
                RedditConnectionManager rcm = new RedditConnectionManager(applicationContext);
                if(rcm.postRequest(rcm.getConnection(Consts.REDDIT_URL + "/api/comment"),
                        body) != null)
                    return true;
                else return false;
            }
            else return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }

    public void vote(String id, int dir) {
        VoteTask tsk = new VoteTask("id=" + id + "&dir="
                + String.valueOf(dir));
        tsk.execute();
    }

    public void submit(String sr, String kind, SubmitActivity sa) {
        View captchaImg = sa.findViewById(R.id.captcha_img);
        String title = ((EditText)sa.findViewById(R.id.submit_title))
                .getText().toString();
        String text = ((EditText)sa.findViewById(R.id.submit_text))
                .getText().toString();
        String url = ((EditText)sa.findViewById(R.id.submit_url))
                .getText().toString();
        String captchaHeader = "";
        if(captchaImg.getVisibility() == View.VISIBLE) {
            captchaHeader = "&iden=" + (String)captchaImg.getTag()
                    + "&captcha=" + ((EditText)sa.findViewById(R.id.captcha_input))
                    .getText().toString();
        }
        if(kind.equals("link"))
            new SubmitTask("kind=" + kind + "&sr=" + sr + "&title="
                    + title + "&url=" + url + "&api_type=json" +
                    captchaHeader, sa).execute((Void)null);
        if(kind.equals("self"))
            new SubmitTask("kind=" + kind + "&sr=" + sr + "&title="
                    + title + "&text=" + text + "&api_type=json"
                    + captchaHeader, sa).execute((Void)null);
    }

    public void reply(String parentId, String replyText) {
        ReplyTask tsk = new ReplyTask("thing_id=" + parentId +
                "&text=" + replyText + "&api_type=json");
        tsk.execute((Void)null);
    }
}

package com.rael.daniel.drc.reddit_login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.rael.daniel.drc.reddit_api.RedditConnectionManager;

import org.json.JSONObject;

import java.net.HttpURLConnection;

public class RedditLogin {
    // The login API URL
    private final String REDDIT_LOGIN_URL = "https://ssl.reddit.com/api/login";

    // The Reddit cookie string
// This should be used by other methods after a successful login.
    private String redditCookie = null;

    private Context applicationContext;

    public RedditLogin(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    private String getCookie(String url, String data){
        RedditConnectionManager rcm =
                new RedditConnectionManager(applicationContext);
        HttpURLConnection con=rcm.getConnection(url);
        con.setDoOutput(true);
        if(rcm.writeContents(con, data)) {
            redditCookie = con.getHeaderField("set-cookie");
            return redditCookie;
        }
        else return null;
    }

    private String getModhash() {
        if(isLoggedIn()) {
            RedditConnectionManager rcm =
                    new RedditConnectionManager(applicationContext);
            try {
                JSONObject data = new JSONObject(rcm.readContents(
                        "http://www.reddit.com/api/me.json")).getJSONObject("data");
                String modhash = data.getString("modhash");
                SharedPreferences.Editor edit = applicationContext
                        .getSharedPreferences("com.rael.daniel.drc.SPREFS",
                                Context.MODE_PRIVATE).edit();
                edit.putString("Modhash", modhash).commit();
                return modhash;
            }
            catch(Exception e) { return null;}
        }
        else return null;

    }


    // This method lets you log in to Reddit.
// It fetches the cookie which can be used in subsequent calls
// to the Reddit API.
    public boolean login(String username, String password){

        RedditConnectionManager conn =
                new RedditConnectionManager(applicationContext);

        //Parameters that the API needs
        String data="user="+username+"&passwd="+password;

        String cookie = getCookie(REDDIT_LOGIN_URL, data);

        if(cookie==null)
            return false;

        cookie=cookie.split(";")[0];
        if(cookie.startsWith("reddit_first")){
            // Login failed
            Log.d("Error", "Unable to login.");
            return false;
        }else if(cookie.startsWith("reddit_session")){
            // Login success
            Log.d("Success", cookie);
            redditCookie = cookie;
            SharedPreferences.Editor edit = applicationContext
                    .getSharedPreferences("com.rael.daniel.drc.SPREFS",
                            Context.MODE_PRIVATE).edit();
            edit.putString("RedditCookie", cookie).commit();
            edit.putBoolean("isLoggedIn", true).commit();
            getModhash();
            return true;
        }
        return false;
    }

    public void logout() {
        SharedPreferences.Editor edit = applicationContext
                .getSharedPreferences("com.rael.daniel.drc.SPREFS",
                        Context.MODE_PRIVATE).edit();
        edit.putBoolean("isLoggedIn", false).commit();
    }

    public boolean isLoggedIn() {
        return applicationContext.getSharedPreferences("com.rael.daniel.drc.SPREFS",
                Context.MODE_PRIVATE).getBoolean("isLoggedIn", false);
    }
}

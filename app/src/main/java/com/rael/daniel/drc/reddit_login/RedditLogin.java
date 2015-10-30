package com.rael.daniel.drc.reddit_login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.rael.daniel.drc.reddit_api.RedditConnectionManager;
import com.rael.daniel.drc.util.Consts;

import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
* Implementation of the Reddit login API
* */
public class RedditLogin {

    //Session cookie
    private String redditCookie = null;
    private Context applicationContext;

    public RedditLogin(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    //Attempts to send login data and receive the session cookie
    private String getCookie(String url, String data){
        RedditConnectionManager rcm =
                new RedditConnectionManager(applicationContext);
        HttpURLConnection con=rcm.getConnection(url);
        con.setDoOutput(true);
        if(rcm.postRequest(con, data) != null) {
            redditCookie = con.getHeaderField("set-cookie");
            return redditCookie;
        }
        else return null;
    }

    //Modhash is required for some API calls (eg Vote)
    private String getModhash() {
        if(isLoggedIn()) {
            RedditConnectionManager rcm =
                    new RedditConnectionManager(applicationContext);
            try {
                JSONObject data = new JSONObject(rcm.readContents(
                        Consts.REDDIT_URL + "/api/me.json")).getJSONObject("data");
                String modhash = data.getString("modhash");
                SharedPreferences.Editor edit = applicationContext
                        .getSharedPreferences(Consts.SPREFS_LOGIN,
                                Context.MODE_PRIVATE).edit();
                edit.putString("Modhash", modhash).apply();
                return modhash;
            }
            catch(Exception e) { return null;}
        }
        else return null;

    }

    //Main login method, stores the session cookie and modhash
    //in shared preferences
    public boolean login(String username, String password){

        RedditConnectionManager conn =
                new RedditConnectionManager(applicationContext);

        String data="user="+username+"&passwd="+password;

        String cookie = getCookie(Consts.REDDIT_LOGIN_URL, data);

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
                    .getSharedPreferences(Consts.SPREFS_LOGIN,
                            Context.MODE_PRIVATE).edit();
            edit.putString("RedditCookie", cookie).apply();
            edit.putBoolean("isLoggedIn", true).apply();
            edit.putString("currentUser", username);
            getModhash();
            return true;
        }
        return false;
    }

    //Sets the login flag to false
    public void logout() {
        SharedPreferences.Editor edit = applicationContext
                .getSharedPreferences(Consts.SPREFS_LOGIN,
                        Context.MODE_PRIVATE).edit();
        edit.putBoolean("isLoggedIn", false).apply();
        edit.putString("currentUser", null);
    }

    public boolean isLoggedIn() {
        return applicationContext.getSharedPreferences(Consts.SPREFS_LOGIN,
                Context.MODE_PRIVATE).getBoolean("isLoggedIn", false);
    }

    public String getCurrentUser() {
        return applicationContext.getSharedPreferences(Consts.SPREFS_LOGIN,
                Context.MODE_PRIVATE).getString("currentUser", null);
    }
}

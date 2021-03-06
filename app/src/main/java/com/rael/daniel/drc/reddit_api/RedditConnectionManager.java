package com.rael.daniel.drc.reddit_api;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.rael.daniel.drc.reddit_login.RedditLogin;
import com.rael.daniel.drc.util.Consts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Used to connect to reddit and set appropriate headers for requests.
 */
public class RedditConnectionManager {

    Context applicationContext;

    public RedditConnectionManager(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    //Test connection
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                URL url = new URL("http://www.google.com/");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000); // mTimeout is in seconds
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                Log.i("warning", "Error checking internet connection", e);
                return false;
            }
        }

        return false;

    }

    //Connect to Reddit via HTTP
    public HttpURLConnection getConnection(String url) {
        System.out.println("URL: " + url);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(30000); // Timeout at 30 seconds
            SharedPreferences pref = applicationContext
                    .getSharedPreferences(Consts.SPREFS_LOGIN,
                            Context.MODE_PRIVATE);
            if (new RedditLogin(applicationContext).isLoggedIn()) {
                String cookie = pref.getString("RedditCookie", null);
                conn.setRequestProperty("Cookie", cookie);
                conn.setRequestProperty("X-Modhash",
                        pref.getString("Modhash", null));
            }
            conn.setRequestProperty("User-Agent", Consts.USER_AGENT);
        } catch (MalformedURLException e) {
            Log.e("getConnection()",
                    "Invalid URL: " + e.toString());
        } catch (IOException e) {
            Log.e("getConnection()",
                    "Could not connect: " + e.toString());
        }
        return conn;
    }

    //Read data from URL
    public String readContents(String url) {
        HttpURLConnection conn = getConnection(url);
        if (conn == null) return null;
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d("HTTP error", String.valueOf(conn.getResponseCode()));
                return "HTTP error " + conn.getResponseCode();
            }
            StringBuilder sb = new StringBuilder(8192);
            String tmp;
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            conn.getInputStream()
                    )
            );
            while ((tmp = br.readLine()) != null)
                sb.append(tmp).append("\n");
            br.close();
            return sb.toString();
        } catch (IOException e) {
            Log.d("READ FAILED", e.toString());
            return "READ FAILED: " + e.toString();
        }
    }

    public String postRequest(final HttpURLConnection con, final String data) {
        try {
            con.setRequestMethod("POST");
            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(
                            con.getOutputStream()
                    )
            );
            pw.write(data);
            pw.close();

            try {
                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("HTTP error", String.valueOf(con.getResponseCode()));
                    return "HTTP error " + con.getResponseCode();
                }
                StringBuilder sb = new StringBuilder(8192);
                String tmp;
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                con.getInputStream()
                        )
                );
                while ((tmp = br.readLine()) != null)
                    sb.append(tmp).append("\n");
                br.close();
                Log.d("response", sb.toString());
                return sb.toString();
            } catch (IOException e) {
                Log.d("READ FAILED", e.toString());
                return "READ FAILED: " + e.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

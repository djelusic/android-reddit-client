package imgur;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.rael.daniel.drc.util.Consts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Connects to imgur with the appropriate authorization header.
 */
public class ImgurConnectionManager {
    /*
        * Connect to Imgur via HTTP
        * */
    public static HttpURLConnection getConnection(String url){
        System.out.println("URL: "+url);
        HttpURLConnection conn = null;
        try {
            conn=(HttpURLConnection)new URL(url).openConnection();
            conn.setReadTimeout(30000); // Timeout at 30 seconds
            conn.setRequestProperty("Authorization", "Client-ID "
                    + Consts.IMGUR_CLIENT_ID);
        } catch (MalformedURLException e) {
            Log.e("ImgurConnectionManager",
                    "Invalid URL: " + e.toString());
        } catch (IOException e) {
            Log.e("ImgurConnectionManager",
                    "Could not connect: " + e.toString());
        }
        return conn;
    }

    /*
    * Read data from URL
    * */
    public static String readContents(String url){
        HttpURLConnection conn=getConnection(url);
        if(conn==null) return null;
        try{
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d("HTTP error", String.valueOf(conn.getResponseCode()));
                return "HTTP error " + conn.getResponseCode();
            }
            StringBuilder sb=new StringBuilder(8192);
            String tmp;
            BufferedReader br=new BufferedReader(
                    new InputStreamReader(
                            conn.getInputStream()
                    )
            );
            while((tmp=br.readLine())!=null)
                sb.append(tmp).append("\n");
            br.close();
            return sb.toString();
        }catch(IOException e){
            Log.d("READ FAILED", e.toString());
            return "READ FAILED: " + e.toString();
        }
    }
}

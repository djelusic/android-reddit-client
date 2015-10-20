package com.rael.daniel.drc.util;

import org.json.JSONArray;

/**
 * Created by Daniel on 19/10/2015.
 */
public class JSONArrayConverter {
    public static String[] convert(JSONArray jsonArray){
        String[] stringArray = null;
        int length = jsonArray.length();
        stringArray = new String[length];
        for(int i=0;i<length;i++){
            stringArray[i]= jsonArray.optString(i);
        }
        return stringArray;
    }
}

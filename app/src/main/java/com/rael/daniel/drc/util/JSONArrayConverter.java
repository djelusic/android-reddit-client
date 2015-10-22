package com.rael.daniel.drc.util;

import org.json.JSONArray;

/**
 * Converts JSON arrays to string arrays
 */
public class JSONArrayConverter {
    public static String[] convert(JSONArray jsonArray){
        String[] stringArray;
        int length = jsonArray.length();
        stringArray = new String[length];
        for(int i=0;i<length;i++){
            stringArray[i]= jsonArray.optString(i);
        }
        return stringArray;
    }
}

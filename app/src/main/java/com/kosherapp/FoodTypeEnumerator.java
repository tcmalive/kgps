package com.kosherapp;

import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wunder on 24-Mar-15.
 */

public class FoodTypeEnumerator{

    private String foodTypeValues;
    private static HashMap<String, String> foodTypeMap = null;

    public FoodTypeEnumerator(){
        String methodInfo = "<FoodTypeEnumerator.FoodTypeEnumerator()>";

        String result = "";

        try {
            String address = "http://www.kosherrestaurantsgps.com/foodtypelist_json.php?";

            URL updateURL = new URL(address);
            URLConnection conn = updateURL.openConnection();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 8192);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);

            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            /* Convert the Bytes read to a String. */
            result = new String(baf.toByteArray());
            Log.d(Common.LOG_TAG, String.format("DB Content: %s", result));
        } catch (Exception e) {
            String message = Common.getString(e);
            Common.Log(methodInfo, message);
            return;
        }
        this.foodTypeValues = result;
    }

    public String getFoodTypeText(int foodType) {
        String methodInfo = "<FoodTypeEnumerator.getFoodTypeText(int):String>";
        String foodTypeText = "";
        foodTypeText = FoodTypeMap().get(String.valueOf(foodType));
        // Common.Log(methodInfo, String
        // .format("foodType: %s", String.valueOf(foodType)));
        // Common.Log(methodInfo, String.format("foodTypeText: %s",
        // foodTypeText));

        return foodTypeText;
    }

    private HashMap<String, String> FoodTypeMap() {
        String methodInfo = "<FoodTypeEnumerator.FoodTypeMap():HashMap<String, String>>";
        if (foodTypeMap == null || foodTypeMap.size() == 0) {
            foodTypeMap = new HashMap<String, String>();

            int counter = 0;
            int counterLimit = 5;
            ArrayList<HashMap<String, String>> foodTypeMaps = null;
            while (counter < counterLimit) {
                foodTypeMaps = JSONParser.parseJSON(RetrieveFoodTypeValues());
                if (foodTypeMaps != null) {
                    break;
                }
            }
            if (foodTypeMaps == null) {
                return foodTypeMap;
            }
            for (int i = 0; i < foodTypeMaps.size(); i++) {
                HashMap<String, String> item = foodTypeMaps.get(i);
                String key = item.get("id");
                String value = item.get("type");
                foodTypeMap.put(key, value);
            }
        }
        return foodTypeMap;
    }

    private String RetrieveFoodTypeValues() {
        final String methodInfo = "<FoodTypeEnumerator.RetrieveFoodTypeValues():String>";

        return this.foodTypeValues;
    }

}

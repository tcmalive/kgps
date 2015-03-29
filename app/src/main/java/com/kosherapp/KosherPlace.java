package com.kosherapp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.util.ByteArrayBuffer;

import android.os.AsyncTask;
import android.util.Log;

public class KosherPlace {

    public static final String FOODTYPE_UNDEFINED = "undefined";

    int kosherId;
    String name;
    String phone;
    String address;
    double longitude;
    double latitude;
    String discount;
    String website;
    String type;
    int foodType;
    int loyaltyId;
    private FoodTypeEnumerator foodTypeEnumerator;

    // Location location;

    public KosherPlace(int Id, String name, String phone, String address,
                       double longitude, double latitude, String discount, String website,
                       String type, int foodType, int loyaltyId, FoodTypeEnumerator foodTypeEnumerator) {
        String methodInfo = "<KosherPlace.KosherPlace(int, String, String, String, double, double, String, String, String, int, int, FoodTypeEnumerator)>";
        this.kosherId = Id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.discount = discount;
        this.website = website;
        this.type = type;
        this.foodType = foodType;
        this.loyaltyId = loyaltyId;
        this.foodTypeEnumerator = foodTypeEnumerator;
    }

    public String Type() {
        String methodInfo = "<KosherPlace.Type():String>";
        // Common.Log(methodInfo, String.format("this.type: %s", this.type));
        return this.type;
    }

    public int getKosherId() {
        return this.kosherId;
    }

    public int getLoyaltyId() {
        return this.loyaltyId;
    }

    public Boolean getHasLoyaltyCards() {
        return (!(this.loyaltyId == Integer.MIN_VALUE));
    }

    public String Name() {
        return name;
    }

    public String Phone() {
        return phone;
    }

    public String Address() {
        return address;
    }

    public double Longitude() {
        return longitude;
    }

    public double Latitude() {
        return latitude;
    }

    public double getDistance(double latOrigin, double longOrigin) {
        double distance = Double.MIN_VALUE;

        double dLat1InRad = latOrigin * (Math.PI / 180.0);
        double dLong1InRad = longOrigin * (Math.PI / 180.0);
        double dLat2InRad = this.latitude * (Math.PI / 180.0);
        double dLong2InRad = this.longitude * (Math.PI / 180.0);

        double dLongitude = dLong2InRad - dLong1InRad;
        double dLatitude = dLat2InRad - dLat1InRad;

        // Intermediate result a.
        double a = Math.pow(Math.sin(dLatitude / 2.0), 2.0)
                + Math.cos(dLat1InRad) * Math.cos(dLat2InRad)
                * Math.pow(Math.sin(dLongitude / 2.0), 2.0);

        // Intermediate result c (great circle distance in Radians).
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        // Distance.
        final Double kEarthRadiusMiles = 3956.0;
        final Double kEarthRadiusKms = 6376.5;
        distance = kEarthRadiusMiles * c;

        // Log.d(Common.LOG_TAG, String.format("distance: %s", String
        // .valueOf(distance)));
        return distance;
    }

    public String Discount() {
        return discount;
    }

    public String Website() {
        return website;
    }

    public String DistanceText(double latOrigin, double longOrigin) {
        double distance = this.getDistance(latOrigin, longOrigin);
        return String.valueOf(Common.Round(distance, 2)) + " miles";
    }

    public int FoodType() {
        return this.foodType;
    }

    public String getFoodTypeText() {
        String foodTypeText = "";

        foodTypeText = this.foodTypeEnumerator.getFoodTypeText(FoodType());

        return foodTypeText;
    }
}

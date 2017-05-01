package com.example.xxfin.recommendationsystemtesina.objects;

//import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import java.util.LinkedList;

/**
 * Created by xxfin on 23/04/2017.
 */

public class Place_Info {
    private CharSequence name;
    private String placeId;
    private LatLng latlng;
    private JSONArray placeTypes;
    private double rating;
    private LinkedList ratingList;

    public Place_Info() {

    }

    public Place_Info(CharSequence name, String placeId, LatLng latlng, JSONArray placeTypes, double rating, LinkedList ratingList) {
        this.name = name;
        this.placeId = placeId;
        this.latlng = latlng;
        this.placeTypes = placeTypes;
        this.rating = rating;
        this.ratingList = ratingList;

    }

    public LinkedList getRatingList() {
        return ratingList;
    }

    public void setRatingList(LinkedList ratingList) {
        this.ratingList = ratingList;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public JSONArray getPlaceTypes() {

        return placeTypes;
    }

    public void setPlaceTypes(JSONArray placeTypes) {
        this.placeTypes = placeTypes;
    }

    public LatLng getLatlng() {

        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public String getPlaceId() {

        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public CharSequence getName() {

        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public String toString() {
        return this.getName() + "\n" + this.getPlaceId() + "\n" + this.getRating();
    }
}

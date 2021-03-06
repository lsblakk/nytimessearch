package com.lukasblakk.nytimessearch.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by lukas on 3/13/17.
 */
@Parcel
public class Article {

    String headline;
    String snippet;
    String webUrl;
    String thumbNail;

    public String getSnippet() { return snippet; }

    public void setSnippet(String snippet) { this.snippet = snippet; }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public void setThumbNail(String thumbNail) {
        this.thumbNail = thumbNail;
    }

    public String getHeadline() {
        return headline;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    // empty constructor needed by the Parceler library
    public Article() {

    }

    public Article(JSONObject jsonObject) throws JSONException {

        this.webUrl = jsonObject.getString("web_url");
        this.headline = jsonObject.getJSONObject("headline").getString("main");
        this.snippet = jsonObject.getString("snippet");

        JSONArray multimedia = jsonObject.getJSONArray("multimedia");
        if (multimedia.length() > 0) {
            int random = new Random().nextInt(multimedia.length());
            JSONObject multimediaJson = multimedia.getJSONObject(random);
            this.thumbNail = "http://www.nytimes.com/" + multimediaJson.getString("url");
        } else {
            this.thumbNail = "";
        }
    }

    public static ArrayList<Article> fromJSONArray(JSONArray array) {
        ArrayList<Article> results = new ArrayList<>();

        for (int x = 0; x < array.length(); x++) {
            try{
                results.add(new Article(array.getJSONObject(x)));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return results;
    }
}

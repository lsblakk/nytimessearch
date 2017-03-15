package com.lukasblakk.nytimessearch.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by lukas on 3/13/17.
 */

public class Article implements Serializable {

    String headline;
    String webUrl;
    String thumbNail;

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


    public Article(JSONObject jsonObject) throws JSONException {

        this.webUrl = jsonObject.getString("web_url");
        this.headline = jsonObject.getJSONObject("headline").getString("main");

        JSONArray multimedia = jsonObject.getJSONArray("multimedia");
        if (multimedia.length() > 0) {
            JSONObject multimediaJson = multimedia.getJSONObject(0);
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

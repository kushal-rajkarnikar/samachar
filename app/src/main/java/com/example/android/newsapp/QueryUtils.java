package com.example.android.newsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.newsapp.NewsActivity.LOG_TAG;

public class QueryUtils {

    private QueryUtils() {
    }

    private static List<Article> extractResponseFromJson(String articleJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(articleJSON)) {
            return null;
        }

        List<Article> articles = new ArrayList<>();

        try {

            JSONObject baseJsonResponse = new JSONObject(articleJSON);
            JSONObject response = baseJsonResponse.getJSONObject("response");
            JSONArray resultsArray = response.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {

                JSONObject currentArticle = resultsArray.getJSONObject(i);
                String sectionName = "No Category";
                if (currentArticle.has("sectionName")) {
                    sectionName = currentArticle.getString("sectionName");
                }

                String webUrl = "https://theguardian.com";
                if (currentArticle.has("webUrl")) {
                    webUrl = currentArticle.getString("webUrl");
                }

                String webPublicationDate = "No Date";
                if (currentArticle.has("webPublicationDate")) {
                    webPublicationDate = currentArticle.getString("webPublicationDate");
                }

                JSONObject fields = currentArticle.getJSONObject("fields");

                // Extract the value for the key called "headline"
                String headline = "No Title";
                if (fields.has("headline")) {
                    headline = fields.getString("headline");
                }


                String trailText = "No Subtitle";
                if (fields.has("trailText")) {
                    trailText = fields.getString("trailText");
                }

                String byline = "Author Unknown";
                if (fields.has("byline")) {
                    byline = fields.getString("byline");
                }

                String thumbnail = "";
                if (fields.has("thumbnail")) {
                    thumbnail = fields.getString("thumbnail");
                }


                Article article = new Article(sectionName, webUrl, webPublicationDate, headline, trailText, byline, thumbnail);

                // Add the new {@link Article} to the list of books.
                articles.add(article);
            }

        } catch (JSONException e) {

            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        }

        // Return the list of articles
        return articles;
    }



    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the article JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static List<Article> fetchArticleData(String requestUrl) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i(LOG_TAG, "Fetch article data");


        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }


        List<Article> articles = extractResponseFromJson(jsonResponse);

        return articles;
    }
}

package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>> {

    public static final String LOG_TAG = NewsActivity.class.getName();

    private static final String GUARDIAN_API_URL = "https://content.guardianapis.com/search?";
    private static final String API_TEST_KEY = "test";

    private ArticleAdapter mAdapter;

    private static final int ARTICLE_LOADER_ID = 11;

    private TextView mEmptyTextView;
    private ProgressBar mProgressView;
    private ArrayList<Article> articleArrayList = new ArrayList<>();
    LoaderManager loaderManager;
    ConnectivityManager cm;
    NetworkInfo activeNetwork;
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Find a reference to the {@link ListView} in the layout
        ListView articleListView = (ListView) findViewById(R.id.list);

        // Create a new adapter that takes an empty list of articles as input
        mAdapter = new ArticleAdapter(this, articleArrayList);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        articleListView.setAdapter(mAdapter);

        // Set a custom message when there are no list items
        mEmptyTextView = (TextView) findViewById(R.id.empty_view_text);
        View emptyLayoutView = findViewById(R.id.empty_layout_view);
        articleListView.setEmptyView(emptyLayoutView);

        mProgressView = (ProgressBar) findViewById(R.id.progress);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website where a user can view the article.
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current article that was clicked on
                Article currentArticle = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri articleUri = Uri.parse(currentArticle.getUrl());

                // Create a new intent to view the article URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, articleUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        if (isConnected()) {

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
            Log.i(LOG_TAG, "Loader on init");
        } else {
            mProgressView.setVisibility(View.GONE);
            mEmptyTextView.setText(R.string.no_internet);
        }

        // Reload the API if the user refreshes the activity
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                            refreshView();
                    }
                }
        );
    }

    private boolean isConnected(){
        // Get a reference to the LoaderManager, in order to interact with loaders.
        loaderManager = getLoaderManager();
        cm  = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void refreshView(){
        // Restart the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.restartLoader(ARTICLE_LOADER_ID, null, NewsActivity.this);
        Log.i(LOG_TAG, "Loader on refresh");
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        // Create a new loader for the given URL
        Log.i(LOG_TAG, "Loader on create");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        String noOfArticles = sharedPrefs.getString(
                getString(R.string.settings_page_size_key),
                getString(R.string.settings_page_size_default));

        Uri baseUri = Uri.parse(GUARDIAN_API_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("q", "artanddesign");
        uriBuilder.appendQueryParameter("format", "json");
        uriBuilder.appendQueryParameter("show-fields", "all");
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("page-size", noOfArticles);
        uriBuilder.appendQueryParameter("api-key", API_TEST_KEY);
        Log.v(LOG_TAG, uriBuilder.toString());

        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {

        // Finish refreshing
        mSwipeRefresh.setRefreshing(false);
        // Clear the adapter of previous article data
        mAdapter.clear();

        if(!isConnected()) {
            // Set empty state text to display "No connection."
            mProgressView.setVisibility(View.GONE);
            mEmptyTextView.setText(R.string.no_internet);
        } else {
            // If there is a valid list of {@link Articles}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (articles != null && !articles.isEmpty()) {
                mAdapter.addAll(articles);
            } else {
                // Set empty state text to display "No articles found."
                mProgressView.setVisibility(View.GONE);
                mEmptyTextView.setText(R.string.no_articles);
            }
        }
        Log.i(LOG_TAG, "Loader on finished");

    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        // Loader reset, so we can clear out our existing data.
        Log.i(LOG_TAG, "Loader on reset");
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Check if user triggered a refresh:
            case R.id.menu_refresh:
                Log.i(LOG_TAG, "Refresh menu item selected");
                // Signal SwipeRefreshLayout to start the progress indicator
                mSwipeRefresh.setRefreshing(true);
                refreshView();
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, CategoriesActivity.class);
                startActivity(settingsIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.lukasblakk.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.lukasblakk.nytimessearch.R;
import com.lukasblakk.nytimessearch.adapters.ArticleArrayAdapter;
import com.lukasblakk.nytimessearch.fragments.SettingsDialogFragment;
import com.lukasblakk.nytimessearch.listeners.EndlessRecyclerViewScrollListener;
import com.lukasblakk.nytimessearch.listeners.ItemClickSupport;
import com.lukasblakk.nytimessearch.models.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity implements SettingsDialogFragment.SettingsDialogListener {

    private EndlessRecyclerViewScrollListener scrollListener;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;
    RecyclerView rvArticles;
    String stQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();

        rvArticles = (RecyclerView) findViewById(R.id.rvArticles);
        adapter = new ArticleArrayAdapter(this, articles);
        rvArticles.setAdapter(adapter);
        // First param is number of columns and second param is orientation i.e Vertical or Horizontal
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // Attach the layout manager to the recycler view
        rvArticles.setLayoutManager(gridLayoutManager);

        // hook up click for RecyclerView
        ItemClickSupport.addTo(rvArticles).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // create an intent to display the article
                        Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                        // get the article to display
                        Article article = articles.get(position);
                        // pass in that article to the intent
                        i.putExtra("article", article);
                        // launch the activity
                        startActivity(i);
                    }
                }
        );

        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvArticles.addOnScrollListener(scrollListener);
    }

    private void showSettingsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SettingsDialogFragment settingsDialogFragment = SettingsDialogFragment.newInstance();
        settingsDialogFragment.show(fm, "fragement_settings");
    }

    // Append the next page of data into the adapter
    private void loadNextDataFromApi(final int offset) {
        Log.d("DEBUG page: ", Integer.toString(offset));
        fetchArticles(offset);
    }

    public void setupViews() {
        articles = new ArrayList<>();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                stQuery = query;
                // Clear the old data
                articles.clear();
                // Notify the adapter of the update
                adapter.notifyDataSetChanged();
                // Reset endless scroll listener as we'll be performing a new search
                scrollListener.resetState();

                // call loadNextDataFromAPI here for initial search results
                loadNextDataFromApi(0);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                // do the filter here
                showSettingsDialog();
                return true;
            case R.id.action_search:
                final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                // Expand the search view and request focus
                item.expandActionView();
                searchView.requestFocus();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    public void fetchArticles(final int offset) {


        if (isNetworkAvailable() && isOnline()) {
            // should be a singleton
            OkHttpClient client = new OkHttpClient();

            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.nytimes.com/svc/search/v2/articlesearch.json").newBuilder();
            urlBuilder.addQueryParameter("api-key", "3456a86ca8b544179487d82c38862881");
            urlBuilder.addQueryParameter("page", Integer.toString(offset));
            urlBuilder.addQueryParameter("q", stQuery);
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            // Get a handler that can be used to post to the main thread
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    // Read data on the worker thread
                    final String responseData = response.body().string();

                    // Run view-related code back on the main thread
                    SearchActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONArray articleJsonResults = null;

                            try {
                                int curSize = articles.size();
                                JSONObject json = new JSONObject(responseData);
                                articleJsonResults = json.getJSONObject("response").getJSONArray("docs");
                                articles.addAll(Article.fromJSONArray(articleJsonResults));
                                // Create adapter passing in the sample user data
                                // New adapter if page is 0 (new search) otherwise add to existing
                                if (offset == 0) {
                                    adapter = new ArticleArrayAdapter(getApplicationContext(), articles);
                                    rvArticles.setAdapter(adapter);
                                } else {
                                    adapter.notifyItemRangeInserted(curSize, articles.size() - 1);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            });
        } else {
            // put some kind of message for internet not available?
        }
    }

    @Override
    public void onFinishSettingsDialog(String datePicked, String sortOrder, String topics) {
        Toast.makeText(this, "Settings are - Date: " + datePicked + " Sort Order: " + sortOrder + " Topics: " + topics , Toast.LENGTH_LONG).show();
    }

}

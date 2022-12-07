package com.example.RandomMemes;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONException;

public class Second_Activity extends AppCompatActivity {
    private String urlName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        final String API_URL = getString(R.string.API_URL);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
                actionBar.hide();

        ProgressBar progressBar = findViewById(R.id.progressbarID);

        SearchView searchView = findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadMeme((API_URL + query).trim(), progressBar);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Call to initiate network call
        loadMeme("", progressBar);

        Button Next = findViewById(R.id.NextbuttonID);
        Button share = findViewById(R.id.SharebuttonID);

        Next.setOnClickListener(v -> {
            String url = searchView.getQuery().toString();

            loadMeme((API_URL + url).trim(), progressBar);
        });

        share.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_SEND).setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, urlName);
            Intent chooser = Intent.createChooser(intent, "Share this using");

            startActivity(chooser);
        });

    }

    void loadMeme(String url, ProgressBar progressBar) {
        if(url.isEmpty())
            url = getString(R.string.API_URL);

        progressBar.setVisibility(View.VISIBLE);

        // Network Call function
        requestMemeFromAPI(url, progressBar);
    }


    void requestMemeFromAPI(String url, ProgressBar progressBar) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        urlName = response.getString("url");

                        ImageView imageView = findViewById(R.id.imageViewID);

                        Glide.with(this).load(urlName).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e,
                                                        Object model, Target<Drawable> target, boolean isFirstResource) {

                                Toast.makeText(Second_Activity.this, "Something went wrong", Toast.LENGTH_LONG).show();

                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                           DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        }).into(imageView);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(Second_Activity.this, "No matches found for the given category", Toast.LENGTH_LONG).show());

        MySingleton.getInstance(Second_Activity.this).addToRequestQueue(jsonObjectRequest);
    }

}

class MySingleton {
    private static MySingleton instance;
    //    private final ImageLoader imageLoader;
    private final Context ctx;
    private RequestQueue requestQueue;

    private MySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new MySingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
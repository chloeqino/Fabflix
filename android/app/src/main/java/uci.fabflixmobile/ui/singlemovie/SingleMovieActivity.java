package uci.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uci.fabflixmobile.R;
import uci.fabflixmobile.data.NetworkManager;
import uci.fabflixmobile.data.model.Movie;

public class SingleMovieActivity extends AppCompatActivity {
    private final String host = "ec2-54-203-218-206.us-west-2.compute.amazonaws.com";
    private final String port = "8443";
    private final String domain = "cs122b-fall22-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private JSONObject movie = null;
    private ListView genreView;
    private ListView starView;
    private TextView title;
    private TextView director;
    private TextView year;
    private TextView rating;
    SingleMovieViewAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlemovie);
        // TODO: this should be retrieved from the backend server
        fetchMovieInfo();

    }

    protected void fetchMovieInfo(){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id = extras.getString("id");
            final StringRequest loginRequest = new StringRequest(
                    Request.Method.GET,
                    baseURL + "/api/single-movie?id=" + id,
                    response -> {
                        // TODO: should parse the json response to redirect to appropriate functions
                        //  upon different response value.
                        try {
                            JSONObject movie = new JSONObject(response);

                            Log.d("Movie", String.valueOf(movie));

                            title = findViewById(R.id.title);
                            director = findViewById(R.id.director);
                            year = findViewById(R.id.year);
                            rating = findViewById(R.id.rating);
                            title.setText(movie.getString("title"));
                            director.setText("Director: " + movie.getString("director"));
                            year.setText(movie.getString("year"));
                            rating.setText("Rating: " + movie.getString("rating"));


                            adapter = new SingleMovieViewAdapter(this, movie.getJSONArray("genres"), "genres");
                            adapter.notifyDataSetChanged();
                            genreView = findViewById(R.id.genre_list);
                            genreView.setAdapter(adapter);

                            adapter = new SingleMovieViewAdapter(this, movie.getJSONArray("stars"), "stars");
                            adapter.notifyDataSetChanged();
                            starView = findViewById(R.id.star_list);
                            starView.setAdapter(adapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Complete and destroy login activity once successful

                    },
                    error -> {
                        // error
                        Log.d("login.error", error.toString());
                    }) {
            };
            queue.add(loginRequest);
        }
        // important: queue.add is where the login request is actually sent
    }
}
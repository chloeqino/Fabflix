package uci.fabflixmobile.ui.main;

import uci.fabflixmobile.R;
import uci.fabflixmobile.data.NetworkManager;
import uci.fabflixmobile.data.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uci.fabflixmobile.databinding.ActivityLoginBinding;
import uci.fabflixmobile.ui.login.LoginActivity;
import uci.fabflixmobile.ui.movielist.MovieListActivity;
import uci.fabflixmobile.ui.movielist.MovieListViewAdapter;

public class MainActivity extends AppCompatActivity {

    private EditText search;
    private JSONArray movies = null;

    private final String host = "ec2-54-189-58-220.us-west-2.compute.amazonaws.com";
    private final String port = "8443";
    private final String domain = "cs122b-fall22-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        search = findViewById(R.id.search);
        final Button searchButton = findViewById(R.id.execute);

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> movie_search());
    }

    public void movie_search(){
        Intent MovieListPage = new Intent(MainActivity.this, MovieListActivity.class);
        System.out.println(search.getText().toString());
        MovieListPage.putExtra("query", search.getText().toString());
        startActivity(MovieListPage);
    }
}

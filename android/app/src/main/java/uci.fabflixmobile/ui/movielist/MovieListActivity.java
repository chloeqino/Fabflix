package uci.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
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
import uci.fabflixmobile.ui.main.MainActivity;
import uci.fabflixmobile.ui.singlemovie.SingleMovieActivity;

public class MovieListActivity extends AppCompatActivity {
    private final String host = "ec2-54-203-218-206.us-west-2.compute.amazonaws.com";
    private final String port = "8443";
    private final String domain = "cs122b-fall22-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private JSONArray movies = null;
    private ListView listView = null;
    MovieListViewAdapter adapter = null;
    private int page = 1;
    private boolean hasNext = true;
    private SearchView searchView = null;
    public String query = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView =
                    (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            MovieListActivity current = this;

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d("query", (String) searchView.getQuery().toString());
                    current.query = searchView.getQuery().toString();
                    current.fetchTopMovies(1);

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);

                    return false;
                }

            });


        return true;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            query = extras.getString("query");}
        // TODO: this should be retrieved from the backend server
        fetchTopMovies(page);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final Button nextBtn = findViewById(R.id.next_button);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final Button prevBtn = findViewById(R.id.prev_button);
        nextBtn.setOnClickListener(view->nextPage());
        prevBtn.setOnClickListener(view->prevPage());

    }
    protected void prevPage(){
        if (page>1){
            page--;
            fetchTopMovies(page);
        }
    }
    protected void nextPage(){
        if (hasNext==false){
            return;
        }
        page++;
        fetchTopMovies(page);
    }
    public void fetchTopMovies(Integer page){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

            final StringRequest loginRequest = new StringRequest(
                    Request.Method.GET,
                    baseURL + "/api/autocomplete?normal=true&limit=20&sort1=rating&order1=desc&sort2=title&order2=asc&title=" + query + "&page=" + page,
                    response -> {
                        // TODO: should parse the json response to redirect to appropriate functions
                        //  upon different response value.
                        try {
                            JSONObject responseObj = new JSONObject(response);
                            movies = responseObj.getJSONArray("movies");
                            hasNext = responseObj.getBoolean("hasNext");

                            Log.d("Movies", String.valueOf(responseObj));
                            adapter = new MovieListViewAdapter(this, movies);
                            adapter.notifyDataSetChanged();
                            listView = findViewById(R.id.list);
                            listView.setAdapter(adapter);


                            listView.setOnItemClickListener((parent, view, position, id) -> {


                                try {
                                    JSONObject movie = (JSONObject) movies.get(position);
                                    @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %s", position, movie.get("title"), movie.getString("id"));
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                    Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                                    SingleMoviePage.putExtra("id", movie.getString("id"));
                                    startActivity(SingleMoviePage);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            });


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

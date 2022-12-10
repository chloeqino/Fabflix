package uci.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uci.fabflixmobile.R;

public class MovieListViewAdapter extends ArrayAdapter {
    private final JSONArray movies;

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView rating;
        TextView year;
        TextView director;
        TextView genres;
        TextView stars;
    }

    public MovieListViewAdapter(Context context, JSONArray movies) {
        super(context, R.layout.movielist_row);
        this.movies = movies;

    }
    @Override
    public int getCount(){
        return movies.length();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("Names", String.valueOf(movies));
        // Get the movie item for this position
        JSONObject movie = null;
        try {
            movie = (JSONObject) movies.get(position);
            //System.out.println(movie);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.movielist_row, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.year = convertView.findViewById(R.id.year);
            viewHolder.rating = convertView.findViewById(R.id.rating);
            viewHolder.director = convertView.findViewById(R.id.director);
            viewHolder.genres = convertView.findViewById(R.id.genres);
            viewHolder.stars = convertView.findViewById(R.id.stars);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.title.setText(movie.getString("title"));
        viewHolder.rating.setText("Rating: " + movie.getString("rating"));
        viewHolder.director.setText("Director: " + movie.getString("director"));
        viewHolder.year.setText("Year: " + movie.getString("year"));
        JSONArray genres = new JSONArray(movie.getString("genres"));
        String genre_string = "";
        for (int i = 0; i < genres.length(); i++) {
            JSONObject genre = genres.getJSONObject(i);
            genre_string += genre.getString("name");
            genre_string += ", ";
        }
        //System.out.println(genre_string);
        if (genre_string.length() != 0) {
            genre_string = genre_string.substring(0, genre_string.length() - 2);
        }
        viewHolder.genres.setText("Genres: " + genre_string);
        JSONArray stars = new JSONArray(movie.getString("stars"));
        String star_string = "";
        for (int i = 0; i < stars.length(); i++) {
            JSONObject star = stars.getJSONObject(i);
            star_string += star.getString("name");
            star_string += ", ";
        }
        if (star_string.length() != 0) {
            star_string = star_string.substring(0, star_string.length() - 2);
        }
        //System.out.println(star_string);
        viewHolder.stars.setText("Stars: " + star_string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
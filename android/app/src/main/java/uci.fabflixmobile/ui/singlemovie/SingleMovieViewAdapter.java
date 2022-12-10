package uci.fabflixmobile.ui.singlemovie;

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

public class SingleMovieViewAdapter extends ArrayAdapter {
    private final JSONArray names;
    private final String row_type;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
    }

    public SingleMovieViewAdapter(Context context, JSONArray names, String row_type) {
        super(context, R.layout.genre_row);
        this.names = names;
        this.row_type = row_type;

    }
    @Override
    public int getCount(){
        return names.length();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("Names", String.valueOf(names));

        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.genre_row, parent, false);
            viewHolder.name = convertView.findViewById(R.id.name);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the movie item for this position
        try {
            if (row_type.equals("stars")) {
                JSONObject name = (JSONObject) names.get(position);
                viewHolder.name.setText(name.getString("name"));
            }
            else {
                String name = (String) names.get(position);
                viewHolder.name.setText(name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
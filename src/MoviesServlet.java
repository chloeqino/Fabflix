import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected ResultSet getQueryResult(Connection conn, String query,String id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, id);

        // Perform the query
        ResultSet rs = statement.executeQuery();
        //statement.close();
        return rs;

    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (out; Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "select m.id, m.title, m.year, m.director, r.rating from movies m join ratings r on m.id=r.movieId order by rating desc limit 20";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                String genresquery = "select name,id from genres_in_movies join genres on genreId = id where movieId = ? limit 3;";
                String starsquery = "select starId,name from stars_in_movies join stars on id=starId where movieId = ? limit 3;";

                PreparedStatement genrestatement = conn.prepareStatement(genresquery);
                genrestatement.setString(1,movie_id);
                ResultSet genres_rs = genrestatement.executeQuery();
                JsonArray genres = new JsonArray();
                while (genres_rs.next()){
                    String name = genres_rs.getString("name");
                    genres.add(name);
                }
                jsonObject.add("genres",genres);
                ResultSet stars_rs = getQueryResult(conn,starsquery,movie_id);
                JsonArray starsArray = new JsonArray();
                while (stars_rs.next()){
                    JsonObject starObj = new JsonObject();
                    String star_id = stars_rs.getString("starId");
                    String star_name = stars_rs.getString("name");
                    starObj.addProperty("star_id",star_id);
                    starObj.addProperty("star_name",star_name);
                    starsArray.add(starObj);
                }
                jsonObject.add("stars",starsArray);
                jsonArray.add(jsonObject);
                genres_rs.close();
                genrestatement.close();
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}

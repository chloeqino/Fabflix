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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (out; Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "select title, year, director,rating from movies left join ratings on id=movieId where id=?;";
            String starquery = "select s.name as name, s.id as id from stars as s, stars_in_movies as sm where s.id=sm.starId\n" +
                    "and sm.movieId=? \n" +
                    "order by (select count(*) from stars_in_movies as sm2 where sm2.starId = s.id) desc,s.name; ";
            String genresquery = "select name,id from genres_in_movies join genres on genreId = id where movieId = ? order by name;";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            PreparedStatement starstatement = conn.prepareStatement(starquery);
            PreparedStatement genrestatement = conn.prepareStatement(genresquery);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);
            starstatement.setString(1, id);
            genrestatement.setString(1,id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            ResultSet starsresult = starstatement.executeQuery();
            ResultSet genreresult = genrestatement.executeQuery();

            JsonObject obj = new JsonObject();
            if(rs.next()){

                String title = rs.getString("title");
                String director = rs.getString("director");
                int year = rs.getInt("year");
                float rating = rs.getFloat("rating");


                obj.addProperty("title",title);
                obj.addProperty("director",director);
                obj.addProperty("year",year);
                obj.addProperty("rating",rating);
                obj.addProperty("price",10);
                JsonArray starsArray = new JsonArray();
                while (starsresult.next()) {
                    JsonObject starObj = new JsonObject();
                    String starId = starsresult.getString("id");
                    String starName = starsresult.getString("name");
                    starObj.addProperty("id",starId);
                    starObj.addProperty("name",starName);
                    starsArray.add(starObj);


                }
                starstatement.close();
                JsonArray genresArray = new JsonArray();
                while (genreresult.next()){
                    String name = genreresult.getString("name");
                    genresArray.add(name);
                }
                genrestatement.close();
                obj.add("stars",starsArray);
                obj.add("genres",genresArray);
            }
            // Iterate through each row of rs

            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(obj.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
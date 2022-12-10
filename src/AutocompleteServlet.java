
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// server endpoint URL
@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    private String searchMovie = "SELECT id, title, year FROM movies WHERE MATCH (title) AGAINST ( ? IN BOOLEAN MODE) LIMIT 10;";


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /*
     * populate the Super hero hash map.
     * Key is hero ID. Value is hero name.
     */
    public static HashMap<Integer, String> movieMap = new HashMap<>();
    public AutocompleteServlet() {
        super();
    }

    /*
     *
     * Match the query against superheroes and return a JSON response.
     *
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try (out; Connection conn = dataSource.getConnection()) {
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String normal = request.getParameter("normal");
            String query;
            if (normal.equals("true")){
                query = request.getParameter("title");
            }
            else {
                query = request.getParameter("query");
            }
            System.out.println(query);
            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            String[] queries = query.split(" ");
            for (int i = 0; i < queries.length; i++){
                if(queries.length>1){
                queries[i] = "+" + queries[i] + '*';
                }else{
                    queries[i] = queries[i] + '*';
                }
            }
            String fullQuery = String.join(" ", queries);
            if (normal.equals("true")) {
                Integer limit = toIntWithMin(request.getParameter("limit"),1);
                Integer page = toIntWithMin(request.getParameter("page"),1);
                String sort1 = request.getParameter("sort1");

                String sort2 = request.getParameter("sort2");
                String order1 = request.getParameter("order1");
                String order2 = request.getParameter("order2");
                String normalQuery = String.format("select m.title,m.year,m.director,m.id,r.rating,m.price from movies as m left join ratings as r on m.id=r.movieId where MATCH (title) AGAINST ( ? IN BOOLEAN MODE) order by %s %s, %s %s limit %s",sort1,order1,sort2,order2,limit);
                String offetClause = String.format(" offset %s",(page-1)*limit);
                PreparedStatement statement = conn.prepareStatement(normalQuery + offetClause);
                statement.setString(1, fullQuery);
                System.out.println(statement);
                System.out.println(statement);
                ResultSet resultSet = statement.executeQuery();

                JsonObject responseObj = new JsonObject();
                JsonArray movies = new JsonArray();
                String genreQry="select name,id from genres_in_movies join genres on genreId = id where movieId = ?  order by name limit 3;";
                String starQry = "select s.name as name, s.id as id from stars as s, stars_in_movies as sm where s.id=sm.starId\n" +
                        "and sm.movieId=? \n" +
                        "order by (select count(*) from stars_in_movies as sm2 where sm2.starId = s.id) desc,s.name limit 3; ";
                int count = 0;
                while (resultSet.next()){
                    count++;
                    String r_title = resultSet.getString("title");
                    String r_year = resultSet.getString("year");
                    String r_director = resultSet.getString("director");
                    String r_id = resultSet.getString("id");
                    String r_rating = resultSet.getString("rating");
                    String r_price = resultSet.getString("price");
                    JsonObject movieObj = new JsonObject();
                    movieObj.addProperty("title",r_title);
                    movieObj.addProperty("year",r_year);
                    movieObj.addProperty("director",r_director);
                    movieObj.addProperty("id",r_id);
                    movieObj.addProperty("rating",r_rating);
                    movieObj.addProperty("price",r_price);
                    PreparedStatement genrestatement = conn.prepareStatement(genreQry);
                    genrestatement.setString(1,r_id);
                    ResultSet genre_rs = genrestatement.executeQuery();
                    JsonArray genres = new JsonArray();
                    while(genre_rs.next()){
                        JsonObject genreObj = new JsonObject();
                        String g_id = genre_rs.getString("id");
                        String g_name = genre_rs.getString("name");
                        genreObj.addProperty("id",g_id);
                        genreObj.addProperty("name",g_name);
                        genres.add(genreObj);
                    }
                    PreparedStatement starstatement = conn.prepareStatement(starQry);
                    starstatement.setString(1,r_id);
                    ResultSet star_rs = starstatement.executeQuery();
                    JsonArray stars = new JsonArray();
                    while (star_rs.next()){
                        JsonObject starObj = new JsonObject();
                        String s_id = star_rs.getString("id");
                        String s_name = star_rs.getString("name");
                        starObj.addProperty("id",s_id);;
                        starObj.addProperty("name",s_name);
                        stars.add(starObj);

                    }
                    movieObj.add("genres",genres);
                    movieObj.add("stars",stars);
                    movies.add(movieObj);
                    genrestatement.close();
                    starstatement.close();

                }
                resultSet.close();
                statement.close();
                responseObj.add("movies",movies);
                responseObj.addProperty("hasNext",count>=limit);
                out.write(responseObj.toString());
                response.setStatus(200);
            }
            else {
                PreparedStatement statement = conn.prepareStatement(searchMovie);
                statement.setString(1, fullQuery);
                System.out.println(statement);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String title = rs.getString("title");
                    String year = rs.getString("year");
                    JsonObject movieObj = new JsonObject();
                    jsonArray.add(generateJsonObject(id, title, year));
                }
                rs.close();
                statement.close();


                response.getWriter().write(jsonArray.toString());

                return;
            }
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieID, String title, String year) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title + " (" + year + ") ");

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieID", movieID);
        additionalDataJsonObject.addProperty("title", title);
        additionalDataJsonObject.addProperty("year", year);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }

    private Integer toIntWithMin(String str,Integer min){
        Integer value = min;
        if(!str.isBlank()) {
            value = Integer.parseInt(str);
            value = Math.max(min,value);
        }
        return value;
    }


}
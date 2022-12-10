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
import java.sql.SQLException;
import java.util.ArrayList;
@WebServlet(name="GenresServlet",urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
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
    private Integer toIntWithMin(String str,Integer min){
        Integer value = min;
        if(!str.isBlank()) {
            value = Integer.parseInt(str);
            value = Math.max(min,value);
        }
        return value;
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String genreId = request.getParameter("id");
        Integer limit = toIntWithMin(request.getParameter("limit"),1);
        Integer page = toIntWithMin(request.getParameter("page"),1);

        String sort1 = request.getParameter("sort1");

        String sort2 = request.getParameter("sort2");
        String order1 = request.getParameter("order1");
        String order2 = request.getParameter("order2");
        String offetClause = String.format(" offset %s",(page-1)*limit);
        String query = String.format("select id,title,director,year,rating,price from movies \n" +
                "join genres_in_movies on movieId=id left join ratings as r on id=r.movieId \n" +
                "where genreId=? order by %s %s, %s %s \n" +
                "limit %s",sort1,order1,sort2,order2,limit);
        JsonObject responseObj = new JsonObject();
        JsonArray movies = new JsonArray();
        String genreQry="select name,id from genres_in_movies join genres on genreId = id where movieId = ?  order by name limit 3;";
        String starQry = "select s.name as name, s.id as id from stars as s, stars_in_movies as sm where s.id=sm.starId\n" +
                "and sm.movieId=? \n" +
                "order by (select count(*) from stars_in_movies as sm2 where sm2.starId = s.id) desc,s.name limit 3; ";
        try (out; Connection conn = dataSource.getConnection()){
            PreparedStatement statement = conn.prepareStatement(query+offetClause);
            statement.setString(1,genreId);
            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();
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
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }finally{
            out.close();

        }

    }
}

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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {

    private long ts_startTime = 0, ts_endTime = 0,
    tj_startTime = 0, tj_endTime = 0;
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
    private Logger logger;

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
    private String setStringwithDefault(HttpServletRequest request, String paramName, String defaultval,String alter){
        if (request.getParameter(paramName)!=null&&request.getParameter(paramName)!=defaultval){
            defaultval = request.getParameter(paramName);
        }
        return defaultval;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger = new Logger();
        ts_startTime = System.nanoTime();
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String title=request.getParameter("title");
        String year= request.getParameter("year");
        String director= request.getParameter("director");
        String star= request.getParameter("star");
        String limit_str = "";
        if(request.getParameter("limit")!=""&&request.getParameter("limit")!=null){
            limit_str = request.getParameter("limit");
        }
        Integer limit = toIntWithMin(limit_str,5);
        String page_str = "";
        if(request.getParameter("page")!=""&&request.getParameter("page")!=null){
            page_str = request.getParameter("page");
        }
        Integer page = toIntWithMin(page_str,1);
        String sort1 = setStringwithDefault(request,"sort1","rating","title");

        String sort2 = setStringwithDefault(request,"sort2","title","rating");



        String order1 = setStringwithDefault(request,"order1","desc","asc");
        String order2 = setStringwithDefault(request,"order2","asc","desc");
        ArrayList<String> subqueries = new ArrayList<String>();
        if(title!=null&&title!=""&&!title.isBlank()){
            subqueries.add(String.format("m.title like '%%%s%%'",title));

        }
        if(year!=null&&year!=""&&!year.isBlank()){
            subqueries.add(String.format("m.year=%s",year));
        }
        if(director!=null&&director!=""&&!director.isBlank()){
            subqueries.add(String.format("m.director like '%%%s%%'",director));
        }
        String clause="";
        clause = String.join(" and ",subqueries);
        System.out.println("Clause: " + clause);
        String query;
        String offetClause = String.format(" offset %s",(page-1)*limit);
        if(star!=null&&star!=""&&!star.isBlank()){
            if(clause!=""){
            query = String.format("select m.title,m.year,m.director,m.id,r.rating,m.price from movies as m left join ratings as r on m.id=r.movieId join (select distinct m.id as mid from movies as m join stars_in_movies as sm join stars as s on m.id=sm.movieId and sm.starId = s.id  \n" +
                    "where s.name like \"%%%s%%\") as ms_table on ms_table.mid = m.id where %s order by %s %s, %s %s limit %s",star,clause,sort1,order1,sort2,order2,limit);
                    
            }else{
                query = String.format("select m.title,m.year,m.director,m.id,r.rating,m.price from movies as m left join ratings as r on m.id=r.movieId join (select distinct m.id as mid from movies as m join stars_in_movies as sm join stars as s on m.id=sm.movieId and sm.starId = s.id  \n" +
                    "where s.name like \"%%%s%%\") as ms_table on ms_table.mid = m.id order by %s %s, %s %s limit %s",star,sort1,order1,sort2,order2,limit);
            }
        }else{

            query = String.format("select m.title,m.year,m.director,m.id,r.rating,m.price from movies as m left join ratings as r on m.id=r.movieId where %s order by %s %s, %s %s limit %s",clause,sort1,order1,sort2,order2,limit);

        }
        JsonObject responseObj = new JsonObject();
        JsonArray movies = new JsonArray();
        String genreQry="select name,id from genres_in_movies join genres on genreId = id where movieId = ?  order by name limit 3;";
        String starQry = "select s.name as name, s.id as id from stars as s, stars_in_movies as sm where s.id=sm.starId\n" +
                "and sm.movieId=? \n" +
                "order by (select count(*) from stars_in_movies as sm2 where sm2.starId = s.id) desc,s.name limit 3; ";
        try (out; Connection conn = dataSource.getConnection()){
            tj_startTime = System.nanoTime();
            PreparedStatement statement = conn.prepareStatement(query+offetClause);

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
            tj_endTime = System.nanoTime();
            ts_endTime = System.nanoTime();
            long tsTotal = ts_endTime-ts_startTime;
            long tjTotal = tj_endTime-tj_startTime;
            String txtLine = String.format("%s,%s\n", Long.toString(tsTotal), Long.toString(tjTotal));
            logger.appendLine(txtLine);
            logger.close();
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

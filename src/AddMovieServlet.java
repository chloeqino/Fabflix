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
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    private String createQuery = "call add_movie (?,?,?,?,?,?, @message)";
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String movie= request.getParameter("movie");
        String director = request.getParameter("director");
        String year = request.getParameter("year");
        String price = request.getParameter("price");
        String star= request.getParameter("star");

        String genre= request.getParameter("genre");
        JsonObject responseObj = new JsonObject();
        try (out; Connection conn = dataSource.getConnection()){

            PreparedStatement statement = conn.prepareStatement(createQuery);
            statement.setString(1,movie);
            statement.setInt(2,Integer.parseInt(year));
            statement.setString(3,director);
            statement.setBigDecimal(4, new BigDecimal(price));
            statement.setString(5,star);
            statement.setString(6,genre);
            System.out.println(statement);

            ResultSet resultSet = statement.executeQuery();
            String message = "";
//            ResultSetMetaData rsmd = resultSet.getMetaData();
//            int columnsNumber = rsmd.getColumnCount();
//            while (resultSet.next()) {
//                for (int i = 1; i <= columnsNumber; i++) {
//                    if (i > 1) System.out.print(",  ");
//                    String columnValue = resultSet.getString(i);
//                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
//                }
//                System.out.println("");
//            }
            if (resultSet.next()){
                message = resultSet.getString("message");
            }
            responseObj.addProperty("status","success");
            responseObj.addProperty("message", message);
            resultSet.close();
            statement.close();
            System.out.println(responseObj.toString());
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
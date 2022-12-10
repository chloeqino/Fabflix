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

@WebServlet(name = "GetGenresServlet", urlPatterns = "/api/all-genres")
public class GetGenresServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query = "select distinct id,name from genres;";
        PrintWriter out = response.getWriter();
        JsonArray genres = new JsonArray();
        try (out; Connection conn = dataSource.getConnection()){
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                String id = rs.getString("id");
                String name = rs.getString("name");
                JsonObject obj = new JsonObject();
                obj.addProperty("id",id);
                obj.addProperty("name",name);
                genres.add(obj);
            }
            rs.close();
            statement.close();
            out.write(genres.toString());
            response.setStatus(200);


        }catch (Exception e) {
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

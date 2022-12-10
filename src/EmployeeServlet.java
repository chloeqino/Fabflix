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
import java.util.ArrayList;

@WebServlet(name = "EmployeeServlet", urlPatterns = "/api/employee")
public class EmployeeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    private String noYear = "INSERT INTO stars (id, name) VALUES(?,?);";

    private String years = "INSERT INTO stars (id, name, birthYear) VALUES(?,?,?);";

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
        String year= request.getParameter("year");
        String star= request.getParameter("star");
        JsonObject responseObj = new JsonObject();
        try (out; Connection conn = dataSource.getConnection()){
            String createQuery = "select max(cast(ifnull(substr(id,5),0) as unsigned)) as newID from stars where id like 'star%'";

            PreparedStatement statement = conn.prepareStatement(createQuery);

            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();
            String id = "star";
            // fix
            if (resultSet.next()){
                String result = resultSet.getString("newID");
                if (result == null) {
                    id = id + "1";
                }
                else {
                    String newID = String.valueOf(Integer.parseInt(result) + 1);
                    id = id + newID;
                }
            }
            System.out.println(id);
            if (year==null||year==""||year.isBlank()){
                statement = conn.prepareStatement(noYear);
                statement.setString(1,id);
                statement.setString(2,star);
            }
            else {
                statement = conn.prepareStatement(years);
                statement.setString(1,id);
                statement.setString(2,star);
                statement.setInt(3,Integer.parseInt(year));

            }
            System.out.println(statement);
            statement.executeUpdate();
            System.out.println("enter");
            responseObj.addProperty("status","success");
            responseObj.addProperty("message", "succesfully added star id: " + id);
            resultSet.close();
            statement.close();
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
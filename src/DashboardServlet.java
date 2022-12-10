import com.google.gson.JsonObject;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String query = "select * from employees where email = ? ;";
        PrintWriter out = response.getWriter();

        //        Recaptcha Verification
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        }
        catch (Exception e){
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Recaptcha Error");
            out.write(responseJsonObject.toString());
            out.close();
            return;
        }

        try (out; Connection conn = dataSource.getConnection()) {
            JsonObject responseJsonObject = new JsonObject();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,email);
            ResultSet rs = statement.executeQuery();
            boolean success = false;
            if(rs.next()){
                String encryptedPassword = rs.getString("password");
                success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                System.out.println("SUCCESS: " + success);
                if (success) {
                    request.getSession().setAttribute("employee", new User(email));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
            }
            if (!success){
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message","incorrect email or password");
            }
            rs.close();
            statement.close();
            out.write(responseJsonObject.toString());
            response.setStatus(200);

        }catch (Exception e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }finally {
            out.close();
        }



    }
}

// AUTOMATE METADATA
//    SELECT COLUMN_NAME
//    FROM INFORMATION_SCHEMA.COLUMNS
//        WHERE
//        TABLE_SCHEMA = Database()
//        AND TABLE_NAME = 'sales' ;
//
//    SELECT TABLE_NAME
//    FROM INFORMATION_SCHEMA.TABLES
//    WHERE
//    TABLE_SCHEMA = Database();
//
//    SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
//    WHERE TABLE_SCHEMA = Database() AND table_name = 'stars' AND COLUMN_NAME = 'name';
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
import java.sql.Statement;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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
        String recap = request.getParameter("recap");
        System.out.println(email);
        System.out.println(password);
        String query = "select * from customers where email = ? ;";
        PrintWriter out = response.getWriter();

        //        Recaptcha Verification
//        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//        if (recap == null || recap == "") {
//            try {
//                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//            } catch (Exception e) {
//                JsonObject responseJsonObject = new JsonObject();
//                responseJsonObject.addProperty("status", "fail");
//                responseJsonObject.addProperty("message", "Recaptcha Error");
//                out.write(responseJsonObject.toString());
//                out.close();
//                return;
//            }
//        }

        try (out; Connection conn = dataSource.getConnection()) {
            System.out.println("Enter");
            //        Recaptcha Verification
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            if (recap == null || recap == "") {
                try {
                    RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                } catch (Exception e) {
                    JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Recaptcha Error");
                    out.write(responseJsonObject.toString());
                    out.close();
                    return;
                }
            }

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
                    request.getSession().setAttribute("user", new User(email));
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
            System.out.println(responseJsonObject.toString());
            out.write(responseJsonObject.toString());
            response.setStatus(200);

        }catch (Exception e){
            System.out.println("ERROR");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            System.out.println(jsonObject.toString());
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
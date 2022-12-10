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

@WebServlet(name = "LogoutServlet", urlPatterns = "/api/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().setAttribute("user", null);
    }
}
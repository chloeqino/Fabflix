import com.google.gson.JsonObject;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.EOFException;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

@WebServlet(name = "CheckOutServlet", urlPatterns = "/api/checkout")
public class CheckOutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
    private String query = "select id as ccn, expiration,firstName as first, lastName as last from creditcards where id=? and expiration=?\n" +
            "and firstName=? and lastName=?;";
    //customerId,movieId,date,qty
    private String insertQry = "INSERT INTO sales VALUES(null,?,?,?,?);";
    private String idQry = "select id from customers where email=?;";
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    public java.sql.Date getCurrentDatetime() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Date(today.getTime());
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String p_ccn = request.getParameter("ccn");
        String p_date = request.getParameter("expDate");
        String last = request.getParameter("last");
        String first = request.getParameter("first");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String email = user.getEmail();
        System.out.println(email);
        String cid = "";
        HashMap<String,Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");
        System.out.println(cartItems);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();
        String currentDate = dtf.format(now);
        System.out.println(currentDate);


        try (out; Connection conn=dataSource.getConnection()){
            JsonObject resObj = new JsonObject();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,p_ccn);
            statement.setString(2,p_date);
            statement.setString(3,first);
            statement.setString(4,last);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                resObj.addProperty("status", "success");
                resObj.addProperty("message", "success");
                PreparedStatement id_statement = conn.prepareStatement(idQry);
                id_statement.setString(1,email);
                ResultSet id_rs = id_statement.executeQuery();
                if(id_rs.next()){
                    cid = id_rs.getString("id");
                    System.out.println(cid);
                    for (String k:cartItems.keySet()){
                        PreparedStatement istatement = conn.prepareStatement(insertQry);
                        //customerId,movieId,date,qty
                        System.out.println(k);
                        istatement.setString(1,cid);
                        System.out.println("set cid"+cid);
                        istatement.setString(2,k);
                        istatement.setDate(3, getCurrentDatetime());
                        System.out.println("set date");
                        istatement.setInt(4,cartItems.get(k));
                        //System.out.println(istatement);
                        istatement.executeUpdate();
                        istatement.close();
                    }
                    cartItems.clear();
                }
            }else{
                resObj.addProperty("status", "fail");
                resObj.addProperty("message","Invalid Card Info");
            }
            rs.close();
            statement.close();
            out.write(resObj.toString());
            response.setStatus(200);

        }catch (Exception e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println("Error:"+e.getMessage());
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }finally{
            out.close();
        }

    }

}


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
@WebServlet(name="CartServlet",urlPatterns = "/api/cart")

public class CartServlet extends HttpServlet {
    private JsonObject mapToJSON(HashMap<String,Integer> hashMap,Connection conn) throws SQLException {
        JsonObject obj = new JsonObject();
        JsonArray items = new JsonArray();
        BigDecimal totalPrice = new BigDecimal("0.0");
        for (String i:hashMap.keySet()){
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,i);
            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();
            JsonObject item = new JsonObject();
            if(resultSet.next()){
            item.addProperty("id",resultSet.getString("id"));
            item.addProperty("title",resultSet.getString("title"));
            item.addProperty("year",resultSet.getString("year"));
            item.addProperty("price",resultSet.getString("price"));
            item.addProperty("quantity",hashMap.get(i));
            BigDecimal a = new BigDecimal(resultSet.getString("price"));
            BigDecimal b = new BigDecimal(hashMap.get(i));
            System.out.println(a);
            System.out.println(b);
            System.out.println(a.multiply(b));
            totalPrice = totalPrice.add(a.multiply(b));
            items.add(item);
            }
            resultSet.close();
            statement.close();


        }
        obj.addProperty("total",String.format("%,.2f", totalPrice));
        obj.add("items",items);
        return obj;
    }
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
    private String query = "select title,id,year,price from movies where id=?;";

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        HashMap<String,Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");
        JsonObject resObj = new JsonObject();
        if(cartItems==null){
            resObj.addProperty("total",0);
            resObj.add("items",new JsonArray());
            out.write(resObj.toString());
            response.setStatus(200);
            out.close();
        }else{
        try (out; Connection conn = dataSource.getConnection()){
            resObj = mapToJSON(cartItems,conn);
            out.write(resObj.toString());
            response.setStatus(200);

        }catch (Exception e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }finally{
            out.close();

        }}

    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String action = request.getParameter("action");
        String itemId = request.getParameter("id");
        HttpSession session = request.getSession();
        HashMap<String,Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");
        if(cartItems==null){
            cartItems = new HashMap<>();
            if(action.equals("add")){
                cartItems.merge(itemId, 1, (a,b) -> a+b);
                session.setAttribute("cartItems",cartItems);
            }

        }else{
            synchronized (cartItems){
                if(action.equals("remove")){
                    if ((cartItems.get(itemId)!=null)&(cartItems.get(itemId))>0){
                        cartItems.put(itemId,cartItems.get(itemId)-1);
                        if(cartItems.get(itemId)<=0){
                            cartItems.remove(itemId);
                        }
                    }
                }
                else if (action.equals("add")){
                    cartItems.merge(itemId, 1, (a,b) -> a+b);
                }else if(action.equals("delete")){
                    if(cartItems.get(itemId)!=null){
                        cartItems.remove(itemId);
                    }
                }
            }
        }

        try (Connection conn = dataSource.getConnection()){

            out.write(mapToJSON(cartItems,conn).toString());
            response.setStatus(200);


        }catch (Exception e){
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

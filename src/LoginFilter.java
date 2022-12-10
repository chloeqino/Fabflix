import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> employeeURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());
        if (httpRequest.getSession().getAttribute("user") != null && this.isUrlAllowedWithEmployee(httpRequest.getRequestURI())) {
            if(httpRequest.getRequestURI().contains("/api/")){
                httpResponse.sendRedirect("../login.html");
            }else {
                httpResponse.sendRedirect("login.html");
            }
        }


        if (httpRequest.getSession().getAttribute("user") == null && this.isUrlAllowedWithEmployee(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            if(httpRequest.getRequestURI().contains("/api/")){
                httpResponse.sendRedirect("../login.html");
            }else {
                httpResponse.sendRedirect("login.html");
            }
        } else {
            chain.doFilter(request, response);
        }

    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isUrlAllowedWithEmployee(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return employeeURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("login.css");
        allowedURIs.add("api/login");
        allowedURIs.add("style.css");
        employeeURIs.add("_dashboard");
        employeeURIs.add("_dashboard.html");
        employeeURIs.add("_dashboard.js");
        employeeURIs.add("api/dashboard");
        employeeURIs.add("employee.html");
        employeeURIs.add("employee.js");
        employeeURIs.add("api/employee");
        employeeURIs.add("api/add-movie");
        allowedURIs.add("favicon.ico");
    }

    public void destroy() {
        // ignored.
    }

}
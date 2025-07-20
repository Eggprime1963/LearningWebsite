package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import org.json.JSONObject;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail != null) {
                request.setAttribute("title", "Logged In");
                request.setAttribute("message", "User is logged in: " + userEmail);
            } else {
                request.setAttribute("title", "Not Logged In");
                request.setAttribute("message", "User is not logged in.");
            }
        } else {
            request.setAttribute("title", "No Active Session");
            request.setAttribute("message", "No active session found.");
        }

        request.getRequestDispatcher("WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();

        // Check if request is JSON (Google login)
        if ("application/json".equals(request.getContentType())) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String json = sb.toString();
            JSONObject obj = new JSONObject(json);
            String googleId = obj.optString("googleId");
            String email = obj.optString("email");
            String firstName = obj.optString("firstName");
            String lastName = obj.optString("lastName");
            String idToken = obj.optString("idToken");

            Optional<User> userOpt = userDAO.findByGoogleId(googleId);
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                session.setAttribute("user", user);
                session.setAttribute("role", user.getRole());
                session.setAttribute("id", user.getId());
                out.print("{\"success\":true,\"redirect\":\"" + request.getContextPath() + "/home\"}");
            } else {
                // Store Google info in session for registration
                session.setAttribute("tempGoogleId", googleId);
                session.setAttribute("tempGoogleEmail", email);
                session.setAttribute("tempGoogleFirstName", firstName);
                session.setAttribute("tempGoogleLastName", lastName);
                out.print("{\"register\":true,\"redirect\":\"" + request.getContextPath() + "/register\"}");
            }
            out.flush();
            return;
        }

        // Standard login (form)
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            request.setAttribute("message", "Username and password are required.");
            request.getRequestDispatcher("WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        Optional<User> userOpt = userDAO.authenticate(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRole());
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            request.setAttribute("message", "Invalid email or password.");
            request.getRequestDispatcher("WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }
}


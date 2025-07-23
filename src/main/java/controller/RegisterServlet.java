package Controller;

import java.io.IOException;

import org.mindrot.jbcrypt.BCrypt;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect to registration page
        request.getRequestDispatcher("WEB-INF/jsp/register.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String role = request.getParameter("role");
        // Check if Google ID is present for Google registration
        String googleId = (String) request.getSession().getAttribute("tempGoogleId");

        if (email == null || username == null || (password == null && googleId == null)) {
            request.setAttribute("message", "All required fields must be filled.");
            request.getRequestDispatcher("WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        // Check if user already exists
        if (userDAO.findByEmailOrUsername(email).isPresent()) {
            request.setAttribute("message", "Email is already registered.");
            request.getRequestDispatcher("WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        // Hash password if not Google registration
        String hashedPassword = (googleId == null) ? BCrypt.hashpw(password, BCrypt.gensalt()) : "";

        User user = new User(username, hashedPassword, email, role, googleId);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        try {
            userDAO.save(user);
            request.getSession().removeAttribute("tempGoogleId");
            request.getSession().removeAttribute("tempGoogleEmail");
            request.setAttribute("message", "Registration successful! Please log in.");
        } catch (Exception e) {
            request.setAttribute("message", "Registration failed: " + e.getMessage());
        }
        request.getRequestDispatcher("WEB-INF/jsp/register.jsp").forward(request, response);
    }

}

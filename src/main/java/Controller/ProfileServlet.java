<<<<<<< HEAD
package Controller;
=======
package controller;
>>>>>>> 53ab6c237513d4082a5582bd813ee73d3fa9835c

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

<<<<<<< HEAD
import Dao.ActivityDAO;
=======
import dao.ActivityDAO;
>>>>>>> 53ab6c237513d4082a5582bd813ee73d3fa9835c
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ActivityDate;
import model.User;
import model.UserActivity;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        // Set user fields for profile edit
        request.setAttribute("firstName", user.getFirstName());
        request.setAttribute("lastName", user.getLastName());
        request.setAttribute("gender", user.getGender());
        request.setAttribute("phone", user.getPhoneNumber());
        request.setAttribute("address", user.getAddress());
        request.setAttribute("school", user.getSchool());
        request.setAttribute("username", user.getUsername());

        List<UserActivity> activities = activityDAO.getUserActivities(user);
        request.setAttribute("userActivities", activities);

        // Prepare activity date map for calendar
        LocalDate today = LocalDate.now();
        Map<Integer, ActivityDate> activityDateMap = new LinkedHashMap<>();
        Map<String, Long> activityTotals = new HashMap<>();

        for (int i = -15; i <= 14; i++) {
            LocalDate date = today.plusDays(i);
            String dateKey = date.toString();
            long totalMinutes = activities.stream()
                .filter(a -> a.getLoginTime() != null && a.getLoginTime().toLocalDate().equals(date))
                .mapToLong(a -> a.getDurationMinutes() != null ? a.getDurationMinutes() : 0)
                .sum();
            activityDateMap.put(i, new ActivityDate(date.getDayOfMonth(), date.getMonthValue(), date.getYear(), date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), dateKey));
            activityTotals.put(dateKey, totalMinutes);
        }
        request.setAttribute("activityDateMap", activityDateMap);
        request.setAttribute("activityTotals", activityTotals);
        request.setAttribute("currentDay", today.getDayOfMonth());
        request.setAttribute("currentMonth", today.getMonthValue());
        request.setAttribute("currentYear", today.getYear());

        request.getRequestDispatcher("WEB-INF/jsp/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        // Get updated fields from form
        user.setFirstName(request.getParameter("firstName"));
        user.setLastName(request.getParameter("lastName"));
        user.setGender(request.getParameter("gender"));
        user.setPhoneNumber(request.getParameter("phone"));
        user.setAddress(request.getParameter("address"));
        user.setSchool(request.getParameter("school"));
        user.setUsername(request.getParameter("username"));
        // Do NOT update email: user.setEmail(...) is intentionally omitted


        userDAO.save(user); // Assumes save() does update if entity has ID
        session.setAttribute("user", user);

        // Set user fields for profile edit
        request.setAttribute("firstName", user.getFirstName());
        request.setAttribute("lastName", user.getLastName());
        request.setAttribute("gender", user.getGender());
        request.setAttribute("phone", user.getPhoneNumber());
        request.setAttribute("address", user.getAddress());
        request.setAttribute("school", user.getSchool());
        request.setAttribute("username", user.getUsername());

        request.setAttribute("message", "Profile updated successfully.");
        List<UserActivity> activities = activityDAO.getUserActivities(user);
        request.setAttribute("userActivities", activities);

        // Prepare activity date map for calendar
        LocalDate today = LocalDate.now();
        Map<Integer, ActivityDate> activityDateMap = new LinkedHashMap<>();
        Map<String, Long> activityTotals = new HashMap<>();

        for (int i = -15; i <= 14; i++) {
            LocalDate date = today.plusDays(i);
            String dateKey = date.toString();
            long totalMinutes = activities.stream()
                .filter(a -> a.getLoginTime() != null && a.getLoginTime().toLocalDate().equals(date))
                .mapToLong(a -> a.getDurationMinutes() != null ? a.getDurationMinutes() : 0)
                .sum();
            activityDateMap.put(i, new ActivityDate(date.getDayOfMonth(), date.getMonthValue(), date.getYear(), date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), dateKey));
            activityTotals.put(dateKey, totalMinutes);
        }
        request.setAttribute("activityDateMap", activityDateMap);
        request.setAttribute("activityTotals", activityTotals);
        request.setAttribute("currentDay", today.getDayOfMonth());
        request.setAttribute("currentMonth", today.getMonthValue());
        request.setAttribute("currentYear", today.getYear());

        System.out.println("DEBUG ProfileServlet: firstName=" + user.getFirstName() +
            ", lastName=" + user.getLastName() +
            ", gender=" + user.getGender() +
            ", phone=" + user.getPhoneNumber() +
            ", address=" + user.getAddress() +
            ", school=" + user.getSchool() +
            ", googleId=" + user.getGoogleId());

        request.getRequestDispatcher("WEB-INF/jsp/profile.jsp").forward(request, response);
    }
}

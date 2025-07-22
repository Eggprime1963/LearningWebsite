package controller;

import java.io.IOException;
import java.util.List;

import dao.CourseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Course;

@WebServlet(name = "BrowseServlet", urlPatterns = {"/browse", "/explore", "/catalog"})
public class BrowseServlet extends HttpServlet {
    private CourseDAO courseDAO = new CourseDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Course> courses = courseDAO.getAllCourses();
        request.setAttribute("courses", courses);
        request.getRequestDispatcher("/WEB-INF/jsp/browse.jsp").forward(request, response);
    }
}
package controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import dao.CourseDAO;
import dao.StudentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Course;
import model.User;

@WebServlet(name = "StudentServlet", urlPatterns = {"/students", "/student", "/learners"})
public class StudentServlet extends HttpServlet {
    private final CourseDAO courseDAO = new CourseDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String courseIdParam = request.getParameter("courseId");
            List<Course> courses = courseDAO.getCourses();
            request.setAttribute("courses", courses);

            List<User> students = null;
            Optional<Course> selectedCourseOpt = Optional.empty();
            Course selectedCourse = null;
            if (courseIdParam != null) {
                int courseId = Integer.parseInt(courseIdParam);
                selectedCourseOpt = courseDAO.findCourseById(courseId);
                if (selectedCourseOpt.isPresent()) {
                    selectedCourse = selectedCourseOpt.get();
                    students = studentDAO.getStudentsByCourse(courseId);
                }
            }
            request.setAttribute("selectedCourse", selectedCourse);
            request.setAttribute("students", students);

            request.getRequestDispatcher("/WEB-INF/jsp/studentList.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("exception", e);
            request.getRequestDispatcher("/WEB-INF/jsp/studentList.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        int courseId = Integer.parseInt(request.getParameter("courseId"));
        int studentId = Integer.parseInt(request.getParameter("studentId"));

        if ("add".equals(action)) {
            studentDAO.addStudentToCourse(studentId, courseId);
        } else if ("remove".equals(action)) {
            studentDAO.removeStudentFromCourse(studentId, courseId);
        }
        response.sendRedirect(request.getContextPath() + "/students?courseId=" + courseId);
    }
}
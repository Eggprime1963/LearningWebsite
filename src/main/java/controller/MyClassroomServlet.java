package controller;

import java.io.IOException;
import java.util.List;

import dao.AssignmentDAO;
import dao.CourseDAO;
import dao.LectureDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Assignment;
import model.Course;
import model.Lecture;


@WebServlet(name = "MyClassroomServlet", urlPatterns = {"/myClassroom", "/classroom", "/my-courses"})
public class MyClassroomServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String section = request.getParameter("section");
        if (section == null) section = "courses";
        request.setAttribute("section", section);

        Object userIdObj = request.getSession().getAttribute("id");
        String role = (String) request.getSession().getAttribute("role");
        if (userIdObj == null || role == null) {
            throw new ServletException("User not logged in.");
        }
        int userId = (Integer) userIdObj;

        boolean canCrud = "teacher".equals(role);
        request.setAttribute("canCrud", canCrud);

        if ("courses".equals(section)) {
            List<Course> courses;
            if (canCrud) {
                // Teacher: show courses they teach
                courses = new CourseDAO().getCoursesByTeacher(userId);
            } else {
                // Student: show enrolled courses
                courses = new CourseDAO().getCoursesByStudent(userId);
            }
            request.setAttribute("courses", courses);
        } else {
            // For assignments/lectures, dropdown shows enrolled (student) or taught (teacher) courses
            List<Course> courses = canCrud
                ? new CourseDAO().getCoursesByTeacher(userId)
                : new CourseDAO().getCoursesByStudent(userId);
            request.setAttribute("courses", courses);

            String courseIdParam = request.getParameter("courseId");
            int courseId = (courseIdParam != null) ? Integer.parseInt(courseIdParam) : 0;
            Course selectedCourse = null;
            if (courseId != 0) {
                selectedCourse = new CourseDAO().findCourseById(courseId).orElse(null);
                request.setAttribute("selectedCourse", selectedCourse);
            }

            if ("assignments".equals(section) && courseId != 0) {
                List<Assignment> assignments = new AssignmentDAO().getAssignmentsByCourseId(courseId);
                request.setAttribute("assignments", assignments);
            } else if ("lectures".equals(section) && courseId != 0) {
                List<Lecture> lectures = new LectureDAO().getLecturesByCourseId(courseId);
                request.setAttribute("lectures", lectures);
            }
        }

        request.getRequestDispatcher("/WEB-INF/jsp/myClassroom.jsp").forward(request, response);
    }

    private int getCurrentUserId(HttpServletRequest request) throws ServletException {
        Object userIdObj = request.getSession().getAttribute("id");
        if (userIdObj == null) {
            throw new ServletException("User not logged in.");
        }
        return (Integer) userIdObj;
    }
}
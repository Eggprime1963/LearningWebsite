package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao.CourseDAO;
import dao.EnrollmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.Enrollment;
import model.User;
@WebServlet(name = "StudentListServlet", urlPatterns = {"/studentList", "/students-list"})
public class StudentListServlet extends HttpServlet {
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"teacher".equals(session.getAttribute("role"))) {
            response.sendRedirect("WEB-INF/jsp/login.jsp");
            return;
        }

        try {
            Integer teacherIdObj = (Integer) session.getAttribute("teacherId");
            if (teacherIdObj == null) {
                request.setAttribute("error", "Teacher ID is not set in session. Please log in again.");
                request.getRequestDispatcher("/WEB-INF/jsp/enrolledCourseList.jsp").forward(request, response);
                return;
            }
            int teacherId = teacherIdObj.intValue();

            List<Course> courses = courseDAO.getCoursesByTeacher(teacherId);
            if (courses != null && !courses.isEmpty()) {
                for (Course course : courses) {
                    List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByCourseId(course.getIdCourse());
                    course.setEnrollments(enrollments);
                    if (enrollments != null) {
                        List<User> students = new ArrayList<>();
                        for (Enrollment enrollment : enrollments) {
                            students.add(enrollment.getStudent());
                        }
                        course.setStudents(students);
                    }
                }
            } else {
                System.out.println("No courses found for teacherId: " + teacherId);
            }

            request.setAttribute("courses", courses);
            request.getRequestDispatcher("/WEB-INF/jsp/enrolledCourseList.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving student list: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/enrolledCourseList.jsp").forward(request, response);
        }
    }
}
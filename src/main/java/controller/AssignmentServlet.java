package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

import dao.AssignmentDAO;
import dao.CourseDAO;
import dao.LectureDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Assignment;
import model.Course;
import model.Lecture;
import model.User;
import util.AccessControl;

@WebServlet(name = "AssignmentServlet", urlPatterns = {"/assignments", "/assignment", "/tasks"})
public class AssignmentServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AssignmentServlet.class.getName());
    private final CourseDAO courseDAO = new CourseDAO();
    private final LectureDAO lectureDAO = new LectureDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Use AccessControl instead of manual session checking
        if (!AccessControl.validateAccess(request, response, AccessControl.AccessLevel.TEACHER)) {
            return; // Access validation handles the error response
        }
        
        HttpSession session = request.getSession();

        int courseId = Integer.parseInt(request.getParameter("courseId"));
        String idLectureParam = request.getParameter("lecture_id");
        Integer idLecture = (idLectureParam != null && !idLectureParam.isEmpty()) ? Integer.parseInt(idLectureParam) : null;
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDateStr = request.getParameter("dueDate"); // Format: yyyy-MM-ddTHH:mm
        String status = request.getParameter("status");
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty() || dueDateStr == null) {
            request.setAttribute("error", "Assignment title, description, and due date are required.");
            request.getRequestDispatcher(request.getContextPath() + "/assignments");
            return;
        }

        try {
        // Convert input date string to LocalDateTime
        LocalDateTime dueDate = LocalDateTime.parse(dueDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        Course course = courseDAO.getCourseById(courseId);
        Lecture lecture = (idLecture != null) ? lectureDAO.getLectureById(idLecture) : null;
        Assignment assignment = new Assignment(course, lecture, title, description, dueDate, status);
        assignmentDAO.saveAssignment(assignment);
        logger.info("Assignment added successfully for course ID: " + courseId);
        session.setAttribute("success", true);
        response.sendRedirect(request.getContextPath() + "/assignments");
        } catch (Exception e) {
            logger.severe("Unexpected error while adding assignment: " + e.getMessage());
            request.setAttribute("error", "An unexpected error occurred: " + e.getMessage());
            request.getRequestDispatcher(request.getContextPath() + "/assignments");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Use AccessControl instead of manual session checking
        if (!AccessControl.validateAccess(request, response, AccessControl.AccessLevel.TEACHER)) {
            return; // Access validation handles the error response
        }
        
        HttpSession session = request.getSession();
            Boolean success = (Boolean) session.getAttribute("success");
            if (success != null) {
                request.setAttribute("success", true);
                session.removeAttribute("success");
            }
        String courseIdParam = request.getParameter("courseId");
        String lectureIdParam = request.getParameter("lectureId");

        try {
            List<Course> courseList = courseDAO.getCoursesByTeacher(((User) session.getAttribute("user")).getId());
            request.setAttribute("courseList", courseList);

            if (courseIdParam != null && !courseIdParam.isEmpty()) {
                int courseId = Integer.parseInt(courseIdParam);
                request.setAttribute("selectedCourseId", courseId);

                List<Lecture> lectureList = lectureDAO.getLecturesByCourseId(courseId);
                request.setAttribute("lectureList", lectureList);

                if (lectureIdParam != null && !lectureIdParam.isEmpty()) {
                    int lectureId = Integer.parseInt(lectureIdParam);
                    request.setAttribute("selectedLectureId", lectureId);

                    List<Assignment> assignmentList = assignmentDAO.getAssignmentsByLecture(courseId, lectureId);
                    request.setAttribute("assignmentList", assignmentList);
                }
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error loading data: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/jsp/assignmentDetail.jsp").forward(request, response);
    }
}

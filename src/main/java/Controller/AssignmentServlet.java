package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import dao.AssignmentDAO;
import dao.CourseDAO;
import dao.LectureDAO;
import dao.SubmissionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Assignment;
import model.Course;
import model.Submission;

@WebServlet(name = "AssignmentServlet", urlPatterns = {"/assignments"})
public class AssignmentServlet extends HttpServlet {
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final LectureDAO lectureDAO = new LectureDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String role = (String) request.getSession().getAttribute("role");
        int userId = (int) request.getSession().getAttribute("id");
        int lectureId = Integer.parseInt(request.getParameter("lectureId"));

        List<Assignment> assignments = assignmentDAO.getAssignmentsByLecture(lectureId);
        request.setAttribute("assignments", assignments);

        if ("teacher".equals(role)) {
            // Optionally, load all submissions for these assignments
            Map<Integer, List<Submission>> submissions = submissionDAO.getSubmissionsByAssignments(assignments);
            request.setAttribute("submissions", submissions);
        } else if ("student".equals(role)) {
            // Load student's submissions for these assignments
            Map<Integer, model.Submission> mySubmissions = submissionDAO.getSubmissionsByStudentAndAssignments(userId, assignments);
            request.setAttribute("mySubmissions", mySubmissions);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.setAttribute("role", role);
        request.getRequestDispatcher("WEB-INF/jsp/assignmentList.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "create";
        
        int courseId = Integer.parseInt(request.getParameter("courseId"));

        try {
            switch (action) {
                case "update":
                    updateAssignment(request);
                    break;
                case "delete":
                    deleteAssignment(request);
                    break;
                default:
                    createAssignment(request);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException("Error processing assignment action: " + action, e);
        }
        response.sendRedirect(request.getContextPath() + "/assignments?courseId=" + courseId);
    }

    private void createAssignment(HttpServletRequest request) throws ServletException {
        int courseId = Integer.parseInt(request.getParameter("courseId"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        LocalDateTime dueDate = LocalDateTime.parse(request.getParameter("dueDate"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Course course = courseDAO.findCourseById(courseId)
                .orElseThrow(() -> new ServletException("Course not found"));

        // Use a default status string, e.g. "not yet"
        Assignment assignment = new Assignment(course, null, title, description, dueDate, "not yet");
        assignmentDAO.saveAssignment(assignment);
    }

    private void updateAssignment(HttpServletRequest request) throws ServletException {
        int assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        LocalDateTime dueDate = LocalDateTime.parse(request.getParameter("dueDate"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String status = request.getParameter("status"); // Should be "not yet", "in progress", or "ended"

        Assignment assignment = assignmentDAO.getAssignmentById(assignmentId)
                .orElseThrow(() -> new ServletException("Assignment not found"));
        
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        if (status != null) assignment.setStatus(status);
        assignmentDAO.updateAssignment(assignment);
    }

    private void deleteAssignment(HttpServletRequest request) {
        int assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
        assignmentDAO.deleteAssignment(assignmentId);
    }
}

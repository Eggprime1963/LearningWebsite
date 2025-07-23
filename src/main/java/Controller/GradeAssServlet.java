package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao.AssignmentDAO;
import dao.CourseDAO;
import dao.LectureDAO;
import dao.SubmissionDAO;
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
import model.Submission;
import model.User;
@WebServlet(name = "GradeAssServlet", urlPatterns = {"/gradeAssignments", "/grade"})
public class GradeAssServlet extends HttpServlet {
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final LectureDAO lectureDAO = new LectureDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        System.out.println("Session exists: " + (session != null) + ", Role: " + (session != null ? session.getAttribute("role") : "null"));
        if (session == null || !"teacher".equals(session.getAttribute("role"))) {
            System.out.println("Redirecting to signIn.jsp due to invalid session/role");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            User teacher = (User) session.getAttribute("user");
            if (teacher == null) {
                throw new Exception("Teacher user not found in session");
            }

            List<Course> courses = courseDAO.getCoursesByTeacher(teacher.getId());
            System.out.println("Found courses for teacher: " + courses.size());

            List<Assignment> allAssignments = new ArrayList<>();
            for (Course course : courses) {
                List<Assignment> assignments = assignmentDAO.getAssignmentsByCourseId(course.getIdCourse());
                allAssignments.addAll(assignments);
            }
            System.out.println("Found assignments: " + allAssignments.size());

            List<Submission> allSubmissions = new ArrayList<>();
            for (Assignment assignment : allAssignments) {
                List<Submission> submissions = submissionDAO.getSubmissionsBeforeDueDate(assignment.getId(), assignment.getDueDateAsJavaUtilDate());
                allSubmissions.addAll(submissions);
            }
            System.out.println("Total submissions found: " + allSubmissions.size());

            // Enrich submissions with course and lecture details
            for (Submission s : allSubmissions) {
                if (s.getAssignment() != null) {
                    Course course = courseDAO.getCourseById(s.getAssignment().getIdCourse());
                    if (course != null) {
                        s.getAssignment().setCourse(course);
                    }
                    if (s.getAssignment().getIdLecture() > 0) {
                        Lecture lecture = lectureDAO.getLectureById(s.getAssignment().getIdLecture());
                        if (lecture != null) {
                            s.getAssignment().setLecture(lecture);
                        }
                    }
                }
            }

            request.setAttribute("submissions", allSubmissions);
            if (!allAssignments.isEmpty()) {
                request.setAttribute("assignment", allAssignments.get(0));
            }

            request.getRequestDispatcher("/WEB-INF/jsp/gradeAssignments.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occurred: " + e.getMessage());
            request.setAttribute("error", "Error retrieving submissions: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/gradeAssignments.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        System.out.println("Session exists in POST: " + (session != null) + ", Role: " + (session != null ? session.getAttribute("role") : "null"));
        if (session == null || !"teacher".equals(session.getAttribute("role"))) {
            System.out.println("Redirecting to signIn.jsp in POST due to invalid session/role");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int submissionId = Integer.parseInt(request.getParameter("submissionId"));
            double grade = Double.parseDouble(request.getParameter("grade"));
            System.out.println("Updating grade for submissionId: " + submissionId + " with grade: " + grade);

            submissionDAO.updateGrade(submissionId, grade);
            response.sendRedirect(request.getContextPath() + "/gradeAssignments");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in POST: " + e.getMessage());
            request.setAttribute("error", "Failed to grade submission: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/gradeAssignments.jsp").forward(request, response);
        }
    }
}
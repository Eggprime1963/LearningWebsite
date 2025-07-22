package controller;

import java.io.IOException;
import java.util.List;

import dao.CourseDAO;
import dao.LectureDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Course;
import model.Lecture;

@WebServlet(name = "LectureServlet", urlPatterns = {"/lectures", "/lecture", "/videos"})
public class LectureServlet extends HttpServlet {
    private final LectureDAO lectureDAO = new LectureDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String role = (String) request.getSession().getAttribute("role");
        int userId = (int) request.getSession().getAttribute("id");
        int courseId = Integer.parseInt(request.getParameter("courseId"));

        List<Lecture> lectures;
        if (null == role) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else switch (role) {
            case "teacher":
                lectures = lectureDAO.getLecturesByCourseForTeacher(courseId, userId);
                break;
            case "student":
                lectures = lectureDAO.getLecturesByCourseForStudent(courseId, userId);
                break;
            default:
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
        }
        request.setAttribute("lectures", lectures);
        request.setAttribute("role", role);
        request.setAttribute("courseId", courseId);
        request.getRequestDispatcher("/WEB-INF/jsp/lectureDetail.jsp").forward(request, response);
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
                    updateLecture(request);
                    break;
                case "delete":
                    deleteLecture(request);
                    break;
                default:
                    createLecture(request);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException("Error processing lecture action: " + action, e);
        }
        response.sendRedirect(request.getContextPath() + "/lectures?courseId=" + courseId);
    }

    private void createLecture(HttpServletRequest request) throws ServletException {
        int courseId = Integer.parseInt(request.getParameter("courseId"));
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String videoUrl = request.getParameter("videoUrl");

        Course course = courseDAO.findCourseById(courseId)
                .orElseThrow(() -> new ServletException("Course not found"));
        
        Lecture lecture = new Lecture(course, title, content, videoUrl, "active");
        lectureDAO.saveLecture(lecture);
    }

    private void updateLecture(HttpServletRequest request) throws ServletException {
        int lectureId = Integer.parseInt(request.getParameter("lectureId"));
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String videoUrl = request.getParameter("videoUrl");

        Lecture lecture = lectureDAO.findLectureById(lectureId)
                .orElseThrow(() -> new ServletException("Lecture not found"));
        
        lecture.setTitle(title);
        lecture.setContent(content);
        lecture.setVideoUrl(videoUrl);
        lectureDAO.updateLecture(lecture);
    }

    private void deleteLecture(HttpServletRequest request) {
        int lectureId = Integer.parseInt(request.getParameter("lectureId"));
        lectureDAO.deleteLecture(lectureId);
    }
}

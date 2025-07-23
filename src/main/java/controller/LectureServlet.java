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
        Integer userId = (Integer) request.getSession().getAttribute("id");
        
        if (role == null || userId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            String lectureIdParam = request.getParameter("lectureId");
            String courseIdParam = request.getParameter("courseId");
            
            if (courseIdParam == null) {
                request.setAttribute("error", "Course ID is required");
                request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
                return;
            }
            
            int courseId = Integer.parseInt(courseIdParam);
            
            // Get all lectures for the course for the sidebar
            List<Lecture> lectures;
            switch (role) {
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
            
            // If lectureId is provided, show specific lecture
            if (lectureIdParam != null) {
                int lectureId = Integer.parseInt(lectureIdParam);
                Lecture currentLecture = lectureDAO.findLectureById(lectureId).orElse(null);
                
                if (currentLecture == null) {
                    request.setAttribute("error", "Lecture not found");
                    request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
                    return;
                }
                
                // Set navigation flags
                boolean hasPrevious = false;
                boolean hasNext = false;
                Integer previousLectureId = null;
                Integer nextLectureId = null;
                
                for (int i = 0; i < lectures.size(); i++) {
                    if (lectures.get(i).getId() == lectureId) {
                        hasPrevious = i > 0;
                        hasNext = i < lectures.size() - 1;
                        if (hasPrevious) {
                            previousLectureId = lectures.get(i - 1).getId();
                        }
                        if (hasNext) {
                            nextLectureId = lectures.get(i + 1).getId();
                        }
                        break;
                    }
                }
                
                request.setAttribute("lecture", currentLecture);
                request.setAttribute("hasPrevious", hasPrevious);
                request.setAttribute("hasNext", hasNext);
                request.setAttribute("previousLectureId", previousLectureId);
                request.setAttribute("nextLectureId", nextLectureId);
                
                // Convert YouTube URL to embed format if needed
                if (currentLecture.getVideoUrl() != null && currentLecture.getVideoUrl().contains("youtube.com/watch")) {
                    String embedUrl = convertToEmbedUrl(currentLecture.getVideoUrl());
                    request.setAttribute("embedUrl", embedUrl);
                }
            } else if (!lectures.isEmpty()) {
                // If no specific lecture, redirect to first lecture
                response.sendRedirect(request.getContextPath() + "/lectures?courseId=" + courseId + "&lectureId=" + lectures.get(0).getId());
                return;
            }
            
            request.setAttribute("lectures", lectures);
            request.setAttribute("role", role);
            request.setAttribute("courseId", courseId);
            request.getRequestDispatcher("/WEB-INF/jsp/lectureDetail.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid course or lecture ID");
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "An error occurred while loading the lecture");
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
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
    
    private String convertToEmbedUrl(String youtubeUrl) {
        if (youtubeUrl != null && youtubeUrl.contains("youtube.com/watch?v=")) {
            String videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
            if (videoId.contains("&")) {
                videoId = videoId.substring(0, videoId.indexOf("&"));
            }
            return "https://www.youtube.com/embed/" + videoId;
        }
        return youtubeUrl;
    }
}

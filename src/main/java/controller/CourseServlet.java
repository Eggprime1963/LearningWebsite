package controller;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import dao.CourseDAO;
import dao.EnrollmentDAO;
import dao.LectureDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.Course;
import model.User;


@WebServlet(name = "CourseServlet", urlPatterns = {"/courses", "/course"})
@MultipartConfig(maxFileSize = 1024 * 1024 * 5) // Giới hạn kích thước file là 5MB
public class CourseServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(CourseServlet.class.getName());
    private final CourseDAO courseDAO = new CourseDAO();
    private final LectureDAO lectureDAO = new LectureDAO();
    private final UserDAO userDAO = new UserDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        // Handle enrollment action
        if ("enroll".equals(action)) {
            handleEnrollment(request, response);
            return;
        }
        
        // Handle course creation (teacher functionality)
        HttpSession session = request.getSession(false);
        if (session == null || !"teacher".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String teacherIdStr = request.getParameter("teacher_id");
        int teacherId = 0;

        // Kiểm tra và parse teacher_id
        if (teacherIdStr == null || teacherIdStr.trim().isEmpty()) {
            request.setAttribute("error", "Teacher ID is missing or invalid.");
            forwardToAddCourses(request, response, session);
            return;
        }
        try {
            teacherId = Integer.parseInt(teacherIdStr);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid Teacher ID format.");
            forwardToAddCourses(request, response, session);
            return;
        }

        // Xử lý file ảnh
        Part filePart = request.getPart("image");
        String imagePath = null;
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = filePart.getSubmittedFileName();
            // Sử dụng đường dẫn tuyệt đối tới thư mục image
            String uploadPath = "/Users/mac/NetBeansProjects/PRJ301/learning_project/src/main/webapp/image/" + fileName;
            logger.info("Attempting to save image to: " + uploadPath);

            // Đảm bảo thư mục image tồn tại
            File uploadDir = new File(uploadPath).getParentFile();
            if (!uploadDir.exists()) {
                logger.info("Creating directory: " + uploadDir.getAbsolutePath());
                if (!uploadDir.mkdirs()) {
                    logger.severe("Failed to create directory: " + uploadDir.getAbsolutePath());
                    request.setAttribute("error", "Failed to create image directory.");
                    forwardToAddCourses(request, response, session);
                    return;
                }
            }

            // Lưu file ảnh
            try {
                filePart.write(uploadPath);
                imagePath = "image/" + fileName; // Đường dẫn tương đối để lưu vào cơ sở dữ liệu
                logger.info("Image saved successfully with path: " + imagePath);
            } catch (IOException e) {
                logger.severe("Error saving image: " + e.getMessage());
                request.setAttribute("error", "Failed to save image: " + e.getMessage());
                forwardToAddCourses(request, response, session);
                return;
            }
        } else {
            logger.warning("No file part or file size is 0 for image.");
            request.setAttribute("error", "Image is required.");
        }

        try {
            if (name == null || name.trim().isEmpty() || description == null || description.trim().isEmpty() || imagePath == null) {
                request.setAttribute("error", "Course name, description, and image are required.");
            } else {
                Course course = new Course(name, description, teacherId, imagePath);
                courseDAO.saveCourse(course);
                request.setAttribute("success", "Course added successfully!");
            }

            forwardToAddCourses(request, response, session);

        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            request.setAttribute("error", "Error: " + e.getMessage());
            forwardToAddCourses(request, response, session);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String courseIdParam = request.getParameter("courseId");
        
        // Handle course detail view
        if (courseIdParam != null) {
            try {
                int courseId = Integer.parseInt(courseIdParam);
                Course course = courseDAO.getCourseById(courseId);
                
                if (course == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
                    return;
                }
                
                // Set course attributes
                request.setAttribute("course", course);
                
                // Set teacher name
                try {
                    User teacher = userDAO.findById(course.getIdTeacher()).orElse(null);
                    if (teacher != null) {
                        course.setTeacherName(teacher.getUsername());
                    }
                } catch (Exception e) {
                    logger.warning("Could not load teacher name: " + e.getMessage());
                    request.setAttribute("error", "Could not load teacher information");
                }
                
                // Get lectures for this course
                List<model.Lecture> lectures = lectureDAO.getLecturesByCourseId(courseId);
                request.setAttribute("lectures", lectures);
                
                // Set lecture count
                course.setLectureCount((long) lectures.size());
                
                // Check if user is enrolled
                HttpSession session = request.getSession(false);
                boolean enrolled = false;
                if (session != null && session.getAttribute("username") != null) {
                    try {
                        String username = (String) session.getAttribute("username");
                        User user = userDAO.findByEmailOrUsername(username).orElse(null);
                        if (user != null) {
                            enrolled = enrollmentDAO.isUserEnrolledInCourse(user.getId(), courseId);
                        }
                    } catch (Exception e) {
                        logger.severe("Error checking enrollment status: " + e.getMessage());
                        request.setAttribute("error", "Could not check enrollment status");
                        enrolled = false;
                    }
                }
                request.setAttribute("enrolled", enrolled);
                
                // Forward to course detail page
                request.getRequestDispatcher("/WEB-INF/jsp/courseDetail.jsp").forward(request, response);
                return;
                
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID");
                return;
            } catch (Exception e) {
                logger.severe("Error loading course details: " + e.getMessage());
                request.setAttribute("error", "Error loading course details: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/jsp/courseDetail.jsp").forward(request, response);
                return;
            }
        }
        
        // Handle teacher course management (original functionality)
        HttpSession session = request.getSession(false);
        if (session == null || !"teacher".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            String username = (String) session.getAttribute("username");
            User user = userDAO.findByEmailOrUsername(username).orElse(null);
            if (user == null) {
                request.setAttribute("error", "User not found.");
                request.getRequestDispatcher("/WEB-INF/jsp/contentDashboard.jsp").forward(request, response);
                return;
            }
            List<Course> courses = courseDAO.getCoursesByTeacher(user.getId());
            request.setAttribute("user", user);
            request.setAttribute("courses", courses);
            request.getRequestDispatcher("/WEB-INF/jsp/contentDashboard.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void forwardToAddCourses(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        try {
            int teacherId = Integer.parseInt((String) session.getAttribute("teacher_id"));
            User user = userDAO.findByEmailOrUsername((String) session.getAttribute("username")).orElse(null);
            List<Course> courses = courseDAO.getCoursesByTeacher(teacherId);
            request.setAttribute("user", user);
            request.setAttribute("courses", courses);
            request.getRequestDispatcher("/WEB-INF/jsp/contentDashboard.jsp").forward(request, response);
        } catch (Exception e) {
            logger.warning("Failed to load courses: " + e.getMessage());
            request.setAttribute("error", "Failed to load courses: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/contentDashboard.jsp").forward(request, response);
        }
    }
    
    private void handleEnrollment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String courseIdParam = request.getParameter("courseId");
        if (courseIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Course ID is required");
            return;
        }
        
        try {
            int courseId = Integer.parseInt(courseIdParam);
            String username = (String) session.getAttribute("username");
            
            logger.info("Processing enrollment for username: " + username + ", courseId: " + courseId);
            
            // Get user and course objects
            User user = userDAO.findByEmailOrUsername(username).orElse(null);
            Course course = courseDAO.getCourseById(courseId);
            
            if (user == null) {
                logger.warning("User not found for username: " + username);
                request.setAttribute("error", "User not found");
                request.getRequestDispatcher("/WEB-INF/jsp/courseDetail.jsp").forward(request, response);
                return;
            }
            
            logger.info("Found user with ID: " + user.getId() + " for username: " + username);
            
            if (course == null) {
                logger.warning("Course not found for courseId: " + courseId);
                request.setAttribute("error", "Course not found");
                request.getRequestDispatcher("/WEB-INF/jsp/courseDetail.jsp").forward(request, response);
                return;
            }
            
            // Check if already enrolled
            if (enrollmentDAO.isUserEnrolledInCourse(user.getId(), courseId)) {
                response.sendRedirect(request.getContextPath() + "/course?courseId=" + courseId + "&message=already_enrolled");
                return;
            }
            
            // Create enrollment
            model.EnrollmentId enrollmentId = new model.EnrollmentId(user.getId(), courseId);
            model.Enrollment enrollment = new model.Enrollment();
            enrollment.setId(enrollmentId);
            enrollment.setStudent(user);
            enrollment.setCourse(course);
            enrollment.setEnrollmentDate(java.time.LocalDateTime.now());
            enrollment.setStatus("active"); // Set default status
            
            // Save enrollment
            enrollmentDAO.save(enrollment);
            
            // Redirect back to course page with success message
            response.sendRedirect(request.getContextPath() + "/course?courseId=" + courseId + "&message=enrolled_successfully");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID");
        } catch (Exception e) {
            logger.severe("Error during enrollment: " + e.getMessage());
            request.setAttribute("error", "Enrollment failed: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/courseDetail.jsp").forward(request, response);
        }
    }
}
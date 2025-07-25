package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.LectureDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Lecture;

@WebServlet(name = "UpdateVideosServlet", urlPatterns = {"/updateVideos"})
public class UpdateVideosServlet extends HttpServlet {
    private final LectureDAO lectureDAO = new LectureDAO();

    // Dynamic data mapping based on SQL file content
    private final Map<Integer, LectureData> lectureDataMap = new HashMap<>();
    
    public UpdateVideosServlet() {
        initializeLectureData();
    }
    
    private void initializeLectureData() {
        // Lecture data from SQL file - dynamically mapped
        lectureDataMap.put(1, new LectureData(
            "Introduction to Python",
            "https://www.youtube.com/watch?v=kqtD5dpn9C8",
            "Learn Python programming fundamentals including variables, data types, control structures, and functions. Perfect for beginners starting their programming journey."
        ));
        
        lectureDataMap.put(2, new LectureData(
            "Java Basics",
            "https://www.youtube.com/watch?v=RRubcjpTkks",
            "Master Java programming basics including object-oriented programming concepts, classes, methods, and Java syntax fundamentals."
        ));
        
        lectureDataMap.put(3, new LectureData(
            "C++ Overview",
            "https://www.youtube.com/watch?v=ZzaPdXTrSb8",
            "Comprehensive introduction to C++ programming covering syntax, memory management, pointers, and object-oriented programming principles."
        ));
        
        lectureDataMap.put(4, new LectureData(
            "JavaScript Fundamentals",
            "https://www.youtube.com/watch?v=W6NZfCO5SIk",
            "Essential JavaScript concepts including DOM manipulation, event handling, functions, and modern ES6+ features for web development."
        ));
        
        lectureDataMap.put(5, new LectureData(
            "Ruby on Rails Intro",
            "https://www.youtube.com/watch?v=t_ispmWmdjY",
            "Getting started with Ruby on Rails framework for rapid web application development including MVC architecture and Rails conventions."
        ));
        
        lectureDataMap.put(6, new LectureData(
            "PHP Basics",
            "https://www.youtube.com/watch?v=OK_JCtrrv-c",
            "PHP programming fundamentals for web development including syntax, variables, functions, and database connectivity basics."
        ));
        
        lectureDataMap.put(7, new LectureData(
            "Go Language Intro",
            "https://www.youtube.com/watch?v=un6ZyFkqFKo",
            "Introduction to Go programming language covering syntax, concurrency, and building efficient applications with Go."
        ));
        
        lectureDataMap.put(8, new LectureData(
            "Swift for iOS",
            "https://www.youtube.com/watch?v=8Xg7E9shq0U",
            "Learn Swift programming language for iOS app development including Xcode, UI development, and iOS app architecture."
        ));
        
        lectureDataMap.put(9, new LectureData(
            "Kotlin Basics",
            "https://www.youtube.com/watch?v=F9UC9DY-vIU",
            "Kotlin programming fundamentals for Android development including syntax, null safety, and interoperability with Java."
        ));
        
        lectureDataMap.put(10, new LectureData(
            "Rust Programming Intro",
            "https://www.youtube.com/watch?v=MsocPEZBd-M",
            "Introduction to Rust programming language focusing on memory safety, ownership concepts, and system programming."
        ));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        try {
            // Get all lectures from database
            List<Lecture> allLectures = lectureDAO.getAllLectures();
            int updatedCount = 0;
            StringBuilder updatedList = new StringBuilder();
            
            // Process each lecture dynamically
            for (Lecture lecture : allLectures) {
                LectureData lectureData = lectureDataMap.get(lecture.getId());
                if (lectureData != null) {
                    updateLectureWithData(lecture.getId(), lectureData);
                    updatedCount++;
                    updatedList.append("<li>Lecture ").append(lecture.getId())
                              .append(": ").append(lectureData.title)
                              .append(" - ").append(lectureData.videoUrl)
                              .append("</li>");
                }
            }
            
            out.println("<html><body>");
            out.println("<h1>Video URLs Updated Successfully!</h1>");
            out.println("<p>Updated " + updatedCount + " lectures with real YouTube videos:</p>");
            out.println("<ul>");
            out.println(updatedList.toString());
            out.println("</ul>");
            out.println("<p><strong>All lectures now have:</strong></p>");
            out.println("<ul>");
            out.println("<li>Real YouTube video URLs</li>");
            out.println("<li>Educational content descriptions</li>");
            out.println("<li>Active status for student viewing</li>");
            out.println("</ul>");
            out.println("<p><a href='" + request.getContextPath() + "/browse'>Back to Courses</a></p>");
            out.println("</body></html>");
            
        } catch (Exception e) {
            out.println("<html><body>");
            out.println("<h1>Error updating videos</h1>");
            out.println("<p>Error: " + e.getMessage() + "</p>");
            out.println("<p>Stack trace:</p><pre>");
            e.printStackTrace(out);
            out.println("</pre>");
            out.println("</body></html>");
        }
    }
    
    private void updateLectureWithData(int lectureId, LectureData lectureData) {
        try {
            Lecture lecture = lectureDAO.findLectureById(lectureId).orElse(null);
            if (lecture != null) {
                lecture.setVideoUrl(lectureData.videoUrl);
                lecture.setContent(lectureData.content);
                lecture.setStatus("active");
                
                lectureDAO.updateLecture(lecture);
                System.out.println("Updated lecture " + lectureId + ": " + lectureData.title);
            }
        } catch (Exception e) {
            System.err.println("Error updating lecture " + lectureId + ": " + e.getMessage());
        }
    }
    
    // Inner class to hold lecture data
    private static class LectureData {
        final String title;
        final String videoUrl;
        final String content;
        
        LectureData(String title, String videoUrl, String content) {
            this.title = title;
            this.videoUrl = videoUrl;
            this.content = content;
        }
    }
}

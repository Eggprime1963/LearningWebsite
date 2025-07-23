package controller;

import java.io.File;// Assuming you have a UserDAO
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import dao.AssignmentDAO;
import dao.SubmissionDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.Assignment;
import model.Submission;
import model.User;

@WebServlet(name = "SubmissionServlet", urlPatterns = {"/submit", "/submission", "/submissions"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
    maxFileSize = 1024 * 1024 * 10,      // 10 MB
    maxRequestSize = 1024 * 1024 * 15   // 15 MB
)
public class SubmissionServlet extends HttpServlet {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final UserDAO userDAO = new UserDAO(); // Assumed
    
    // IMPORTANT: Configure this path for your server environment
    private static final String UPLOAD_DIRECTORY = "C:/path/to/your/uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get Form Data
        int assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
        int studentId = ((User) request.getSession().getAttribute("user")).getId();
        Part filePart = request.getPart("submissionFile");

        // 2. Handle File Upload
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        File uploadDir = new File(UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // Create directory if it doesn't exist
        }
        
        File file = new File(uploadDir, fileName);
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // This URL would be what you store in the DB.
        // In a real app, this would be a web-accessible URL.
        String fileUrl = UPLOAD_DIRECTORY + File.separator + fileName;

        // 3. Create Submission Entity
        try {
            // Fetch the related entities
            Assignment assignment = assignmentDAO.getAssignmentById(assignmentId)
                    .orElseThrow(() -> new ServletException("Assignment not found with ID: " + assignmentId));
            User student = userDAO.findById(studentId)
                    .orElseThrow(() -> new ServletException("Student not found with ID: " + studentId));

            // Create the submission object
            Submission submission = new Submission(assignment, student, fileUrl, LocalDateTime.now());

            // 4. Save Submission using DAO
            submissionDAO.saveSubmission(submission);
            
            // 5. Redirect to a success page
            response.sendRedirect("submissionSuccess.jsp");

        } catch (Exception e) {
            // In case of error, you might want to delete the uploaded file
            Files.deleteIfExists(file.toPath());
            throw new ServletException("Error creating submission record", e);
        }
    }
}
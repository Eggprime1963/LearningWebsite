/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao.CourseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Course;

/**
 *
 * @author mac
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home", "/", "/index", "/dashboard"})
public class HomeServlet extends HttpServlet {
private final CourseDAO courseDAO = new CourseDAO();
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.getRequestDispatcher("/WEB-INF/jsp/homePage.jsp").forward(request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
   @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    

        try {
            // Fetch all courses with their teacher names and lecture counts
            List<Object[]> coursesWithTeachers = courseDAO.getCoursesWithTeacherNames();
            List<Course> courses = new ArrayList<>();
            
            for (Object[] row : coursesWithTeachers) {
                Course course = (Course) row[0];
                String teacherName = (String) row[1];
                Long lectureCount = (Long) row[2];
                course.setTeacherName(teacherName);
                course.setLectureCount(lectureCount);
                courses.add(course);
            }
            
            request.setAttribute("courses", courses);
            request.getRequestDispatcher("/WEB-INF/jsp/homePage.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error loading courses: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/homePage.jsp").forward(request, response);
        }
    }
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

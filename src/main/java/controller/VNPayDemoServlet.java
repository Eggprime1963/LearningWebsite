package controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * VNPay Demo Navigation Servlet
 * Provides access to VNPay official demo
 */
@WebServlet("/vnpay-demo")
public class VNPayDemoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to the VNPay demo JSP page
        request.getRequestDispatcher("/vnpay-demo.jsp").forward(request, response);
    }
}

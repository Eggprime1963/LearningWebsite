package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Test servlet to verify Ollama connectivity
 */
@WebServlet(name = "OllamaTestServlet", urlPatterns = {"/ollama-test", "/ai-test"})
public class OllamaTestServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        response.getWriter().println("<!DOCTYPE html>");
        response.getWriter().println("<html><head><title>Ollama Test</title></head><body>");
        response.getWriter().println("<h1>Ollama Connectivity Test</h1>");
        
        try {
            // Test 1: Check if Ollama service is running
            response.getWriter().println("<h2>Test 1: Service Status</h2>");
            HttpURLConnection connection = (HttpURLConnection) new URI("http://localhost:11434/api/tags").toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                response.getWriter().println("<p style='color: green;'>✅ Ollama service is running</p>");
                
                // Read available models
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder responseBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line);
                    }
                    
                    JSONObject modelsResponse = new JSONObject(responseBody.toString());
                    response.getWriter().println("<p>Available models:</p>");
                    response.getWriter().println("<pre>" + modelsResponse.toString(2) + "</pre>");
                }
            } else {
                response.getWriter().println("<p style='color: red;'>❌ Ollama service not responding (Code: " + responseCode + ")</p>");
            }
            
            // Test 2: Simple generation test
            response.getWriter().println("<h2>Test 2: Simple Generation</h2>");
            String testResponse = testOllamaGeneration("llama3:latest", "Hello! Say 'AI is working' if you can respond.");
            if (testResponse != null && !testResponse.isEmpty()) {
                response.getWriter().println("<p style='color: green;'>✅ Generation test successful</p>");
                response.getWriter().println("<p><strong>Response:</strong> " + testResponse + "</p>");
            } else {
                response.getWriter().println("<p style='color: red;'>❌ Generation test failed</p>");
            }
            
        } catch (Exception e) {
            response.getWriter().println("<p style='color: red;'>❌ Error: " + e.getMessage() + "</p>");
            e.printStackTrace();
        }
        
        response.getWriter().println("<br><a href='/'>Back to Home</a>");
        response.getWriter().println("</body></html>");
    }
    
    private String testOllamaGeneration(String model, String prompt) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("model", model);
            payload.put("prompt", prompt);
            payload.put("stream", false);
            
            HttpURLConnection connection = (HttpURLConnection) new URI("http://localhost:11434/api/generate").toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder responseBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line);
                    }
                    
                    JSONObject jsonResponse = new JSONObject(responseBody.toString());
                    return jsonResponse.optString("response", "No response field found");
                }
            } else {
                return "Error: HTTP " + responseCode;
            }
            
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
}

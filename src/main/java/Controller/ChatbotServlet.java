package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ChatbotServlet", urlPatterns = {"/chatbot", "/ai", "/chat"})
public class ChatbotServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Read user message from request
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        String userMsg = new JSONObject(sb.toString()).optString("message", "");

        // Prepare Ollama API call
        String ollamaUrl = "http://localhost:11434/api/generate";
        JSONObject payload = new JSONObject();
        payload.put("model", "llama3"); // or your preferred model
        payload.put("prompt", userMsg);

        // Send POST request to Ollama
        HttpURLConnection conn = (HttpURLConnection) new URL(ollamaUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes("UTF-8"));
        }

        // Read Ollama response (streaming, so read until "done":true)
        StringBuilder ollamaResponse = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject chunk = new JSONObject(line);
                if (chunk.optBoolean("done", false)) break;
                ollamaResponse.append(chunk.optString("response", ""));
            }
        }

        // Return AI reply as JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject json = new JSONObject();
        json.put("reply", ollamaResponse.toString().trim());
        response.getWriter().write(json.toString());
    }
}
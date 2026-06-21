package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/Game2048Servlet")
public class Game2048Servlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(Game2048Servlet.class);
    private HighScoreDAO highScoreDAO = new HighScoreDAO();

    // Setter for testing
    public void setHighScoreDAO(HighScoreDAO highScoreDAO) {
        this.highScoreDAO = highScoreDAO;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Received POST request to save score");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String playerName = request.getParameter("playerName");
        String scoreStr = request.getParameter("score");

        // Input Validation
        if (playerName == null || playerName.trim().isEmpty() || scoreStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new JSONObject().put("error", "Invalid input parameters").toString());
            out.flush();
            return;
        }

        try {
            int score = Integer.parseInt(scoreStr);
            highScoreDAO.saveScore(playerName, score);
            logger.info("Score saved successfully for player: {}", playerName);
            
            out.print(new JSONObject().put("status", "success").toString());
        } catch (NumberFormatException e) {
            logger.error("Invalid score format", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new JSONObject().put("error", "Score must be a valid number").toString());
        } catch (SQLException e) {
            logger.error("Error saving score", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(new JSONObject().put("error", "Internal server error").toString());
        } finally {
            out.flush();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Received GET request to fetch high scores");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            JSONArray highScores = highScoreDAO.getHighScores(10);
            logger.info("Fetched high scores successfully");
            out.print(highScores.toString());
        } catch (SQLException e) {
            logger.error("Error fetching high scores", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(new JSONObject().put("error", "Internal server error").toString());
        } finally {
            out.flush();
        }
    }
}

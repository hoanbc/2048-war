package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/Game2048Servlet")
public class Game2048Servlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(Game2048Servlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Received POST request to save score");
        String playerName = request.getParameter("playerName");
        int score = Integer.parseInt(request.getParameter("score"));

        String query = "INSERT INTO high_scores (player_name, score) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerName);
            stmt.setInt(2, score);
            stmt.executeUpdate();
            logger.info("Score saved successfully for player: {}", playerName);

        } catch (SQLException e) {
            logger.error("Error saving score", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Received GET request to fetch high scores");
        String query = "SELECT player_name, score FROM high_scores ORDER BY score DESC LIMIT 10";
        JSONArray highScores = new JSONArray();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                JSONObject scoreEntry = new JSONObject();
                scoreEntry.put("player_name", rs.getString("player_name"));
                scoreEntry.put("score", rs.getInt("score"));
                highScores.put(scoreEntry);
            }
            logger.info("Fetched high scores successfully");

        } catch (SQLException e) {
            logger.error("Error fetching high scores", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(highScores);
        out.flush();
    }
}

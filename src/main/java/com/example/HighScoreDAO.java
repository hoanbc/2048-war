package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HighScoreDAO {

    public void saveScore(String playerName, int score) throws SQLException {
        String query = "INSERT INTO high_scores (player_name, score) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerName);
            stmt.setInt(2, score);
            stmt.executeUpdate();
        }
    }

    public JSONArray getHighScores(int limit) throws SQLException {
        String query = "SELECT player_name, score FROM high_scores ORDER BY score DESC LIMIT ?";
        JSONArray highScores = new JSONArray();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JSONObject scoreEntry = new JSONObject();
                    scoreEntry.put("player_name", rs.getString("player_name"));
                    scoreEntry.put("score", rs.getInt("score"));
                    highScores.put(scoreEntry);
                }
            }
        }
        return highScores;
    }
}

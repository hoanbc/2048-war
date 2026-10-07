package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighScoreDAOTest {

    private HighScoreDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        dao = new HighScoreDAO();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS high_scores");
            stmt.execute("CREATE TABLE high_scores ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "player_name VARCHAR(255) NOT NULL, "
                    + "score INT NOT NULL)");
        }
    }

    @Test
    void getHighScoresReturnsEmptyArrayWhenNoData() throws Exception {
        JSONArray result = dao.getHighScores(10);

        assertTrue(result.isEmpty());
    }

    @Test
    void saveScoreThenGetHighScoresReturnsSavedEntry() throws Exception {
        dao.saveScore("Alice", 2048);

        JSONArray result = dao.getHighScores(10);

        assertEquals(1, result.length());
        JSONObject entry = result.getJSONObject(0);
        assertEquals("Alice", entry.getString("player_name"));
        assertEquals(2048, entry.getInt("score"));
    }

    @Test
    void getHighScoresIsOrderedByScoreDescending() throws Exception {
        dao.saveScore("Low", 128);
        dao.saveScore("High", 4096);
        dao.saveScore("Mid", 1024);

        JSONArray result = dao.getHighScores(10);

        assertEquals(3, result.length());
        assertEquals("High", result.getJSONObject(0).getString("player_name"));
        assertEquals("Mid", result.getJSONObject(1).getString("player_name"));
        assertEquals("Low", result.getJSONObject(2).getString("player_name"));
    }

    @Test
    void getHighScoresRespectsLimit() throws Exception {
        for (int i = 1; i <= 15; i++) {
            dao.saveScore("Player" + i, i * 100);
        }

        JSONArray result = dao.getHighScores(10);

        assertEquals(10, result.length());
        assertEquals(1500, result.getJSONObject(0).getInt("score"));
        assertEquals(600, result.getJSONObject(9).getInt("score"));
    }

    @Test
    void saveScoreThrowsSqlExceptionWhenTableMissing() throws Exception {
        dropTable();

        assertThrows(SQLException.class, () -> dao.saveScore("Bob", 512));
    }

    @Test
    void getHighScoresThrowsSqlExceptionWhenTableMissing() throws Exception {
        dropTable();

        assertThrows(SQLException.class, () -> dao.getHighScores(10));
    }

    private void dropTable() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS high_scores");
        }
    }
}

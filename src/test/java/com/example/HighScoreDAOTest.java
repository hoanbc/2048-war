package com.example;

import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HighScoreDAOTest {

    private final HighScoreDAO dao = new HighScoreDAO();

    @Test
    public void testSaveScoreExecutesInsert() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        try (MockedStatic<DatabaseConnection> mocked = mockStatic(DatabaseConnection.class)) {
            mocked.when(DatabaseConnection::getConnection).thenReturn(conn);

            dao.saveScore("Alice", 1024);

            verify(conn).prepareStatement("INSERT INTO high_scores (player_name, score) VALUES (?, ?)");
            verify(stmt).setString(1, "Alice");
            verify(stmt).setInt(2, 1024);
            verify(stmt).executeUpdate();
            // try-with-resources must close the statement and connection
            verify(stmt).close();
            verify(conn).close();
        }
    }

    @Test
    public void testSaveScorePropagatesSqlException() throws Exception {
        try (MockedStatic<DatabaseConnection> mocked = mockStatic(DatabaseConnection.class)) {
            mocked.when(DatabaseConnection::getConnection).thenThrow(new SQLException("connect failed"));

            SQLException ex = assertThrows(SQLException.class, () -> dao.saveScore("Bob", 10));
            assertEquals("connect failed", ex.getMessage());
        }
    }

    @Test
    public void testGetHighScoresReturnsMappedRows() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        // Two rows then stop
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("player_name")).thenReturn("Alice", "Bob");
        when(rs.getInt("score")).thenReturn(2048, 1024);

        try (MockedStatic<DatabaseConnection> mocked = mockStatic(DatabaseConnection.class)) {
            mocked.when(DatabaseConnection::getConnection).thenReturn(conn);

            JSONArray result = dao.getHighScores(10);

            assertEquals(2, result.length());
            assertEquals("Alice", result.getJSONObject(0).getString("player_name"));
            assertEquals(2048, result.getJSONObject(0).getInt("score"));
            assertEquals("Bob", result.getJSONObject(1).getString("player_name"));
            assertEquals(1024, result.getJSONObject(1).getInt("score"));

            verify(stmt).setInt(1, 10);
            verify(stmt).executeQuery();
            verify(rs).close();
            verify(stmt).close();
            verify(conn).close();
        }
    }

    @Test
    public void testGetHighScoresEmptyResult() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DatabaseConnection> mocked = mockStatic(DatabaseConnection.class)) {
            mocked.when(DatabaseConnection::getConnection).thenReturn(conn);

            JSONArray result = dao.getHighScores(5);

            assertEquals(0, result.length());
            verify(stmt).setInt(1, 5);
        }
    }

    @Test
    public void testGetHighScoresPropagatesSqlException() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenThrow(new SQLException("query failed"));

        try (MockedStatic<DatabaseConnection> mocked = mockStatic(DatabaseConnection.class)) {
            mocked.when(DatabaseConnection::getConnection).thenReturn(conn);

            SQLException ex = assertThrows(SQLException.class, () -> dao.getHighScores(10));
            assertEquals("query failed", ex.getMessage());
        }
    }
}

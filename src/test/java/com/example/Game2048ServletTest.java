package com.example;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class Game2048ServletTest {

    private Game2048Servlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HighScoreDAO mockDao;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    public void setUp() throws Exception {
        servlet = new Game2048Servlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        mockDao = mock(HighScoreDAO.class);
        
        servlet.setHighScoreDAO(mockDao);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    public void testDoPostSuccess() throws Exception {
        when(request.getParameter("playerName")).thenReturn("TestPlayer");
        when(request.getParameter("score")).thenReturn("2048");

        servlet.doPost(request, response);

        verify(mockDao).saveScore("TestPlayer", 2048);
        
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("success"));
    }
    
    @Test
    public void testDoPostInvalidScore() throws Exception {
        when(request.getParameter("playerName")).thenReturn("TestPlayer");
        when(request.getParameter("score")).thenReturn("invalid");

        servlet.doPost(request, response);

        verify(mockDao, never()).saveScore(anyString(), anyInt());
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Score must be a valid number"));
    }

    @Test
    public void testDoPostMissingPlayerName() throws Exception {
        when(request.getParameter("playerName")).thenReturn(null);
        when(request.getParameter("score")).thenReturn("2048");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Invalid input parameters"));
    }

    @Test
    public void testDoPostEmptyPlayerName() throws Exception {
        when(request.getParameter("playerName")).thenReturn("   ");
        when(request.getParameter("score")).thenReturn("2048");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Invalid input parameters"));
    }

    @Test
    public void testDoPostMissingScore() throws Exception {
        when(request.getParameter("playerName")).thenReturn("TestPlayer");
        when(request.getParameter("score")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Invalid input parameters"));
    }

    @Test
    public void testDoPostSqlException() throws Exception {
        when(request.getParameter("playerName")).thenReturn("TestPlayer");
        when(request.getParameter("score")).thenReturn("2048");

        doThrow(new SQLException("DB error")).when(mockDao).saveScore(anyString(), anyInt());

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Internal server error"));
    }

    @Test
    public void testDoGetSuccess() throws Exception {
        JSONArray mockArray = new JSONArray();
        mockArray.put(new JSONObject().put("player_name", "TestPlayer").put("score", 2048));
        when(mockDao.getHighScores(10)).thenReturn(mockArray);

        servlet.doGet(request, response);

        printWriter.flush();
        String responseContent = stringWriter.toString();

        assertTrue(responseContent.contains("TestPlayer"));
        assertTrue(responseContent.contains("2048"));
        
        verify(response).setContentType("application/json");
    }

    @Test
    public void testDoGetSqlException() throws Exception {
        when(mockDao.getHighScores(anyInt())).thenThrow(new SQLException("DB error"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Internal server error"));
    }
}

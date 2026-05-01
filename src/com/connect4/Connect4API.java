package com.connect4;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Connect4API {
    private static final String BASE_URL = System.getProperty("connect4.url", "http://localhost:8080");

    public static Response newGame(String user) {
        return makeRequest("/newgame?user=" + encode(user));
    }

    public static Response move(String user, int column) {
        return makeRequest("/move?user=" + encode(user) + "&column=" + column);
    }

    public static Response status(String user) {
        return makeRequest("/status?user=" + encode(user));
    }

    public static Response listStrats() {
        return makeRequest("/liststrats");
    }

    public static Response setStrat(String user, String strategy) {
        return makeRequest("/setstrat?user=" + encode(user) + "&strategy=" + encode(strategy));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Response makeRequest(String endpoint) {
        Response response = new Response();
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            response.statusCode = conn.getResponseCode();
            InputStream is = (response.statusCode >= 200 && response.statusCode < 300) 
                             ? conn.getInputStream() : conn.getErrorStream();

            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                response.status = json.optString("status");
                response.message = json.optString("message");
                
                if (json.has("board")) {
                    JSONArray boardArray = json.getJSONArray("board");
                    response.board = new int[6][7];
                    for (int i = 0; i < 6; i++) {
                        JSONArray row = boardArray.getJSONArray(i);
                        for (int j = 0; j < 7; j++) {
                            response.board[i][j] = row.getInt(j);
                        }
                    }
                }

                response.gameOver = json.optBoolean("gameOver", false);
                response.winner = json.optString("winner", null);
                
                if (json.has("strategies")) {
                    JSONArray stratsArray = json.getJSONArray("strategies");
                    List<String> strategies = new ArrayList<>();
                    for (int i = 0; i < stratsArray.length(); i++) {
                        strategies.add(stratsArray.getString(i));
                    }
                    response.strategies = strategies;
                }

                String playerSym = json.optString("playerSymbol", "");
                if (!playerSym.isEmpty()) response.playerSymbol = playerSym.charAt(0);
                
                String serverSym = json.optString("serverSymbol", "");
                if (!serverSym.isEmpty()) response.serverSymbol = serverSym.charAt(0);

                if (json.has("playerMove") && !json.isNull("playerMove")) {
                    response.playerMove = json.getInt("playerMove");
                }
                if (json.has("serverMove") && !json.isNull("serverMove")) {
                    response.serverMove = json.getInt("serverMove");
                }
            }
        } catch (Exception e) {
            response.status = "error";
            response.message = "Network error: " + e.getMessage();
        }
        return response;
    }
}

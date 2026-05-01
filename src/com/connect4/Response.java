package com.connect4;

import java.util.List;

public class Response {
    public int statusCode;
    public String status;
    public String message;
    public int[][] board;
    public boolean gameOver;
    public String winner;
    public List<String> strategies;
    public char playerSymbol;
    public char serverSymbol;
    public Integer playerMove;
    public Integer serverMove;

    public Response() {}
}

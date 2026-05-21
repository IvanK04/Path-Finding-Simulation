package com.example.backend.dto;

import lombok.Data;

@Data
public class PathfindingRequest {
    private String algorithm;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int width;
    private int height;
    private int[][] walls;
    private boolean allowDiagonal;
}

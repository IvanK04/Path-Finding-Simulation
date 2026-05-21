package com.example.backend.model;

import lombok.Data;

@Data
public class Node {
    private int x;
    private int y;
    private double gScore;
    private double fScore;
    private double hScore;
    private boolean isWall;
    private Node parent;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.gScore = Double.MAX_VALUE;
        this.fScore = Double.MAX_VALUE;
        this.hScore = Double.MAX_VALUE;
        this.isWall = false;
        this.parent = null;
    }

}

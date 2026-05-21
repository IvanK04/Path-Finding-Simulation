package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.model.*;

@Service
public class HeuristicCalculator {
    public static double manhattan(Node a, Node b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    public static double euclidean(Node a, Node b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }

    public static double chebyshev(Node a, Node b) {
        return Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY()));
    }

    public static double octile(Node a, Node b) {
        double dx = Math.abs(a.getX() - b.getX());
        double dy = Math.abs(a.getY() - b.getY());

        return 1.0 * (dx + dy) + (1.414 - 2 * 1.0) * Math.min(dx, dy);
    }

}

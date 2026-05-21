package com.example.backend.service;

import com.example.backend.model.*;
import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class AStarService {

    private boolean isDiagonal(int[] dir) {
        return dir[0] != 0 && dir[1] != 0;
    }

    private int[][] getDirections(boolean allowDiagonal) {
        if (allowDiagonal) {
            return new int[][] { { -1, 0 }, { 1, 0 }, { 0, 1 }, { 0, -1 }, { -1, 1 }, { -1, -1 }, { 1, 1 }, { 1, -1 } };
        } else {
            return new int[][] { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
        }
    }

    private List<Node> reconstructPath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    public List<Node> runAStar(Node startNode, Node endNode, Node[][] nodeGrid, boolean allowDiagonal) {
        int height = nodeGrid.length;
        int width = nodeGrid[0].length;
        int[][] directions = getDirections(allowDiagonal);
        PriorityQueue<Node> openList = new PriorityQueue<>((a, b) -> Double.compare(a.getFScore(), b.getFScore()));

        Set<Node> closedSet = new HashSet<>();

        startNode.setGScore(0);
        if (allowDiagonal) {
            startNode.setFScore(HeuristicCalculator.octile(startNode, endNode));

        } else {
            startNode.setFScore(HeuristicCalculator.manhattan(startNode, endNode));

        }
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.getX() == endNode.getX() && current.getY() == endNode.getY()) {
                return reconstructPath(current);
            }
            closedSet.add(current);

            for (int[] dir : directions) {
                int nextX = current.getX() + dir[0];
                int nextY = current.getY() + dir[1];

                if (nextX < 0 || nextX >= width || nextY < 0 || nextY >= height) {
                    continue;
                }

                Node neighbor = nodeGrid[nextY][nextX];

                if (neighbor.isWall() || closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGScore = current.getGScore() + (isDiagonal(dir) ? 1.414 : 1.0);

                if (tentativeGScore < neighbor.getGScore()) {
                    if (openList.contains(neighbor)) {
                        openList.remove(neighbor);
                    }
                    neighbor.setParent(current);
                    neighbor.setGScore(tentativeGScore);
                    if (allowDiagonal) {
                        neighbor.setFScore(tentativeGScore + HeuristicCalculator.octile(neighbor, endNode));
                    } else {
                        neighbor.setFScore(tentativeGScore + HeuristicCalculator.manhattan(neighbor, endNode));

                    }

                    openList.add(neighbor);
                }
            }
        }

        return new ArrayList<>();
    }

}

package com.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.backend.dto.AlgorithmEvent;
import com.example.backend.model.*;
import java.util.*;

@Service
public class DFSService {

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

    public List<Node> runDFS(Node startNode, Node endNode, Node[][] nodeGrid, boolean allowDiagonal) {
        int height = nodeGrid.length;
        int width = nodeGrid[0].length;

        int[][] directions = getDirections(allowDiagonal);

        Stack<Node> stack = new Stack<>();
        boolean[][] visited = new boolean[height][width];

        stack.push(startNode);
        visited[startNode.getY()][startNode.getX()] = true;
        while (!stack.isEmpty()) {
            Node current = stack.pop();

            if (current.getX() == endNode.getX() && current.getY() == endNode.getY()) {
                return reconstructPath(current);
            }

            for (int[] dir : directions) {
                int nextX = current.getX() + dir[0];
                int nextY = current.getY() + dir[1];

                if (nextX < 0 || nextX >= width || nextY < 0 || nextY >= height) {
                    continue;
                }

                Node neighbor = nodeGrid[nextY][nextX];

                if (neighbor.isWall() || visited[nextY][nextX])
                    continue;

                visited[nextY][nextX] = true;

                neighbor.setParent(current);
                stack.push(neighbor);
            }
        }

        return new ArrayList<>();

    }

    public void runDFSRealTime(Node startNode, Node endNode, Node[][] nodeGrid, boolean allowDiagonal,
            SseEmitter emitter) {
        int height = nodeGrid.length;
        int width = nodeGrid[0].length;
        int[][] directions = getDirections(allowDiagonal);

        Deque<Node> stack = new ArrayDeque<>();
        boolean[][] visited = new boolean[height][width];

        stack.push(startNode);

        try {
            while (!stack.isEmpty()) {
                Node current = stack.pop();

                if (current.getX() == endNode.getX() && current.getY() == endNode.getY()) {
                    List<Node> path = reconstructPath(current);
                    for (Node pathNode : path) {
                        if (pathNode != startNode && pathNode != endNode) {
                            emitter.send(SseEmitter.event().name("algorithm-step")
                                    .data(new AlgorithmEvent("PATH", pathNode.getX(), pathNode.getY())));
                            Thread.sleep(30);
                        }
                    }
                    emitter.send(SseEmitter.event().name("algorithm-step")
                            .data(new AlgorithmEvent("DONE", endNode.getX(), endNode.getY())));
                    return;
                }

                if (!visited[current.getY()][current.getX()]) {
                    visited[current.getY()][current.getX()] = true;
                    if (current != startNode) {
                        emitter.send(SseEmitter.event().name("algorithm-step")
                                .data(new AlgorithmEvent("EXPLORED", current.getX(), current.getY())));
                        Thread.sleep(20);
                    }

                    for (int[] dir : directions) {
                        int nextX = current.getX() + dir[0];
                        int nextY = current.getY() + dir[1];

                        if (nextX < 0 || nextX >= width || nextY < 0 || nextY >= height) {
                            continue;
                        }

                        Node neighbor = nodeGrid[nextY][nextX];

                        if (neighbor.isWall() || visited[nextY][nextX]) {
                            continue;
                        }

                        neighbor.setParent(current);
                        stack.push(neighbor);
                    }
                }
            }
            emitter.send(SseEmitter.event().name("algorithm-step")
                    .data(new AlgorithmEvent("DONE", 0, 0)));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}

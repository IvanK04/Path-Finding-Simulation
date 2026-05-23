package com.example.backend.service;

import com.example.backend.dto.AlgorithmEvent;
import com.example.backend.model.*;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class MazeService {
    private void addNeighbors(int x, int y, int width, int height, List<int[]> wallList, Node[][] nodeGrid) {
        int[][] dirs = { { 0, 2 }, { 0, -2 }, { 2, 0 }, { -2, 0 } };
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (nx >= 0 && nx < width - 1 && ny >= 0 && ny < height - 1) {
                if (nodeGrid[ny][nx].isWall()) {
                    wallList.add(new int[] { nx, ny, x, y });
                }
            }
        }
    }

    public void generateMazeRealTime(int width, int height, Node nodeGrid[][], boolean allowDiagonal,
            SseEmitter emitter) {
        Random rand = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodeGrid[y][x].setWall(true);
                try {
                    emitter.send(SseEmitter.event().name("maze-step").data(new AlgorithmEvent("WALL", x, y)));
                } catch (Exception ignored) {
                }
            }
        }

        List<int[]> wallsLists = new ArrayList<>();

        int startX = 1;
        int startY = 1;

        nodeGrid[startY][startX].setWall(false);

        try {
            emitter.send(SseEmitter.event().name("maze-step").data(new AlgorithmEvent("EMPTY", startX, startY)));
            Thread.sleep(5);
        } catch (Exception ignored) {
        }

        addNeighbors(startX, startY, width, height, wallsLists, nodeGrid);

        try {
            while (!wallsLists.isEmpty()) {
                int randomIndex = rand.nextInt(wallsLists.size());
                int[] wall = wallsLists.remove(randomIndex);

                int x = wall[0];
                int y = wall[1];
                int px = wall[2];
                int py = wall[3];

                if (nodeGrid[y][x].isWall()) {
                    nodeGrid[y][x].setWall(false);
                    nodeGrid[(y + py) / 2][(x + px) / 2].setWall(false);

                    emitter.send(SseEmitter.event().name("maze-step").data(new AlgorithmEvent("EMPTY", x, y)));
                    emitter.send(SseEmitter.event().name("maze-step")
                            .data(new AlgorithmEvent("EMPTY", (x + px) / 2, (y + py) / 2)));

                    addNeighbors(x, y, width, height, wallsLists, nodeGrid);
                    Thread.sleep(10);
                }
            }
            emitter.send(SseEmitter.event().name("maze-step").data(new AlgorithmEvent("DONE", 0, 0)));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

    }
}

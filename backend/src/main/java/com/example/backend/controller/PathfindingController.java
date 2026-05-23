package com.example.backend.controller;

import com.example.backend.model.*;
import com.example.backend.service.*;
import com.example.backend.dto.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/pathfinding")
@CrossOrigin(origins = { "http://localhost:5173/", "https://striking-hope-production-4b5d.up.railway.app/" })
public class PathfindingController {

    @Autowired
    private AStarService aStarService;

    @Autowired
    private DijkstraService dijkstraService;

    @Autowired
    private BFSService bfsService;

    @Autowired
    private DFSService dfsService;

    @Autowired
    private MazeService mazeService;

    @PostMapping("/solve")
    public ResponseEntity<List<Node>> findPath(@RequestBody PathfindingRequest request) {
        int height = request.getHeight();
        int width = request.getWidth();

        Node[][] nodeGrid = new Node[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodeGrid[y][x] = new Node(x, y);
            }
        }

        if (request.getWalls() != null) {
            for (int[] wall : request.getWalls()) {
                int wX = wall[0];
                int wY = wall[1];

                if (wX >= 0 && wX < width && wY >= 0 && wY < height) {
                    nodeGrid[wY][wX].setWall(true);
                }
            }
        }

        if (request.getStartX() < 0 || request.getStartX() >= width || request.getStartY() < 0
                || request.getStartY() >= height ||
                request.getEndX() < 0 || request.getEndX() >= width || request.getEndY() < 0
                || request.getEndY() >= height) {
            return ResponseEntity.badRequest().body(null);
        }

        Node startNode = nodeGrid[request.getStartY()][request.getStartX()];
        Node endNode = nodeGrid[request.getEndY()][request.getEndX()];

        List<Node> resultPath = new ArrayList<>();
        String algo = request.getAlgorithm().toUpperCase();

        switch (algo) {
            case "ASTAR": {
                resultPath = aStarService.runAStar(startNode, endNode, nodeGrid, request.isAllowDiagonal());
                break;
            }
            case "DIJKSTRA": {
                resultPath = dijkstraService.runDijkstra(startNode, endNode, nodeGrid, request.isAllowDiagonal());
                break;
            }
            case "BFS": {
                resultPath = bfsService.runBFS(startNode, endNode, nodeGrid, request.isAllowDiagonal());
                break;
            }
            case "DFS": {
                resultPath = dfsService.runDFS(startNode, endNode, nodeGrid, request.isAllowDiagonal());
                break;
            }
            default:
                return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(resultPath);
    }

    @GetMapping(value = "/solve-realtime", produces = "text/event-stream")
    public SseEmitter findPathRealTime(
            @RequestParam String algorithm,
            @RequestParam int startX, @RequestParam int startY,
            @RequestParam int endX, @RequestParam int endY,
            @RequestParam int width, @RequestParam int height,
            @RequestParam boolean allowDiagonal,
            @RequestParam(required = false) String wallsStr) {

        SseEmitter emitter = new SseEmitter(600000L);

        if (startX < 0 || startX >= width || startY < 0 || startY >= height ||
                endX < 0 || endX >= width || endY < 0 || endY >= height) {
            try {
                emitter.send(SseEmitter.event().name("error").data("Tọa độ Start/End vượt quá kích thước bản đồ"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        Node[][] nodeGrid = new Node[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodeGrid[y][x] = new Node(x, y);
            }
        }

        if (wallsStr != null && !wallsStr.isEmpty()) {
            for (String w : wallsStr.split("-")) {
                String[] tokens = w.split(",");
                int wx = Integer.parseInt(tokens[0]);
                int wy = Integer.parseInt(tokens[1]);
                if (wx >= 0 && wx < width && wy >= 0 && wy < height) {
                    nodeGrid[wy][wx].setWall(true);
                }
            }
        }

        Node startNode = nodeGrid[startY][startX];
        Node endNode = nodeGrid[endY][endX];
        String algo = algorithm.toUpperCase();

        new Thread(() -> {
            try {
                switch (algo) {
                    case "ASTAR" -> aStarService.runAStarRealTime(startNode, endNode, nodeGrid, allowDiagonal, emitter);
                    case "DIJKSTRA" ->
                        dijkstraService.runDijkstraRealTime(startNode, endNode, nodeGrid, allowDiagonal, emitter);
                    case "BFS" -> bfsService.runBFSRealTime(startNode, endNode, nodeGrid, allowDiagonal, emitter);
                    case "DFS" -> dfsService.runDFSRealTime(startNode, endNode, nodeGrid, allowDiagonal, emitter);
                    default -> emitter.send(SseEmitter.event().name("error").data("Thuật toán không hợp lệ"));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    @GetMapping(value = "/generate-maze", produces = "text/event-stream")
    public SseEmitter generateMazeRealTime(@RequestParam int width, int height) {
        SseEmitter emitter = new SseEmitter(600000L);

        Node[][] nodeGrid = new Node[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodeGrid[y][x] = new Node(x, y);
            }
        }

        new Thread(() -> {
            try {
                mazeService.generateMazeRealTime(width, height, nodeGrid, false, emitter);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

}
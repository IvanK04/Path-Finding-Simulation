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

@RestController
@RequestMapping("/api/pathfinding")
@CrossOrigin(origins = "*")
public class PathfindingController {

    @Autowired
    private AStarService aStarService;

    @Autowired
    private DijkstraService dijkstraService;

    @Autowired
    private BFSService bfsService;

    @Autowired
    private DFSService dfsService;

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
}

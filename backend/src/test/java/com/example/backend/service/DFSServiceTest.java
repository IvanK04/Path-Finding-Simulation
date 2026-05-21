package com.example.backend.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.backend.model.*;;

@SpringBootTest
public class DFSServiceTest {
    @Autowired
    private DFSService dfsService;

    @Test
    public void testDFS() {
        int height = 10;
        int width = 10;

        Node[][] nodeGrid = new Node[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodeGrid[y][x] = new Node(x, y);
            }
        }

        nodeGrid[1][0].setWall(true);

        nodeGrid[0][2].setWall(true);
        nodeGrid[1][2].setWall(true);
        nodeGrid[3][2].setWall(true);
        nodeGrid[4][2].setWall(true);

        Node startNode = nodeGrid[0][0];
        Node endNode = nodeGrid[9][9];

        boolean allowDiagonal = true;
        List<Node> resultPath = dfsService.runDFS(startNode, endNode, nodeGrid, allowDiagonal);

        System.out.print("\n === Simulation result ===\n");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == startNode.getX() && y == startNode.getY())
                    System.out.print("[S] ");
                else if (x == endNode.getX() && y == endNode.getY())
                    System.out.print("[E] ");
                else if (nodeGrid[y][x].isWall())
                    System.out.print("[W] ");
                else if (resultPath.contains(nodeGrid[y][x]))
                    System.out.print("[*] ");
                else
                    System.out.print("[ ] ");
            }
            System.out.println();
        }
        System.out.print("==========================================\n");
        System.out.print("Total nodes for shortest path: " + resultPath.size() + "\n");
    }
}

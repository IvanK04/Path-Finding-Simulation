package com.example.backend.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.backend.model.*;

@SpringBootTest
public class AStarServiceTest {

    @Autowired
    private AStarService aStarService;

    @Test
    public void testAStar() {
        int height = 10;
        int width = 10;
        Node[][] nodeGrid = new Node[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodeGrid[y][x] = new Node(x, y);
            }
        }

        // nodeGrid[1][0].setWall(true);

        // nodeGrid[0][2].setWall(true);
        // nodeGrid[1][2].setWall(true);
        // nodeGrid[3][2].setWall(true);
        // nodeGrid[4][2].setWall(true);

        // nodeGrid[3][3].setWall(true);
        // nodeGrid[4][3].setWall(true);
        // nodeGrid[5][3].setWall(true);

        // nodeGrid[0][4].setWall(true);
        // nodeGrid[1][4].setWall(true);
        // nodeGrid[2][4].setWall(true);
        // nodeGrid[3][4].setWall(true);

        // nodeGrid[5][7].setWall(true);
        // nodeGrid[6][7].setWall(true);
        // nodeGrid[8][7].setWall(true);
        // nodeGrid[7][7].setWall(true);
        // nodeGrid[9][7].setWall(true);

        Node startNode = nodeGrid[0][0];
        Node endNode = nodeGrid[0][9];

        boolean allowDiagonal = true;
        List<Node> resultPath = aStarService.runAStar(startNode, endNode, nodeGrid, allowDiagonal);

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

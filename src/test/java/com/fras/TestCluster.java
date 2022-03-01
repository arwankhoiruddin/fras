package com.fras;

import cluster.*;
import common.Functions;
import common.MRConfigs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestCluster {

    @BeforeAll
    public static void setUp() {
        Cluster cluster = new Cluster();
    }

    @AfterAll
    public static void tearDown() {

    }

    @Test
    public void testAddBlock() {
        Block block = new Block(1);
        Cluster.nodes[0].addBlock(block);
        assert Cluster.nodes[0].getDisk().getBlocks().getFirst().getUserID() == 1;
    }

    @Test
    public void testDiskInit() {
        Disk disk = new Disk(SataType.SATA1, 100);
        assert disk.getDiskSpeed() == 150;
        assert disk.getDiskSpace() == 100;
    }

    @Test
    public void testNodeInit() {
        Node node = new Node(1, 4, 2, new Disk(SataType.SATA3, 160));
        assert node.getNodeID() == 1;
        assert node.getCpu() == 2;
        assert node.getRam() == 4;
        assert node.getDisk().getDiskSpace() == 160;
        assert node.getDisk().getDiskSpeed() == 600;
    }

    @Test
    public void testUserInit() {
        User user = new User(1, 10);
        user.setCpuLoad(5);
        assert user.getUserID() == 1;
        assert user.getDataSize() == 10;
        assert user.getCpuLoad() == 5;
    }

    @Test
    public void testClusterInit() {
        Cluster cluster = new Cluster();
        assert Cluster.nodes.length == MRConfigs.numNodes;
    }

    @Test
    public void testGetNumberOfBlocks() {
        int dataSize = 10;
        int numBlocks = Functions.getNumberOfBlocks(dataSize);
        assert (numBlocks == 160);
    }

    @Test
    public void testMatrixConvolution() {
        int[][] b = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}; // kernel
        int[][] a = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        int[][] res = {{9, 18, 27}, {36, 45, 54}, {63, 72, 81}};

        int[][] conv = Functions.convolution(a, b);
        assert (Arrays.deepEquals(res, conv));
    }

    @Test
    public void testSumConvolution() {
        int[][] a = {{1, 4, 5}, {1, 1, 8}, {1, 8, 9}};
        int[][] res = {{9, 18, 27}, {36, 45, 54}, {63, 72, 81}};

        int[][] c = Functions.convolution(a);
        int[][] conv = Functions.convolution(c);
        Functions.printArray(conv);
        assert (Arrays.deepEquals(res, conv));
    }
}

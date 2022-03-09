package com.fras;

import cluster.*;
import common.Functions;
import common.MRConfigs;
import mapreduce.Block;
import mapreduce.HeartBeat;
import mapreduce.Job;
import mapreduce.Parity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

public class TestCluster {

    @BeforeAll
    public static void setUp() {
        Cluster cluster = new Cluster();
    }

    @Test
    public void testFreeRam() {
        for (int i=0; i< MRConfigs.vCpuPerNodes; i++) {
            Cluster.nodes[0].addJob(new Job(0, 0, 0.5, 0.3));
        }
        Cluster.nodes[0].runJob();
        assert Cluster.nodes[0].getFreeRam() == 14;
    }

    @Test
    public void testFreeCPU() {
        Job job = new Job(0, 0, 0.7, 0.1);
        // add 4 jobs with similar characteristics
        for (int i=0; i<4; i++) {
            Cluster.nodes[1].addJob(job);
        }
        // run the jobs
        Cluster.nodes[1].runJob();
        double[] freeCPU = Cluster.nodes[1].getFreeCPU();
        assert freeCPU[1] == 0.9;
    }

    @Test 
    public void testNodeReachable() {
        Node n = new Node(1, 4, 4, new Disk(SataType.SATA1, 100));
        n.setReachable(false);
        assert !n.isReachable();
    }

    @Test
    public void testNodeInit() {
        Node node = new Node(1, 4, 2, new Disk(SataType.SATA3, 100));
        assert node.getNodeID() == 1;
        assert node.getCpu() == 2;
        assert node.getRam() == 4;
        assert node.getDisk().getDiskSpace() == 102400;
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

}

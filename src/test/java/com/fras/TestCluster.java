package com.fras;

import cluster.*;
import common.Functions;
import common.MRConfigs;
import mapreduce.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

public class TestCluster {

    @BeforeAll
    public static void setUp() {
        new Cluster();
    }

    @Test
    public void testFreeRam() {
        for (int i=0; i< MRConfigs.vCpuPerNodes; i++) {
            Cluster.nodes[0].addJob(new Job(0, 0, 0.5, 0.3, 20));
        }
        Cluster.nodes[0].runJob();
        assert Cluster.nodes[0].getFreeRam() == 14;
    }

    @Test
    public void testFreeCPU() {
        Job job = new Job(0, 0, 0.7, 0.1, 20);
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

    @Test
    public void testSendBlock() {
        // send block to other node
        Cluster.nodes[0].sendData(1, new Block(1));
        assert Cluster.nodes[1].getDisk().getBlocks().getLast().getUserID() == 1;

        // send other block from other user
        Cluster.nodes[0].sendData(1, new Block(0));
        assert Cluster.nodes[1].getDisk().getBlocks().getLast().getUserID() == 0;
    }

    @Test
    public void testSendParity() {
        Cluster.nodes[0].sendData(1, new Parity(1));
        assert Cluster.nodes[1].getDisk().getParity().getLast().getUserID() == 1;

        // send other parity from other user
        Cluster.nodes[0].sendData(1, new Parity(2));
        assert Cluster.nodes[1].getDisk().getParity().getLast().getUserID() == 2;
    }

    @Test
    public void testSendIntermediary() {
        Intermediary intermediary = new Intermediary(1, 10);
        Cluster.nodes[0].sendData(1, intermediary);
    }

    @Test
    public void testDistributeBlocks() {
        Cluster.nodes[0].sendData(1, new Block(1));
        Cluster.nodes[1].sendData(2, Cluster.nodes[1].getDisk().getBlocks().getLast());
        assert Cluster.nodes[1].getDisk().getBlocks().getLast().getUserID() == 1;
    }

    @Test
    public void testTimeLine4Map() {
        int numNode = 0;
        int numVCore = 0;
        Mapper job = new Mapper(0, 0, 0.5, 0.5, 20);
        Cluster.nodes[0].addJob(job);
        Cluster.nodes[0].runJob();
        assert Time.times.get(numNode).get(numVCore).size() == 20;
        assert Time.times.get(numNode).get(numVCore).get(0) == Status.RUN_MAP;
    }

    @Test
    public void testTimeLine4Reducer() {
        int numNode = 0;
        int numVCore = 0;
        Reducer job = new Reducer(0, 0, 0.5, 0.5, 20);
        Cluster.nodes[0].addJob(job);
        Cluster.nodes[0].runJob();
        assert Time.times.get(numNode).get(numVCore).size() == 20;
        assert Time.times.get(numNode).get(numVCore).get(0) == Status.RUN_REDUCE;
    }

    @Test
    public void testTimeLine4Shuffle() {
        int numNode = 0;
        int numVCore = 0;
        Shuffle job = new Shuffle(0, 0, 0.5, 0.5, 20);
        Cluster.nodes[0].addJob(job);
        Cluster.nodes[0].runJob();
        assert Time.times.get(numNode).get(numVCore).size() == 20;
        assert Time.times.get(numNode).get(numVCore).get(0) == Status.RUN_SHUFFLE;
    }

    @Test
    public void testTimeLine4Sort() {
        int numNode = 0;
        int numVCore = 0;
        Sort job = new Sort(0, 0, 0.5, 0.5, 20);
        Cluster.nodes[0].addJob(job);
        Cluster.nodes[0].runJob();
        assert Time.times.get(numNode).get(numVCore).size() == 20;
        assert Time.times.get(numNode).get(numVCore).get(0) == Status.RUN_SORT;
    }
}

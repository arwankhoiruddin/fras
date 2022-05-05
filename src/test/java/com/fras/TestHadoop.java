package com.fras;

import cluster.*;
import common.Log;
import common.MRConfigs;
import fifo.FifoScheduler;
import fras.BlockPlacementStrategy;
import fras.FRAS;
import late.LATEScheduler;
import mapreduce.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TestHadoop {

    @Test
    public void testHeartBeat() {
        new Cluster();
        HeartBeat heartBeat = new HeartBeat();
        heartBeat.sendHeartBeat();

        // now we can get the status of each node when the heartbeat is sent
    }

    @Test
    public void testHDFSPut() {
        new Cluster();
        User user = new User(0, 10);
        assert Cluster.nodes[1].getDisk().getBlocks().size() == 0;
        HDFS.put(0);
        assert Cluster.nodes[1].getDisk().getBlocks().size() > 0;
    }

    @Test
    public void testScheduleJobsFIFO() {
        new Cluster();
        LinkedList<Job> jobs = new LinkedList<Job>();
        Job job1 = new Job(0, 0, 0.5, 0.7,100);
        Job job2 = new Job(1, 0, 0.3, 0.5, 40);
        Job job3 = new Job(2, 0, 0.8, 0.3, 60);

        jobs.add(job1);
        jobs.add(job2);
        jobs.add(job3);

        MRConfigs.scheduler = new FifoScheduler();
        LinkedList<Job> scheduled = MRConfigs.scheduler.scheduleJob(jobs);

        assert scheduled.get(0).getJobLength() == 100;
        assert scheduled.get(1).getJobLength() == 40;
        assert scheduled.get(2).getJobLength() == 60;
    }

    @Test
    public void testScheduleJobsLate() {
        new Cluster();
        LinkedList<Job> jobs = new LinkedList<Job>();
        Job job1 = new Job(0, 0, 0.5, 0.7,100);
        Job job2 = new Job(1, 0, 0.3, 0.5, 40);
        Job job3 = new Job(2, 0, 0.8, 0.3, 60);

        jobs.add(job1);
        jobs.add(job2);
        jobs.add(job3);

        MRConfigs.scheduler = new LATEScheduler();
        LinkedList<Job> scheduled = MRConfigs.scheduler.scheduleJob(jobs);

        assert scheduled.get(0).getJobLength() == 40;
        assert scheduled.get(1).getJobLength() == 60;
        assert scheduled.get(2).getJobLength() == 100;
    }

    @Test
    public void testScheduleJobsFRAS() {
        new Cluster();
        LinkedList<Job> jobs = new LinkedList<Job>();
        Job job1 = new Job(0, 0, 0.5, 0.7,100);
        Job job2 = new Job(1, 0, 0.3, 0.5, 40);
        Job job3 = new Job(2, 0, 0.8, 0.3, 60);

        jobs.add(job1);
        jobs.add(job3);
        jobs.add(job2);

        MRConfigs.scheduler = new FRAS();
        LinkedList<Job> scheduled = MRConfigs.scheduler.scheduleJob(jobs);

        assert scheduled.get(0).getJobLength() == 60;
        assert scheduled.get(1).getJobLength() == 40;
        assert scheduled.get(2).getJobLength() == 100;

    }

    @Test
    public void testMap() {
        new Cluster();
        Map<Integer, Double> val = new HashMap<>();
        val.put(0, 0.9);
        val.put(1, 0.7);

        val.entrySet().stream().sorted(Map.Entry.<Integer, Double> comparingByValue())
                .forEach(System.out::println);

    }

    @Test
    public void testReverse() {
        for (int i=9; i>=0; i--) {
            Log.debug(i);
        }
    }

    @Test
    public void testFindNodeInSameRack() {
        Switch aSwitch = new Switch(0, LinkType.GIGABIT);
        Cluster.nodes = new Node[4];
        for (int i=0; i<Cluster.nodes.length; i++) {
            Cluster.nodes[i] = new Node(i, 1, 4, new Disk(SataType.SATA1, 50));
            Cluster.nodes[i].connectSwitch(aSwitch);
        }

        for (int i=0; i<Cluster.nodes.length; i++) {
            int otherNodeInRack = HDFS.getRandomNodeSameRack(i);
            Log.debug("current node: " + i + " other node in rack: " + otherNodeInRack);
            assert i != otherNodeInRack;
        }
    }

    @Test
    public void testFindOtherNodeDifferentRack() {
        Switch switch1 = new Switch(0, LinkType.GIGABIT);
        Switch switch2 = new Switch(1, LinkType.GIGABIT);
        Switch mainSwitch = new Switch(2, LinkType.GIGABIT);

        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        Cluster.nodes = new Node[8];

        for (int i=0; i<Cluster.nodes.length; i++) {
            Cluster.nodes[i] = new Node(i, 1, 4, new Disk(SataType.SATA1, 50));

            if (i < 4)
                Cluster.nodes[i].connectSwitch(switch1);
            else
                Cluster.nodes[i].connectSwitch(switch2);
        }

        int nodeDifferentRack = HDFS.getRandomNodeDifferentRack(0);
        Log.debug("Node in different rack: " + nodeDifferentRack);
        assert nodeDifferentRack >= 4;

        int otherNodeDifferentRack = HDFS.getRandomNodeSameRack(nodeDifferentRack);
        Log.debug("Other node in different rack: " + otherNodeDifferentRack);
        assert nodeDifferentRack >= 4;
    }
    
    @Test
    public void testFindNodeInDifferentRack() {
        Switch switch1 = new Switch(0, LinkType.GIGABIT);
        Switch switch2 = new Switch(1, LinkType.GIGABIT);
        Switch mainSwitch = new Switch(2, LinkType.GIGABIT);

        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        Cluster.nodes = new Node[8];

        for (int i=0; i<Cluster.nodes.length; i++) {
            Cluster.nodes[i] = new Node(i, 1, 4, new Disk(SataType.SATA1, 50));

            if (i < 4)
                Cluster.nodes[i].connectSwitch(switch1);
            else
                Cluster.nodes[i].connectSwitch(switch2);
        }

        for (int i=0; i<3; i++) {
            int otherNodeInRack = HDFS.getRandomNodeDifferentRack(i);
            Log.debug("i: " + i + " other node: " + otherNodeInRack);
            assert otherNodeInRack != i;
        }
    }

    @Test
    public void testJobStartToFinishOneUser() {
        // only one user here

        // init nodes

        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpu = {4, 1, 2, 8, 10, 16, 6, 4};
        int[] ram = {12, 4, 8, 16, 20, 20, 8, 8};

        for (int i = 0; i < MRConfigs.numNodes; i++) {
            Cluster.nodes[i] = new Node(i, cpu[i], ram[i], new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
        }

        // init links --> Hadoop assumes that the topology used is tree topology
        // so we will divide the nodes into two racks
        int nodePerRack = MRConfigs.numNodes / 2;

        // main switch
        Switch mainSwitch = new Switch(0, LinkType.TENGIGABIT);

        // switches for racks
        Switch switch1 = new Switch(1, LinkType.GIGABIT);
        Switch switch2 = new Switch(2, LinkType.GIGABIT);
        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        for (int i=0; i<MRConfigs.numNodes; i++) {
            if (i < nodePerRack) {
                // first rack
                Cluster.nodes[i].connectSwitch(switch1);
            } else {
                // second rack
                Cluster.nodes[i].connectSwitch(switch2);
            }
        }

        // only one user here
        User user1 = new User(0, 1);
        Cluster.users = new User[1];
        Cluster.users[0] = user1;

        int[] taskLengths = {10, 20, 30, 40};
        Cluster.taskLengths[user1.getUserID()] = taskLengths;

        HDFS.put();

        // see the blocks on each node
        for (int i=0; i<MRConfigs.numNodes; i++) {
            Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
        }

        MapReduce.runMR();

        Log.debug("==========================================");
        Log.debug("Total makespan: " + Cluster.totalMakeSpan);

        // check blocks
    }

    @Test
    public void testRunMaps() {
        Node n1 = new Node(0, 10, 12, new Disk(SataType.SATA1, 100));
        Node n2 = new Node(1, 4, 12, new Disk(SataType.SATA1, 60));

        User arwan = new User(0, 15);
        User ahmad = new User(1, 10);

        Job jobArwan = new Job(0, arwan.getUserID());
        Job jobAhmad = new Job(1, ahmad.getUserID());

        Mapper mapper = new Mapper(0, arwan.getUserID(), 25);
        Mapper mapper2 = new Mapper(0, ahmad.getUserID(), 20);

        jobArwan.addMapper(mapper);
        jobAhmad.addMapper(mapper2);

        n1.addJob(jobArwan);
        n1.addJob(jobAhmad);
        n1.runJob();

        // if n2 runs the job, then it must be slower
        n2.addJob(jobArwan);
        n2.addJob(jobAhmad);
        n2.runJob();
    }

    @Test
    public void testRunJobs() {
        int taskID = 0;

        Node n1 = new Node(0, 10, 12, new Disk(SataType.SATA1, 100));
        Node n2 = new Node(1, 4, 12, new Disk(SataType.SATA1, 60));

        User arwan = new User(0, 15);
        User ahmad = new User(1, 10);

        Job jobArwan = new Job(0, arwan.getUserID());
        Job jobAhmad = new Job(1, ahmad.getUserID());

        Mapper mapperArwan = new Mapper(taskID++, arwan.getUserID(), 25);
        Mapper mapperAhmad = new Mapper(taskID++, ahmad.getUserID(), 20);

        Shuffle shuffleArwan = new Shuffle(taskID++, arwan.getUserID(), 15);
        Shuffle shuffleAhmad = new Shuffle(taskID++, ahmad.getUserID(), 30);

        Sort sortArwan = new Sort(taskID++, arwan.getUserID(), 30);
        Sort sortAhmad = new Sort(taskID++, ahmad.getUserID(), 35);

        Reducer reducerArwan = new Reducer(taskID++, arwan.getUserID(), 15);
        Reducer reducerAhmad = new Reducer(taskID++, ahmad.getUserID(), 20);

        jobArwan.addMapper(mapperArwan);
        jobArwan.addShuffle(shuffleArwan);
        jobArwan.addSort(sortArwan);
        jobArwan.addReducer(reducerArwan);

        jobAhmad.addMapper(mapperAhmad);
        jobAhmad.addShuffle(shuffleAhmad);
        jobAhmad.addSort(sortAhmad);
        jobAhmad.addReducer(reducerAhmad);

        n1.addJob(jobArwan);
        n1.addJob(jobAhmad);
        n1.runJob();

        // if n2 runs the job, then it must be slower
        n2.addJob(jobArwan);
        n2.addJob(jobAhmad);
        n2.runJob();
    }
}

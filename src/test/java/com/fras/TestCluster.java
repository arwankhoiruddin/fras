package com.fras;

import cluster.*;
import common.Functions;
import common.Log;
import common.MRConfigs;
import fifo.FifoScheduler;
import late.LATEScheduler;
import mapreduce.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import yarn.Scheduler;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class TestCluster {

    @BeforeAll
    public static void setUp() {
        new Cluster();
    }

    @Test
    public void testClusterRackNodes() {
        assert Cluster.nodes[1].getConnectedSwitch().nodes.size() == 4;
        assert Cluster.nodes[5].getConnectedSwitch().nodes.size() == 4;
        assert Cluster.nodes[1].getConnectedSwitch().parentSwitch.switches.size() == 2;
    }

    @Test
    public void testClusterSwitch() {
        assert Cluster.nodes[1].getConnectedSwitch().parentSwitch.getSwitchID() == 0;
        assert Cluster.nodes[1].getConnectedSwitch().getSwitchID() == 1;
        assert Cluster.nodes[6].getConnectedSwitch().getSwitchID() == 2;
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
    public void testNodeLink() {
        Node n1 = new Node(0, 10, 4, new Disk(SataType.SATA1, 50));
        Node n2 = new Node(1, 10, 3, new Disk(SataType.SATA3, 100));
        n1.setLink(1, LinkType.TENGIGABIT);

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
        Cluster.nodes[0].sendData(1, new Block(1, 0));
        assert Cluster.nodes[1].getDisk().getBlocks().getLast().getUserID() == 1;

        // send other block from other user
        Cluster.nodes[0].sendData(1, new Block(0, 1));
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
        Cluster.nodes[0].sendData(1, new Block(1, 0));
        Cluster.nodes[1].sendData(2, Cluster.nodes[1].getDisk().getBlocks().getLast());
        assert Cluster.nodes[1].getDisk().getBlocks().getLast().getUserID() == 1;
    }

    @Test
    public void testTimeLine4Map() {
        int numNode = 0;
        int numVCore = 0;
        Mapper mapper = new Mapper(0, 0, 20);
        Job job = new Job(0, 0);
        job.addMapper(mapper);
        Cluster.nodes[0].addJob(job);
        Cluster.nodes[0].runJob();
        assert Time.times.get(numNode).get(numVCore).size() == 20;
        assert Time.times.get(numNode).get(numVCore).get(0) == Status.RUN_MAP;
    }

    @Test
    public void testTimeLine4Reducer() {
        int numNode = 0;
        int numVCore = 0;
        Reducer reducer = new Reducer(0, 0, 20);
        Job job = new Job(0, 0);
        job.addReducer(reducer);
        Cluster.nodes[0].addJob(job);
        Cluster.nodes[0].runJob();
        assert Time.times.get(numNode).get(numVCore).size() == 20;
        assert Time.times.get(numNode).get(numVCore).get(0) == Status.RUN_REDUCE;
    }

    @Test
    public void testTimeLine4Shuffle() {
        int numNode = 0;
        int numVCore = 0;
        Shuffle shuffle = new Shuffle(0, 0, 20);

        Job job = new Job(0, 0);
        job.addShuffle(shuffle);

        Cluster.nodes[0].addJob(job);
        Cluster.nodes[0].runJob();
        assert Time.times.get(numNode).get(numVCore).size() == 20;
        assert Time.times.get(numNode).get(numVCore).get(0) == Status.RUN_SHUFFLE;
    }

    @Test
    public void testTimeLine4Sort() {
        int numNode = 0;
        int numVCore = 0;
        Sort sort = new Sort(0, 0, 20);
        Job job = new Job(0, 0);
        job.addSort(sort);
        Cluster.nodes[0].addJob(job);
        Cluster.nodes[0].runJob();
        assert Time.times.get(numNode).get(numVCore).size() == 20;
        assert Time.times.get(numNode).get(numVCore).get(0) == Status.RUN_SORT;
    }

    @Test
    public void testTimeLine() {

    }

    @Test
    public void testHeartbeat() {

        double[] userData = new double[MRConfigs.numUsers];
        double[] cpuLoads = new double[MRConfigs.numUsers];
        double[] ioLoads = new double[MRConfigs.numUsers];

        for (int i=0; i< MRConfigs.numUsers; i++) {
            if (MRConfigs.randomData) {
                userData[i] = (1 + new Random().nextInt(4)) * (10 ^ new Random().nextInt(2));
                cpuLoads[i] = new Random().nextDouble();
                ioLoads[i] = new Random().nextDouble();
            } else {
                double[] initUserData = {10, 40, 5, 80, 2, 160, 20, 40};
                double[] initCPU = {0.5, 0.3, 0.8, 0.1, 0.9, 0.3, 0.4, 0.7};
                double[] initIO = {0.5, 0.7, 0.6, 0.3, 0.9, 0.1, 0.3, 0.9};

                userData[i] = initUserData[i];
                cpuLoads[i] = initCPU[i];
                ioLoads[i] = initIO[i];
            }
        }

        // job characteristic for each user
        for (int user=0; user < userData.length; user++) {
            Cluster.users[user] = new User(user, userData[user]);
            Cluster.users[user].setCpuLoad(cpuLoads[user]);
            Cluster.users[user].setIoLoad(ioLoads[user]);
        }

        HDFS.put();
        HeartBeat heartBeat = new HeartBeat();
        // emulate that some nodes are unreachable
        Cluster.nodes[1].setReachable(false);
        Cluster.nodes[4].setReachable(false);
        heartBeat.sendHeartBeat();
        assert Cluster.liveNodes.size() == (MRConfigs.numNodes - 2);
    }

    @Test
    public void testFIFOScheduler() {
        MRConfigs.scheduler = new FifoScheduler();

        Job job1 = new Job(0, 0, 0.5, 0.7, 100);
        Job job2 = new Job(1, 0, 0.3, 0.5, 500);

        LinkedList<Job> jobs = new LinkedList<>();
        jobs.add(job1);
        jobs.add(job2);

        Scheduler scheduler = MRConfigs.scheduler;
        LinkedList<Job> scheduledJobs = scheduler.scheduleJob(jobs);

        assert scheduledJobs.removeFirst().getJobID() == 0;
        assert scheduledJobs.removeFirst().getJobID() == 1;

    }

    @Test
    public void testLATEScheduler() {
        MRConfigs.scheduler = new LATEScheduler();

        Job job1 = new Job(0, 0, 0.5, 0.7, 275);
        Job job2 = new Job(1, 0, 0.3, 0.5, 500);
        Job job3 = new Job(2, 0, 0.3, 0.8, 300);

        LinkedList<Job> jobs = new LinkedList<>();
        jobs.add(job1);
        jobs.add(job2);
        jobs.add(job3);

        Scheduler scheduler = MRConfigs.scheduler;
        LinkedList<Job> scheduledJobs = scheduler.scheduleJob(jobs);

        assert scheduledJobs.removeFirst().getJobID() == 0;
        assert scheduledJobs.removeFirst().getJobID() == 2;
        assert scheduledJobs.removeFirst().getJobID() == 1;

    }

    @Test
    public void testGigabit() {
        Switch s = new Switch(0, LinkType.GIGABIT);
        assert s.getLinkSpeed() == (1000 / 8);
    }

    @Test
    public void testConnectSwitch() {
        Switch aSwitch = new Switch(0, LinkType.GIGABIT);

        Node node1 = new Node(0, 12, 4, new Disk(SataType.SATA1, 60));
        Node node2 = new Node(0, 12, 4, new Disk(SataType.SATA1, 60));

        node1.connectSwitch(aSwitch);
        node2.connectSwitch(aSwitch);

        assert aSwitch.isConnected(node1);
        assert aSwitch.isConnected(node2);
        assert aSwitch.nodes.size() == 2;
    }

    @Test
    public void testSendDataSameRack() {
        Switch aSwitch = new Switch(0, LinkType.GIGABIT);

        Node node1 = new Node(0, 12, 4, new Disk(SataType.SATA1, 60));
        Node node2 = new Node(1, 12, 4, new Disk(SataType.SATA1, 60));

        node1.connectSwitch(aSwitch);
        node2.connectSwitch(aSwitch);

        assert node2.getDisk().getBlocks().size() == 0;
        node1.sendData(node2, new Block(0, 0));
        assert node2.getDisk().getBlocks().size() == 1;
    }

    @Test
    public void testSendDataDifferentRack() {
        Switch switch1 = new Switch(0, LinkType.GIGABIT);
        Switch switch2 = new Switch(1, LinkType.GIGABIT);
        Switch switch0 = new Switch(2, LinkType.TENGIGABIT);


        Node node1 = new Node(0, 12, 4, new Disk(SataType.SATA1, 60));
        Node node2 = new Node(1, 12, 4, new Disk(SataType.SATA1, 60));

        switch1.connectParentSwitch(switch0);
        switch2.connectParentSwitch(switch0);

        node1.connectSwitch(switch1);
        node2.connectSwitch(switch2);

        assert node2.getDisk().getBlocks().size() == 0;
        node1.sendData(node2, new Block(0, 0));
        assert node2.getDisk().getBlocks().size() == 1;
    }

    @Test
    public void testAsParentSwitch() {
        Switch parentSwitch = new Switch(0, LinkType.TENGIGABIT);
        Switch childSwitch1 = new Switch(1, LinkType.GIGABIT);
        Switch childSwitch2 = new Switch(2, LinkType.GIGABIT);

        childSwitch2.connectParentSwitch(parentSwitch);
        childSwitch1.connectParentSwitch(parentSwitch);

        assert childSwitch1.parentSwitch.equals(parentSwitch);
        assert childSwitch2.parentSwitch.equals(parentSwitch);
        assert parentSwitch.switches.size() == 2;
    }

    @Test
    public void testTreeTopology() {
        // we will create 2 racks connected by one switch
        // each rack contains 2 nodes

        Switch switch1 = new Switch(0, LinkType.GIGABIT);
        Switch switch2 = new Switch(1, LinkType.TENGIGABIT);
        Switch switch3 = new Switch(2, LinkType.GIGABIT);

        Node node1 = new Node(0, 12, 4, new Disk(SataType.SATA1, 60));
        Node node2 = new Node(0, 12, 4, new Disk(SataType.SATA1, 60));
        Node node3 = new Node(0, 12, 4, new Disk(SataType.SATA1, 60));
        Node node4 = new Node(0, 12, 4, new Disk(SataType.SATA1, 60));

        // connect nodes to switch
    }

    @Test
    public void testProcessorSpeed() {
        Node node1 = new Node(0, 1, 4, new Disk(SataType.SATA1, 60));
        Job job = new Job(0, 1);
        Mapper mapper = new Mapper(0, 1, 10);
        job.addMapper(mapper);
        node1.addJob(job);
        double speed1 = node1.getProcessingSpeed(job.getMappers().getLast().getTaskLength());
        assert speed1 == 10;

        Node node2 = new Node(1, 2, 2, new Disk(SataType.SATA1, 60));
        Job job2 = new Job(0, 1);
        Mapper mapper2 = new Mapper(0, 1, 200);
        job2.addMapper(mapper2);
        node2.addJob(job2);
        double speed2 = node2.getProcessingSpeed(job2.getMappers().getLast().getTaskLength());
        assert speed2 == 100;
    }

    @Test
    public void testNewCluster() {
        MRConfigs.numNodes = 100;
        Cluster cluster = new Cluster();
        cluster.randomInit();
    }
}

package com.fras;

import cluster.*;
import common.Functions;
import common.Log;
import common.MRConfigs;
import common.Ping;
import fras.BlockPlacementStrategy;
import mapreduce.*;
import net.sourceforge.jFuzzyLogic.FIS;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.*;

public class TestFunctions {
    @Test
    public void testMatrixConvolution() {
        int[][] b = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}; // kernel
        int[][] a = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        int[][] res = {{9, 18, 27}, {36, 45, 54}, {63, 72, 81}};

        int[][] conv = Functions.convolution(a, b);
        assert (Arrays.deepEquals(res, conv));
    }

    @Test
    public void testStatRandomCPURAM() {
        int minCPU = 1;
        int minRAM = 1;
        int maxCPU = 24;
        int maxRAM = 24;

        int stdDevCPU = 4;
        int meanCPU = 12;
        int stdDevRAM = 2;
        int meanRAM = 16;

        int numNodes = 100;

        for (int i=0; i<numNodes; i++) {
            if (new Random().nextBoolean() == true)
                Log.debug(new Random().nextDouble());
            else
                Log.debug("-" + new Random().nextDouble());
        }
    }

    @Test
    public void testRandGen() {
        int numNodes = 100;
        int[] nodes = new int[numNodes];
        for (int i=0; i<numNodes; i++) {
            nodes[i] = Functions.randStatGen(MRConfigs.meanCPU, MRConfigs.stdDevCPU);
        }
        Functions.printArrayToFile("test.txt", nodes);
    }

    @Test
    public void testRand() {
        for (int i=0; i<10; i++) {
            Log.debug(Functions.randStatGen(MRConfigs.meanLink, MRConfigs.stdLink) % 4);
        }
    }

    @Test
    public void testGNNMatrix() {
        Switch mainSwitch = new Switch(0, LinkType.TENGIGABIT);
        Switch switch1 = new Switch(1, LinkType.GIGABIT);
        Switch switch2 = new Switch(2, LinkType.FIVEGIGABIT);

        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpus = {1, 4, 2, 6, 1, 12, 1, 4};
        int[] rams = {4, 4, 16, 10, 4, 20, 2, 6};

        for (int i=0; i<MRConfigs.numNodes; i++) {
            Cluster.nodes[i] = new Node(0, cpus[i], rams[i], new Disk(SataType.SATA1, 60));

            if (i < 4)
                Cluster.nodes[i].connectSwitch(switch1);
            else
                Cluster.nodes[i].connectSwitch(switch2);
        }

        double[][] matrix = Functions.GNNMatrix();

        for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }

        Functions.printArrayToFile("matrix.txt", matrix);
    }

    @Test
    public void testGNN() {
        Switch mainSwitch = new Switch(0, LinkType.TENGIGABIT);
        Switch switch1 = new Switch(1, LinkType.GIGABIT);
        Switch switch2 = new Switch(2, LinkType.FIVEGIGABIT);

        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpus = {1, 4, 2, 6, 12, 8, 10, 4};
        int[] rams = {4, 4, 16, 10, 8, 8, 20, 6};

        for (int i=0; i<MRConfigs.numNodes; i++) {
            Cluster.nodes[i] = new Node(0, cpus[i], rams[i], new Disk(SataType.SATA1, 60));

            if (i < 4)
                Cluster.nodes[i].connectSwitch(switch1);
            else
                Cluster.nodes[i].connectSwitch(switch2);
        }

        double[] matrix = Functions.GNN();

        Log.debug("Max node weight: " + Functions.maxNodeWeight());
    }

    @Test
    public void testFuzzy() {
        Map<String, Double> fuzzyVar = new HashMap<>();
        fuzzyVar.put("ping", 0.5);
        fuzzyVar.put("cpu", 1.);
        fuzzyVar.put("ram", 4.);
        Log.debug("FIS Result: " + Functions.fuzzyInference("gnn.fcl", fuzzyVar, "priority"));
    }

    @Test
    public void testTaskLengthConfig() {
        int[] user1Length = {10, 20, 30};
        int[] user2Length = {20, 30, 40};
        Cluster.taskLengths[0] = user1Length;
        Cluster.taskLengths[1] = user2Length;
    }

    @Test
    public void testFindMinimal() {
        int[] a = {5, 10, 3, 8, 20};
        int minIdx = 0;
        int min = 2000;

        for (int i=0; i<a.length; i++) {
            if (a[i] < min) {
                min = a[i];
                minIdx = i;
            }
        }
        Log.debug("Index of minimum value: " + minIdx);
    }

    @Test
    public void testRoulette() {
        int[] cpu = {4, 1, 2, 4, 2, 16, 1, 2};
        int[] ram = {12, 4, 8, 4, 4, 20, 2, 3};

        MRConfigs.numNodes = cpu.length;

        Cluster.nodes = new Node[MRConfigs.numNodes];

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

        // check ping
        for (int i=0; i<MRConfigs.numNodes; i++) {
            for (int j=0; j<MRConfigs.numNodes; j++) {
                Log.debug("Ping from " + i + " to " + j + ": " + Cluster.nodes[i].ping(Cluster.nodes[j]));
            }
        }

        boolean multiple = true;

        if (multiple) {
            int[] freq = new int[MRConfigs.numNodes];
            for (int i = 0; i < 100; i++) {
                int chosen = Functions.randGNNRoulette();
                freq[chosen]++;
            }

            for (int i = 0; i < freq.length; i++) {
                Log.debug(i + "\t" + cpu[i] + "\t" + ram[i] + "\t" + freq[i]);
            }
        } else {
            Log.debug(Functions.randGNNRoulette());
        }
    }

    @Test
    public void testDoubleRandom() {
        int n=100;
        double[] rand = new double[n];
        for (int i=0; i<n; i++) {
            rand[i] = new Random().nextDouble();
        }

        Functions.printArrayToFile("test.txt", rand);
    }

    @Test
    public void testPlotArray() {
        LinkedList<Long> n = new LinkedList<>();
        for (int i=0; i<200; i++) {
            n.add(new Random().nextLong(10000));
        }

        Functions.plotList(n, "test");
    }

    @Test
    public void testPlotList() throws Exception {
        Ping.measureProcess();
        Ping.pingSeveralTimes("139.59.111.23", 100);
//        Functions.printListToFile("ping.txt", Ping.pings);
        Functions.plotList(Ping.pings, "Ping for DigitalOcean");
    }

    @Test
    public void testHeartBeatConceptOneProcessorWithRuntime() {
        int heartBeat = 3;
        int CPUSpeed = 2;
        int[] jobs = {4, 1, 5, 2, 10, 1, 3};

        LinkedList<Double> scheduled = new LinkedList();
        LinkedList status = new LinkedList();

        double runningTime = 0;

        for (int i=0; i<jobs.length; i++) {
            runningTime = (double) jobs[i] / CPUSpeed;
            Log.debug("Running time: " + runningTime);
            int hbCount = (int) runningTime / heartBeat;
            if (hbCount > 0) {
                for (int j=0; j<(runningTime / heartBeat); j++) {
                    runningTime -= heartBeat;
                    scheduled.add((double) heartBeat);
                    status.add(1);
                }
            }
            scheduled.add(runningTime);
            status.add(1);

            if (runningTime % heartBeat != 0) {
                double reminder = heartBeat - runningTime;
                scheduled.add(reminder);
                status.add(0);
            }
        }

        double totalTime = 0;
        for (int i=0; i<status.size(); i++) {
            Log.debug(status.get(i) + " \t " + scheduled.get(i));
            totalTime += (double) scheduled.get(i);
        }
        Log.debug("Total time: " + totalTime);

    }

    @Test
    public void testHeartBeatConceptOneProcessor() {
        int heartBeat = 3;
        int[] jobs = {4, 1, 5, 2, 10, 1, 3};
        LinkedList scheduled = new LinkedList();


        for (int i=0; i<jobs.length; i++) {
            for (int j=0; j < jobs[i]; j++) {
                scheduled.add(1);
            }
            if (jobs[i] % heartBeat != 0) {
                int reminder = heartBeat - (jobs[i] % heartBeat);
                for (int j=0; j< reminder; j++) {
                    scheduled.add(0);
                }
            }
        }

        Functions.printList(scheduled);
    }

    @Test
    public void testHeartBeatConceptMultiProcessor() {
        int heartBeat = 3;
        int[] jobs = {4, 1, 5, 2, 10, 1, 3};
        int nProcessor = 4;

        LinkedList[] scheduled = new LinkedList[nProcessor];
        for (int i=0; i<nProcessor; i++) {
            scheduled[i] = new LinkedList();
        }

        int procNumber = 0;
        for (int i=0; i<jobs.length; i++) {
            for (int j=0; j<jobs[i]; j++)
                scheduled[procNumber].add(1);

            if (jobs[i] % heartBeat != 0) {
                int reminder = heartBeat - (jobs[i] % heartBeat);
                for (int j=0; j<reminder; j++) {
                    scheduled[procNumber].add(0);
                }
            }
            if (procNumber >= nProcessor - 1)
                procNumber = 0;
            else
                procNumber++;
        }

        for (int i=0; i<nProcessor; i++) {
            Functions.printList(scheduled[i]);
        }
    }

    @Test
    public void testHeartBeatConceptMultiProcessorScheduled() {
        int heartBeat = 3;
        int[] jobs = {4, 1, 5, 2, 10, 1, 3};
        int nProcessor = 4;

        LinkedList[] scheduled = new LinkedList[nProcessor];
        for (int i=0; i<nProcessor; i++) {
            scheduled[i] = new LinkedList();
        }

        for (int i=0; i<jobs.length; i++) {

            // now we try to put the job in the shortest LinkedList
            int procNumber = 0;
            int minLength = 1000000;
            for (int j=0; j<nProcessor; j++) {
                if (scheduled[j].size() < minLength) {
                    minLength = scheduled[j].size();
                    procNumber = j;
                }
            }

            for (int j=0; j<jobs[i]; j++)
                scheduled[procNumber].add(1);

            if (jobs[i] % heartBeat != 0) {
                int reminder = heartBeat - (jobs[i] % heartBeat);
                for (int j=0; j<reminder; j++) {
                    scheduled[procNumber].add(0);
                }
            }
        }

        for (int i=0; i<nProcessor; i++) {
            Functions.printList(scheduled[i]);
        }
    }

    @Test
    public void testHeartBeatConceptMultiProcessorSomeStepsScheduled() {
        int heartBeat = 3;
        int[] jobs = {4, 1, 5, 2, 10, 1, 3};
        int[] shuffle = {8, 5, 7};
        int[] sort = {7, 8, 3};
        int nProcessor = 4;

        LinkedList[] scheduled = new LinkedList[nProcessor];
        for (int i=0; i<nProcessor; i++) {
            scheduled[i] = new LinkedList();
        }

        scheduled = schedule(jobs, nProcessor, scheduled, heartBeat);
        scheduled = fillZero(nProcessor, scheduled);

        // schedule the next process
        scheduled = schedule(shuffle, nProcessor, scheduled, heartBeat);
        scheduled = fillZero(nProcessor, scheduled);

        // schedule the next process
        scheduled = schedule(sort, nProcessor, scheduled, heartBeat);
        scheduled = fillZero(nProcessor, scheduled);
    }

    @Test
    public void testHeartBeatConceptMultiSlotsSomeStepsScheduled() {
        int heartBeat = 3;
        int[] jobs = {4, 1, 5, 2, 10, 1, 3};
        int[] shuffle = {8, 5, 7};
        int[] sort = {7, 8, 3};
        int[] cpu = {1, 2, 4}; // per CPU one slot
        int nProcessor = 0;
        for (int i=0; i<cpu.length; i++) {
            nProcessor += cpu[i];
        }

        LinkedList[] scheduled = new LinkedList[nProcessor];
        for (int i=0; i<nProcessor; i++) {
            scheduled[i] = new LinkedList();
        }

        scheduled = schedule(jobs, nProcessor, scheduled, heartBeat);
        scheduled = fillZero(nProcessor, scheduled);

        // schedule the next process
        scheduled = schedule(shuffle, nProcessor, scheduled, heartBeat);
        scheduled = fillZero(nProcessor, scheduled);

        // schedule the next process
        scheduled = schedule(sort, nProcessor, scheduled, heartBeat);
        scheduled = fillZero(nProcessor, scheduled);
    }

    private LinkedList[] fillZero(int nProcessor, LinkedList[] scheduled) {
        // find the max length from each processor
        int idxMax = 0;
        int maxLength = 0;
        for (int i=0; i<nProcessor; i++) {
            if (scheduled[i].size() > maxLength) {
                maxLength = scheduled[i].size();
                idxMax = i;
            }
            Functions.printList(scheduled[i]);
        }
        Log.debug("Max length: " + maxLength + " idx: " + idxMax);

        // fill the rest with zeros
        for (int i=0; i<nProcessor; i++) {
            for (int j=scheduled[i].size(); j<maxLength; j++) {
                scheduled[i].add(0);
            }
            Functions.printList(scheduled[i]);
        }
        return scheduled;
    }

    private LinkedList[] schedule(int[] jobs, int nProcessor, LinkedList[] scheduled, int heartBeat) {
        for (int i=0; i<jobs.length; i++) {

            // now we try to put the job in the shortest LinkedList
            int procNumber = 0;
            int minLength = 1000000;
            for (int j=0; j<nProcessor; j++) {
                if (scheduled[j].size() < minLength) {
                    minLength = scheduled[j].size();
                    procNumber = j;
                }
            }

            for (int j=0; j<jobs[i]; j++)
                scheduled[procNumber].add(1);

            if (jobs[i] % heartBeat != 0) {
                int reminder = heartBeat - (jobs[i] % heartBeat);
                for (int j=0; j<reminder; j++) {
                    scheduled[procNumber].add(0);
                }
            }
        }
        return scheduled;
    }

    @Test
    public void mapBlockWithUser() {
        Map<Integer, Integer> blockMap = new HashMap<>(); // key: blockID, value: userID
        blockMap.put(0, 1);
        blockMap.put(1, 1);
        blockMap.put(5, 2);
        blockMap.put(10, 2);

        Log.debug("Block number 1 belongs to user number: " + blockMap.get(1));
        Log.debug("Block number 10 belongs to user number: " + blockMap.get(10));
    }

    @Test
    public void testBlockPlacement() {
        // to find the best data structure to save block placements

        Map userBlock = new HashMap();
        Map blockPlacement = new HashMap();
        Map<Integer, List<Integer>> replications = new HashMap<>();

        userBlock.put(1, 0); // block 1 belongs to user 0
        userBlock.put(2, 3); // block 2 belongs to user 3

        blockPlacement.put(2, 4); // block 2 is placed in node 4
        blockPlacement.put(1, 3); // block 1 is placed in node 3

        replications.put(1, new ArrayList<>());
        replications.get(1).add(3);
        replications.get(1).add(4);

        // block x belongs to user y is placed in node z
        for (int i=1; i<=userBlock.size(); i++) {
            Log.debug("Block " + i + " belongs to user " + userBlock.get(i) + " is placed in node " + blockPlacement.get(i));
            if (replications.get(i) != null) {
                ArrayList rep = (ArrayList) replications.get(i);
                for (int j=0; j< rep.size(); j++) {
                    Log.debug("Replica in node: " + rep.get(j));
                }
            }
        }
    }

    @Test
    public void testRunMapper() {
        Map userBlock = new HashMap();
        Mapper mapper = new Mapper(0, 0, 10);
        Job job = new Job(0, 0);
        job.addMapper(mapper);
    }

    @Test
    public void testMRTaskSub() {
        Mapper mapper = new Mapper(0, 0, 10);
        Reducer reducer = new Reducer(0, 0, 10);
    }

    @Test
    public void testExperiment() {
        MRConfigs.numNodes = 10;
        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
        MRConfigs.displayLog = true;
        MRConfigs.debugLog = true;

        int numExperiments = 5;
        double[] makespans = new double[numExperiments];

        for (int n=0; n<numExperiments; n++) {
            System.out.println("Experiment number: " + n);
            Cluster cluster = new Cluster();
            cluster.randomInit();

//
//            HDFS.put();
//
//            // see the blocks on each node
//            for (int i=0; i<MRConfigs.numNodes; i++) {
//                Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
//            }
//
//            MapReduce.runMR();
//
//            Log.debug("==========================================");
//            System.out.println("Total makespan: " + Cluster.totalMakeSpan);
//            makespans[n] = Cluster.totalMakeSpan;
        }

        Functions.printArrayToFile("experiment1.txt", makespans);
    }
}

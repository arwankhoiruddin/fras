package cluster;

import common.Functions;
import common.Log;
import common.MRConfigs;
import mapreduce.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Cluster {
    public static Switch[] switches;
    public static Node[] nodes;
    public static User[] users = new User[MRConfigs.numUsers];
    public static int numBlocks;
    public static LinkedList liveNodes = new LinkedList();

    public static int blockID = 0;
    public static int taskID = 0;
    public static int jobID = 0;

    public static Map<Integer, Integer> blockUserID = new HashMap<>();  // key: blockID, value: userID
    public static Map<Integer, Integer> blockPlacement = new HashMap<>(); // key: blockID, value: node
    public static Map<Integer, List<Integer>> replications = new HashMap<>(); // key: blockID, values: nodes to put the replicas

    public static int[][] taskLengths = new int[MRConfigs.numUsers][4]; // mapper, shuffle, sort, reducer

    public static double totalMakeSpan = 0;

    // Hahaha.. finally got the solution... spent a lot of time on this one
    // So, the problem was because the MapReduce job run is based on the iteration of blockID
    // Because on each experiment, the blockID was not reset to 0, then the run time is always incremented
    public Cluster() {
        Cluster.blockID = 0;
    }

    public void init() {
        // init nodes
        for (int i=0; i < MRConfigs.numNodes; i++) {
            nodes[i] = new Node(i, MRConfigs.ramPerNodes, MRConfigs.vCpuPerNodes, new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
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
                nodes[i].connectSwitch(switch1);
            } else {
                // second rack
                nodes[i].connectSwitch(switch2);
            }
        }

        // initialize the times based on number of nodes and cores
        for (int i=0; i< MRConfigs.numNodes; i++) {
            LinkedList node = new LinkedList();
            for (int j=0; j < MRConfigs.vCpuPerNodes; j++) {
                node.add(new LinkedList<>());
            }
            Time.times.add(node);
        }
    }

    public void randomInit() {

        switches = new Switch[MRConfigs.numRacks + 1];
        nodes = new Node[MRConfigs.numNodes];

        for (int i=0; i<switches.length; i++) {
            LinkType linkType = randLink();
            switches[i] = new Switch(i, linkType);
            if (i > 0) {
                switches[i].connectParentSwitch(switches[0]);
            }
        }

        for (int i=0; i<MRConfigs.numNodes; i++) {
            int cpu = Functions.randStatGen(MRConfigs.meanCPU, MRConfigs.stdDevCPU);
            int ram = Functions.randStatGen(MRConfigs.meanRAM, MRConfigs.stdDevRAM);
            nodes[i] = new Node(i, cpu, ram, new Disk(SataType.SATA1, 100));
            Log.debug("Node " + i + ": " + cpu + " " + ram);
        }

        int nodeNumber = 0;
        int nodePerRack = MRConfigs.numNodes / MRConfigs.numRacks;

        for (int i=0; i<MRConfigs.numRacks; i++) {
            for (int j=0; j<nodePerRack; j++) {
                nodes[nodeNumber].connectSwitch(switches[i+1]);
                Log.debug("Node " + nodeNumber + " is connected to switch " + nodes[nodeNumber].getConnectedSwitch().getSwitchID());
                nodeNumber++;
            }
        }

        for (int i=0; i<MRConfigs.numUsers; i++) {
            Cluster.users[i] = new User(i, Functions.randStatGen(MRConfigs.meanDataSize, MRConfigs.stdDevDataSize));
            int[] taskLength = new int[4]; // mapper, shuffle, sort, reducer
            for (int j=0; j<4; j++) {
                taskLength[j] = Functions.randStatGen(MRConfigs.meanTaskLength, MRConfigs.stdDevTaskLength);
            }
            Cluster.taskLengths[i] = taskLength;
            if (MRConfigs.debugLog) {
                Log.debug("Task length for user number: " + i);
                Functions.printArray(taskLength);
            }
        }

    }

    private LinkType randLink() {
        LinkType result = LinkType.GIGABIT;
        int link = Functions.randStatGen(MRConfigs.meanLink, MRConfigs.stdLink) % 4;

        switch (link) {
            case 0: result = LinkType.GIGABIT; break;
            case 1: result = LinkType.FIVEGIGABIT; break;
            case 2: result = LinkType.TENGIGABIT; break;
            case 3: result = LinkType.TWENTYFIVEGIGABIT; break;
        }
        return result;
    }
}

package cluster;

import common.MRConfigs;
import mapreduce.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Cluster {
    public static Node[] nodes = new Node[MRConfigs.numNodes];
    public static User[] users = new User[MRConfigs.numUsers];
    public static int numBlocks;
    public static LinkedList liveNodes = new LinkedList();
    public static int blockID = 0;

    public static Map<Integer, Integer> blockUserID = new HashMap<>();  // key: blockID, value: userID
    public static Map<Integer, Integer> blockPlacement = new HashMap<>(); // key: blockID, value: node
    public static Map<Integer, List<Integer>> replications = new HashMap<>(); // key: blockID, values: nodes to put the replicas

    public Cluster() {
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
}

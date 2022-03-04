package cluster;

import common.MRConfigs;
import mapreduce.*;

import java.util.LinkedList;

public class Cluster {
    public static Node[] nodes = new Node[MRConfigs.numNodes];
    public static User[] users = new User[MRConfigs.numUsers];
    public static int numBlocks;
    public static HeartBeat heartBeat;

    public Cluster() {
        // init nodes
        for (int i=0; i < MRConfigs.numNodes; i++) {
            nodes[i] = new Node(i, MRConfigs.ramPerNodes, MRConfigs.vCpuPerNodes, new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
        }

        // init links
        for (int i=0; i<MRConfigs.numNodes; i++) {
            for (int j=0; j<MRConfigs.numNodes; j++) {
                nodes[i].setLink(j, LinkType.GIGABIT);
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

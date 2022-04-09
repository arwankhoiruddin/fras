package com.fras;

import cluster.*;
import common.MRConfigs;
import mapreduce.HDFS;
import mapreduce.MapReduce;
import org.junit.jupiter.api.Test;

public class ExperimentalResults {

    @Test
    public void testLoadBalancingHomogeneous() {
        /*
         here we realize that random distribution of HDFS may cause imbalance in the load of each node.
         Here is one example
         Node 0 has 0 blocks
         Node 1 has 3 blocks
         Node 2 has 1 blocks
         Node 3 has 2 blocks

         So we may need load balance algorithm for this
         */

        // init nodes

        int numNodes = 4;
        Cluster.nodes = new Node[numNodes];

        for (int i = 0; i < numNodes; i++) {
            Cluster.nodes[i] = new Node(i, MRConfigs.ramPerNodes, MRConfigs.vCpuPerNodes, new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
        }

        // init links --> Hadoop assumes that the topology used is tree topology
        // so we will divide the nodes into two racks
        int nodePerRack = numNodes / 2;

        // main switch
        Switch mainSwitch = new Switch(0, LinkType.TENGIGABIT);

        // switches for racks
        Switch switch1 = new Switch(1, LinkType.GIGABIT);
        Switch switch2 = new Switch(2, LinkType.GIGABIT);
        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        for (int i=0; i<numNodes; i++) {
            if (i < nodePerRack) {
                // first rack
                Cluster.nodes[i].connectSwitch(switch1);
            } else {
                // second rack
                Cluster.nodes[i].connectSwitch(switch2);
            }
        }

        // only one user here
        User user1 = new User(0, 0.25);
        Cluster.users = new User[1];
        Cluster.users[0] = user1;

        HDFS.put();

        // see the blocks on each node
        for (int i=0; i<numNodes; i++) {
            System.out.println("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
        }
    }

    @Test
    public void testLoadBalancingHeterogeneous() {
        /*
         here we realize that random distribution of HDFS may cause imbalance in the load of each node.
         Here is one example
         Node 0 has 0 blocks
         Node 1 has 3 blocks
         Node 2 has 1 blocks
         Node 3 has 2 blocks

         So we may need load balance algorithm for this
         */

        // init nodes

        int numNodes = 4;
        Cluster.nodes = new Node[numNodes];

        for (int i = 0; i < numNodes; i++) {
            Cluster.nodes[i] = new Node(i, MRConfigs.ramPerNodes, MRConfigs.vCpuPerNodes, new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
        }

        // init links --> Hadoop assumes that the topology used is tree topology
        // so we will divide the nodes into two racks
        int nodePerRack = numNodes / 2;

        // main switch
        Switch mainSwitch = new Switch(0, LinkType.TENGIGABIT);

        // switches for racks
        Switch switch1 = new Switch(1, LinkType.GIGABIT);
        Switch switch2 = new Switch(2, LinkType.GIGABIT);
        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        for (int i=0; i<numNodes; i++) {
            if (i < nodePerRack) {
                // first rack
                Cluster.nodes[i].connectSwitch(switch1);
            } else {
                // second rack
                Cluster.nodes[i].connectSwitch(switch2);
            }
        }

        // only one user here
        User user1 = new User(0, 0.25);
        Cluster.users = new User[1];
        Cluster.users[0] = user1;

        HDFS.put();

        // see the blocks on each node
        for (int i=0; i<numNodes; i++) {
            System.out.println("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
        }
    }

}

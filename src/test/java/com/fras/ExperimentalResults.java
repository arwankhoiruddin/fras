package com.fras;

import cluster.*;
import common.Functions;
import common.Log;
import common.MRConfigs;
import fras.BlockPlacementStrategy;
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
            Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
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
            Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
        }
    }

    @Test
    public void resultDefaultBlockPlacement() {
        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;

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
    }

    @Test
    public void resultFRASBlockPlacement() {
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
    }

    @Test
    public void resultHomogeneousDefault() {
        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
        MRConfigs.isHomogeneous = true;

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpu = {4, 1, 2, 8, 10, 16, 6, 4};
        int[] ram = {12, 4, 8, 16, 20, 20, 8, 8};

        for (int i = 0; i < MRConfigs.numNodes; i++) {
            if (MRConfigs.isHomogeneous)
                Cluster.nodes[i] = new Node(i, cpu[0], ram[0], new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
            else
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
    }

    @Test
    public void resultHomogeneousFRAS() {
        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
        MRConfigs.isHomogeneous = true;

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpu = {4, 1, 2, 8, 10, 16, 6, 4};
        int[] ram = {12, 4, 8, 16, 20, 20, 8, 8};

        for (int i = 0; i < MRConfigs.numNodes; i++) {
            if (MRConfigs.isHomogeneous)
                Cluster.nodes[i] = new Node(i, cpu[0], ram[0], new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
            else
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
    }

    @Test
    public void resultHeterogeneousFRASMultiUser() {
        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
        MRConfigs.isHomogeneous = false;

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpu = {4, 1, 2, 8, 10, 16, 6, 4};
        int[] ram = {12, 4, 8, 16, 20, 20, 8, 8};

        for (int i = 0; i < MRConfigs.numNodes; i++) {
            if (MRConfigs.isHomogeneous)
                Cluster.nodes[i] = new Node(i, cpu[0], ram[0], new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
            else
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
        User user2 = new User(1, 2);

        Cluster.users = new User[2];
        Cluster.users[0] = user1;
        Cluster.users[1] = user2;

        int[] taskLengths = {10, 20, 30, 40};
        int[] taskLengths2 = {5, 10, 7, 9};

        Cluster.taskLengths[user1.getUserID()] = taskLengths;
        Cluster.taskLengths[user2.getUserID()] = taskLengths2;

        HDFS.put();

        // see the blocks on each node
        for (int i=0; i<MRConfigs.numNodes; i++) {
            Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
        }

        MapReduce.runMR();

        Log.debug("==========================================");
        Log.debug("Total makespan: " + Cluster.totalMakeSpan);
    }

    @Test
    public void resultHomogeneousFRASMultiUser() {
        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
        MRConfigs.isHomogeneous = true;

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpu = {4, 1, 2, 8, 10, 16, 6, 4};
        int[] ram = {12, 4, 8, 16, 20, 20, 8, 8};

        for (int i = 0; i < MRConfigs.numNodes; i++) {
            if (MRConfigs.isHomogeneous)
                Cluster.nodes[i] = new Node(i, cpu[0], ram[0], new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
            else
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
        User user2 = new User(1, 2);

        Cluster.users = new User[2];
        Cluster.users[0] = user1;
        Cluster.users[1] = user2;

        int[] taskLengths = {10, 20, 30, 40};
        int[] taskLengths2 = {5, 10, 7, 9};
        Cluster.taskLengths[user1.getUserID()] = taskLengths;
        Cluster.taskLengths[user2.getUserID()] = taskLengths2;

        HDFS.put();

        // see the blocks on each node
        for (int i=0; i<MRConfigs.numNodes; i++) {
            Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
        }

        MapReduce.runMR();

        Log.debug("==========================================");
        Log.debug("Total makespan: " + Cluster.totalMakeSpan);
    }

    @Test
    public void resultHeterogeneousDefaultMultiUser() {
        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
        MRConfigs.isHomogeneous = false;

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpu = {4, 1, 2, 8, 10, 16, 6, 4};
        int[] ram = {12, 4, 8, 16, 20, 20, 8, 8};

        for (int i = 0; i < MRConfigs.numNodes; i++) {
            if (MRConfigs.isHomogeneous)
                Cluster.nodes[i] = new Node(i, cpu[0], ram[0], new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
            else
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
        User user2 = new User(1, 2);

        Cluster.users = new User[2];
        Cluster.users[0] = user1;
        Cluster.users[1] = user2;

        int[] taskLengths = {10, 20, 30, 40};
        int[] taskLengths2 = {5, 10, 7, 9};
        Cluster.taskLengths[user1.getUserID()] = taskLengths;
        Cluster.taskLengths[user2.getUserID()] = taskLengths2;

        HDFS.put();

        // see the blocks on each node
        for (int i=0; i<MRConfigs.numNodes; i++) {
            Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
        }

        MapReduce.runMR();

        Log.debug("==========================================");
        Log.debug("Total makespan: " + Cluster.totalMakeSpan);
    }

    @Test
    public void resultHomogeneousDefaultMultiUser() {
        MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
        MRConfigs.isHomogeneous = true;

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

        int[] cpu = {4, 1, 2, 8, 10, 16, 6, 4};
        int[] ram = {12, 4, 8, 16, 20, 20, 8, 8};

        for (int i = 0; i < MRConfigs.numNodes; i++) {
            if (MRConfigs.isHomogeneous)
                Cluster.nodes[i] = new Node(i, cpu[0], ram[0], new Disk(SataType.SATA3, MRConfigs.diskSpacePerNodes));
            else
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
        User user2 = new User(1, 2);

        Cluster.users = new User[2];
        Cluster.users[0] = user1;
        Cluster.users[1] = user2;

        int[] taskLengths = {10, 20, 30, 40};
        int[] taskLengths2 = {5, 10, 7, 9};
        Cluster.taskLengths[user1.getUserID()] = taskLengths;
        Cluster.taskLengths[user2.getUserID()] = taskLengths2;

        HDFS.put();

        // see the blocks on each node
        for (int i=0; i<MRConfigs.numNodes; i++) {
            Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
        }

        MapReduce.runMR();

        Log.debug("==========================================");
        Log.debug("Total makespan: " + Cluster.totalMakeSpan);
    }

    @Test
    public void testExperimentsGNN() {
        Switch mainSwitch = new Switch(0, LinkType.TENGIGABIT);
        Switch switch1 = new Switch(1, LinkType.GIGABIT);
        Switch switch2 = new Switch(2, LinkType.FIVEGIGABIT);

        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        int[] cpus = {1, 4, 2, 6, 1, 12, 1, 4};
        int[] rams = {4, 4, 16, 10, 4, 20, 2, 6};

        MRConfigs.numNodes = 8;
        Cluster.nodes = new Node[MRConfigs.numNodes];

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
    public void experiment1() {
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 4;
        MRConfigs.stdDevTaskLength = 2;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 10;

        int numExperiments = 100;

        double[] makespanFRAS = new double[numExperiments];
        double[] makespanDefault = new double[numExperiments];

        int expVariation = 2;

        for (int n=0; n<numExperiments; n++) {
            Cluster.totalMakeSpan = 0;
            System.out.println("Experiment number: " + n);
            Cluster cluster = new Cluster();
            cluster.randomInit();

            for (int exp=0; exp<expVariation; exp++) {
                // reset blockID
                Cluster.blockID = 0;

                switch (exp) {
                    case 0: MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS; break;
                    case 1: MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT; break;
                }

                HDFS.put();

                // see the blocks on each node
                for (int i=0; i<MRConfigs.numNodes; i++) {
                    Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                }

                MapReduce.runMR();

                Log.debug("==========================================");
                System.out.println("Total makespan: " + Cluster.totalMakeSpan);

                switch (exp) {
                    case 0:
                        makespanFRAS[n] = Cluster.totalMakeSpan;
                        break;
                    case 1:
                        makespanDefault[n] = Cluster.totalMakeSpan;
                        break;
                }
            }

        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
    }

    @Test
    public void experiment2() {
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 1000;
        MRConfigs.stdDevTaskLength = 500;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;

        int numExperiments = 30;
        int numVariations = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.stdDevCPU = var;
            System.out.println("Standard Deviation: " + var);

            for (int n = 0; n < numExperiments; n++) {
                System.out.println("Experiment number: " + n);
                Cluster cluster = new Cluster();
                cluster.randomInit();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }

    @Test
    public void experiment3() {
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 4;
        MRConfigs.stdDevTaskLength = 2;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;

        int numExperiments = 30;
        int numVariations = 10;

        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 1;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.stdDevCPU = var;
            System.out.println("Standard Deviation: " + var);

            for (int n = 0; n < numExperiments; n++) {
                MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                Cluster.totalMakeSpan = 0;
                System.out.println("Experiment number: " + n);
                Cluster cluster = new Cluster();
                cluster.randomInit();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    Cluster.blockID = 0;


                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Total makespan: " + Cluster.totalMakeSpan);

                    makespanFAIR[n][var] = Cluster.totalMakeSpan;
                }
            }
        }

        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }

    @Test
    public void experimentIncreasingCPUFromOne() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 40;
        MRConfigs.stdDevTaskLength = 10;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.incrementConfig = true;

        int numExperiments = 30;
        int numVariations = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.stdDevCPU = var;
            System.out.println("Standard Deviation: " + var);

            for (int n = 0; n < numExperiments; n++) {
                System.out.println("Experiment number: " + n);
                Cluster cluster = new Cluster();
                cluster.randomInit();
                cluster.simulateClusterProblem();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }

    @Test
    public void experimentDifferentRackNumbers() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        Functions.clearLog();

        int numVariations = 10;
        int numExperiments = 30;

        MRConfigs.numNodes = 100;
        MRConfigs.numRacks = 4;

        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 400;
        MRConfigs.stdDevTaskLength = 100;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numRacks += var;
            System.out.println("Number of Racks: " + MRConfigs.numRacks);

            for (int n = 0; n < numExperiments; n++) {
                System.out.println("Experiment number: " + n);
                Cluster cluster = new Cluster();
                cluster.randomInit();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }

    @Test
    public void experimentDifferentNodeNumbers() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        Functions.clearLog();

        int numVariations = 10;
        int numExperiments = 30;

        MRConfigs.numNodes = 10;
        MRConfigs.numRacks = 4;

        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 400;
        MRConfigs.stdDevTaskLength = 100;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numNodes *= (var+1);
            System.out.println("Number of Nodes: " + MRConfigs.numNodes);

            for (int n = 0; n < numExperiments; n++) {
                System.out.println("Experiment number: " + n);
                Cluster cluster = new Cluster();
                cluster.randomInit();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }
}

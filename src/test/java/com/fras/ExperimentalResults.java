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
    public void experimentCompareFRASWithDefault() {
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
    public void experimentCompareFAIRDefaultFRAS() {
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
    public void experimentFair() {
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
    public void experimentHomogeneousDifferentRackNumbers() {
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
        MRConfigs.isHomogeneous = true;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numRacks += var;

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Number of racks: " + MRConfigs.numRacks + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHomogeneousDifferentNodeNumbers() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        Functions.clearLog();

        int numVariations = 10;
        int numExperiments = 30;

        MRConfigs.numNodes = 8;
        MRConfigs.numRacks = 4;

        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 400;
        MRConfigs.stdDevTaskLength = 100;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 11;
        MRConfigs.isHomogeneous = true;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numNodes = 10*(var+1);
            System.out.println("Number of Nodes: " + MRConfigs.numNodes);

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: " + MRConfigs.numNodes + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHomogeneousDifferentNodeNumbersSimulateClusterProblem() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        Functions.clearLog();

        int numVariations = 10;
        int numExperiments = 30;

        MRConfigs.numNodes = 8;
        MRConfigs.numRacks = 4;

        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 400;
        MRConfigs.stdDevTaskLength = 100;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 11;
        MRConfigs.isHomogeneous = true;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numNodes = 10*(var+1);
            System.out.println("Number of Nodes: " + MRConfigs.numNodes);

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: " + MRConfigs.numNodes + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHeterogeneousDifferentRackNumbers() {
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
        MRConfigs.isHomogeneous = false;

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
    public void experimentHeterogeneousDifferentNodeNumbers() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        Functions.clearLog();

        int numVariations = 10;
        int numExperiments = 30;

        MRConfigs.numNodes = 8;
        MRConfigs.numRacks = 4;

        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 400;
        MRConfigs.stdDevTaskLength = 100;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 11;
        MRConfigs.isHomogeneous = false;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numNodes = 10*(var+1);
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: " + MRConfigs.numNodes + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHeterogeneousDifferentNodeNumbersSimulateClusterProblem() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        Functions.clearLog();

        int numVariations = 8;
        int numExperiments = 5;

        MRConfigs.numNodes = 8;
        MRConfigs.numRacks = 4;

        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 400;
        MRConfigs.stdDevTaskLength = 100;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 11;
        MRConfigs.isHomogeneous = false;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numNodes = 10*(var+1);
            System.out.println("Number of Nodes: " + MRConfigs.numNodes);

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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: " + MRConfigs.numNodes + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentIncreaseHeterogeneity() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 100;
        MRConfigs.stdDevTaskLength = 30;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.incrementConfig = true;
        MRConfigs.isHomogeneous = false;

        int numExperiments = 30;
        int numVariations = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.stdDevCPU = var;

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: "
                            + MRConfigs.numNodes + "Standard Deviation: " + MRConfigs.stdDevCPU
                            + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentIncreaseHeterogeneitySimulateClusterProblem() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 40;
        MRConfigs.stdDevTaskLength = 10;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.incrementConfig = true;
        MRConfigs.isHomogeneous = false;

        int numExperiments = 30;
        int numVariations = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.stdDevCPU = var;

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: "
                            + MRConfigs.numNodes + "Standard Deviation: " + MRConfigs.stdDevCPU
                            + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHomogeneousIncreasingJobLengthVariation() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 40;
        MRConfigs.stdDevTaskLength = 10;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.isHomogeneous = true;

        int numExperiments = 30;
        int numVariations = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.meanTaskLength = 100 * (var + 1);
            MRConfigs.stdDevTaskLength = 30 * (var + 1);

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: "
                            + MRConfigs.numNodes + "Standard Deviation: " + MRConfigs.stdDevCPU
                            + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHomogeneousIncreasingJobLengthVariationSimulateClusterProblem() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 40;
        MRConfigs.stdDevTaskLength = 10;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.isHomogeneous = true;

        int numExperiments = 30;
        int numVariations = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.meanTaskLength = 10 * (var + 1);
            MRConfigs.stdDevTaskLength = 3 * (var + 1);

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: "
                            + MRConfigs.numNodes + "Standard Deviation: " + MRConfigs.stdDevCPU
                            + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHeterogeneousIncreasingJobLengthVariation() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10;
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanTaskLength = 40;
        MRConfigs.stdDevTaskLength = 10;
        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.isHomogeneous = false;

        int numExperiments = 30;
        int numVariations = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.meanTaskLength = 100 * (var + 1);
            MRConfigs.stdDevTaskLength = 30 * (var + 1);

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: "
                            + MRConfigs.numNodes + "Standard Deviation: " + MRConfigs.stdDevCPU
                            + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHeterogeneousIncreasingJobLengthVariationSimulateClusterProblem() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10; // this might be the culprit on the performance degradation when the job lengths are long
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 2;
        MRConfigs.isHomogeneous = false;

        boolean isShort = false;

        int numExperiments;
        int numVariations;

        if (isShort) {
            numExperiments = 5;
            numVariations = 8;
        } else {
            numExperiments = 30;
            numVariations = 11;
        }

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.meanTaskLength = 100 * (var + 1);
            MRConfigs.stdDevTaskLength = 30 * (var + 1);

            for (int n = 0; n < numExperiments; n++) {
                Cluster cluster = new Cluster();
                cluster.randomInit();
                cluster.simulateClusterProblem();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: "
                            + MRConfigs.numNodes + " Job length mean: " + MRConfigs.meanTaskLength
                            + "Job standard deviation: " + MRConfigs.stdDevTaskLength
                            + " Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }

    @Test
    public void experimentHeterogeneousIncreasingJobLengthVariationSimulateClusterProblemManyNodes() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 200; // this might be the culprit on the performance degradation when the job lengths are long
        MRConfigs.numRacks = 10;

        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.numUsers = 2;

        MRConfigs.meanCPU = 12;
        MRConfigs.isHomogeneous = false;

        int numExperiments = 30;
        int numVariations = 11;

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        MRConfigs.stdDevTaskLength = 3;
        for (int var = 0; var < numVariations; var++) {
            MRConfigs.meanTaskLength = 100*(var+1);
            MRConfigs.stdDevTaskLength = 30*(var+1);

            for (int n = 0; n < numExperiments; n++) {
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
                    System.out.println("Experiment number " + n + " variation number " + var + " Node number: "
                            + MRConfigs.numNodes + " Job length mean: " + MRConfigs.meanTaskLength
                            + " Total makespan: " + Cluster.totalMakeSpan);

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
    public void experimentHomogeneousIncreasingNumberUser() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10; // this might be the culprit on the performance degradation when the job lengths are long
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 2;
        MRConfigs.isHomogeneous = true;

        MRConfigs.meanTaskLength = 100;
        MRConfigs.stdDevTaskLength = 10;

        boolean isShort = false;

        int numExperiments;
        int numVariations;

        if (isShort) {
            numExperiments = 5;
            numVariations = 8;
        } else {
            numExperiments = 30;
            numVariations = 11;
        }

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numUsers = var + 1;
            Cluster cluster = new Cluster();
            Cluster.users = new User[MRConfigs.numUsers];
            Cluster.taskLengths = new int[MRConfigs.numUsers][4];

            for (int n = 0; n < numExperiments; n++) {
                cluster.randomInit();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Experiment number " + n
                            + " variation number " + var
                            + " Number of users: " + MRConfigs.numUsers
                            + " Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }

    @Test
    public void experimentHomogeneousIncreasingNumberUserClusterProblem() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10; // this might be the culprit on the performance degradation when the job lengths are long
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 2;
        MRConfigs.isHomogeneous = true;

        MRConfigs.meanTaskLength = 100;
        MRConfigs.stdDevTaskLength = 10;

        boolean isShort = false;

        int numExperiments;
        int numVariations;

        if (isShort) {
            numExperiments = 5;
            numVariations = 8;
        } else {
            numExperiments = 30;
            numVariations = 11;
        }

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numUsers = var + 1;
            Cluster cluster = new Cluster();
            Cluster.users = new User[MRConfigs.numUsers];
            Cluster.taskLengths = new int[MRConfigs.numUsers][4];

            for (int n = 0; n < numExperiments; n++) {
                cluster.randomInit();
                cluster.simulateClusterProblem();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Experiment number " + n
                            + " variation number " + var
                            + " Number of users: " + MRConfigs.numUsers
                            + " Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }

    @Test
    public void experimentHeterogeneousIncreasingNumberUser() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10; // this might be the culprit on the performance degradation when the job lengths are long
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 2;
        MRConfigs.isHomogeneous = false;

        MRConfigs.meanTaskLength = 100;
        MRConfigs.stdDevTaskLength = 10;

        boolean isShort = false;

        int numExperiments;
        int numVariations;

        if (isShort) {
            numExperiments = 5;
            numVariations = 8;
        } else {
            numExperiments = 30;
            numVariations = 11;
        }

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numUsers = var + 1;
            Cluster cluster = new Cluster();
            Cluster.users = new User[MRConfigs.numUsers];
            Cluster.taskLengths = new int[MRConfigs.numUsers][4];

            for (int n = 0; n < numExperiments; n++) {
                cluster.randomInit();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Experiment number " + n
                            + " variation number " + var
                            + " Number of users: " + MRConfigs.numUsers
                            + " Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }

    @Test
    public void experimentHeterogeneousIncreasingNumberUserClusterProblem() {
        // with simulated cluster problem
        // experiment with different standar deviations of CPU
        // from homogeneous to very heterogeneous
        Functions.clearLog();

        MRConfigs.numNodes = 10; // this might be the culprit on the performance degradation when the job lengths are long
        MRConfigs.debugLog = true;
        MRConfigs.displayLog = true;

        MRConfigs.meanCPU = 12;
        MRConfigs.stdDevCPU = 2;
        MRConfigs.isHomogeneous = false;

        MRConfigs.meanTaskLength = 100;
        MRConfigs.stdDevTaskLength = 10;

        boolean isShort = false;

        int numExperiments;
        int numVariations;

        if (isShort) {
            numExperiments = 5;
            numVariations = 8;
        } else {
            numExperiments = 30;
            numVariations = 11;
        }

        double[][] makespanFRAS = new double[numExperiments][numVariations];
        double[][] makespanDefault = new double[numExperiments][numVariations];
        double[][] makespanFAIR = new double[numExperiments][numVariations];

        int expVariation = 3;

        for (int var = 0; var < numVariations; var++) {
            MRConfigs.numUsers = var + 1;
            Cluster cluster = new Cluster();
            Cluster.users = new User[MRConfigs.numUsers];
            Cluster.taskLengths = new int[MRConfigs.numUsers][4];

            for (int n = 0; n < numExperiments; n++) {
                cluster.randomInit();
                cluster.simulateClusterProblem();

                for (int exp = 0; exp < expVariation; exp++) {
                    // reset blockID
                    cluster.resetCluster();

                    switch (exp) {
                        case 0:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FRAS;
                            break;
                        case 1:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.FAIR;
                            break;
                        case 2:
                            MRConfigs.blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;
                    }

                    HDFS.put();

                    // see the blocks on each node
                    for (int i = 0; i < MRConfigs.numNodes; i++) {
                        Log.debug("Node " + i + " has " + Cluster.nodes[i].getDisk().getBlocks().size() + " blocks");
                    }

                    MapReduce.runMR();

                    Log.debug("==========================================");
                    System.out.println("Experiment number " + n
                            + " variation number " + var
                            + " Number of users: " + MRConfigs.numUsers
                            + " Total makespan: " + Cluster.totalMakeSpan);

                    switch (exp) {
                        case 0:
                            makespanFRAS[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 1:
                            makespanFAIR[n][var] = Cluster.totalMakeSpan;
                            break;
                        case 2:
                            makespanDefault[n][var] = Cluster.totalMakeSpan;
                    }
                }
            }
        }

        Functions.printArrayToFile("makespanFRAS.txt", makespanFRAS);
        Functions.printArrayToFile("makespanDefault.txt", makespanDefault);
        Functions.printArrayToFile("makespanFAIR.txt", makespanFAIR);
    }
}

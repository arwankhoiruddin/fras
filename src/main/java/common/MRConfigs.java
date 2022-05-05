package common;

import fifo.FifoScheduler;
import fras.BlockPlacementStrategy;
import fras.FRAS;
import mapreduce.ReplicationStrategy;
import yarn.Scheduler;

public class MRConfigs {
    public static boolean displayLog = false;
    public static boolean debugLog = false;
    public static boolean randomData = false;
    public static boolean simulateClusterProblems = false;
    public static int heartbeat = 3; // minutes

    public static int numRacks = 2;
    public static int numUsers = 4;
    public static int vCpuPerNodes = 4;
    public static int ramPerNodes = 16;
    public static int diskSpacePerNodes = 60;
    public static double blockSize = 128;
    public static int numNodes = 8;

    public static ReplicationStrategy replicationStrategy = ReplicationStrategy.REPLICATION;

    public static BlockPlacementStrategy blockPlacementStrategy = BlockPlacementStrategy.FRAS;

    public static boolean isHomogeneous = true;

    public static Scheduler scheduler = new FRAS();

    // experiment configurations

    // job length configurations
    public static int minTaskLength = 1;
    public static int maxTaskLength = 25;
    public static int meanTaskLength = 15;
    public static int stdDevTaskLength = 10;

    // data size configurations
    public static int meanDataSize = 4;
    public static int stdDevDataSize = 2;

    // link configurations
    public static int minLink = 1;
    public static int maxLink = 25;
    public static int meanLink = 15;
    public static int stdLink = 10;

    // CPU and RAM configurations

    public static int minCPU = 1;
    public static int minRAM = 1;
    public static int maxCPU = 24;
    public static int maxRAM = 24;

    public static int stdDevCPU = 6;
    public static int meanCPU = 12;
    public static int stdDevRAM = 2;
    public static int meanRAM = 16;

}

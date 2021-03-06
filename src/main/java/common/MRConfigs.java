package common;

import fifo.FifoScheduler;
import fras.BlockPlacementStrategy;
import fras.FRAS;
import mapreduce.ReplicationStrategy;
import yarn.Scheduler;

public class MRConfigs {
    public static String logPath = "C:\\Users\\arwan\\Documents\\log.txt";

    public static boolean displayLog = true;
    public static boolean debugLog = true;
    public static boolean randomData = false;

    public static boolean simulateClusterProblems = false;
    public static double nodeProblemProbability = 0.15;

    public static int heartbeat = 3; // minutes

    public static int numRacks = 2;
    public static int numUsers = 4;
    public static int vCpuPerNodes = 4;
    public static int ramPerNodes = 16;
    public static int diskSpacePerNodes = 60;
    public static double blockSize = 128;
    public static int numNodes = 8;

    // weighting
    public static int ownCPUWeight = 10;
    public static int ownRAMWeight = 5;
    public static int neighCPUWeight = 5;
    public static int neighRAMWeight = 3;
    public static int pingWeight = 10;

    public static ReplicationStrategy replicationStrategy = ReplicationStrategy.REPLICATION;

    public static BlockPlacementStrategy blockPlacementStrategy = BlockPlacementStrategy.FRAS;

    public static boolean isHomogeneous = true;

    public static Scheduler scheduler = new FRAS();

    // experiment configurations
    public static boolean incrementConfig = false;

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
    public static double maxPing = 100;

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

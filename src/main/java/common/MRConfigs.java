package common;

import fifo.FifoScheduler;
import fras.BlockPlacementStrategy;
import fras.FRAS;
import mapreduce.ReplicationStrategy;
import yarn.Scheduler;

public class MRConfigs {
    public static boolean displayLog = true;
    public static boolean debugLog = true;
    public static boolean randomData = false;
    public static boolean simulateClusterProblems = false;
    public static int heartbeat = 3; // minutes

    public static int numUsers = 4;
    public static ReplicationStrategy replicationStrategy = ReplicationStrategy.REPLICATION;

    public static BlockPlacementStrategy blockPlacementStrategy = BlockPlacementStrategy.DEFAULT;

    public static boolean isHomogeneous = true;

    public static int numNodes = 4;
    public static int vCpuPerNodes = 4;
    public static int ramPerNodes = 16;
    public static int diskSpacePerNodes = 60;

    public static double blockSize = 128;

    public static Scheduler scheduler = new FRAS();
}

package common;

import fifo.FifoScheduler;
import fras.FRAS;
import mapreduce.ReplicationStrategy;
import yarn.Scheduler;

public class MRConfigs {
    public static boolean displayLog = true;
    public static boolean debugLog = true;
    public static boolean randomData = false;
    public static boolean simulateClusterProblems = false;

    public static int numUsers = 4;
    public static ReplicationStrategy replicationStrategy = ReplicationStrategy.REPLICATION;

    public static int numNodes = 8;
    public static int vCpuPerNodes = 4;
    public static int ramPerNodes = 16;
    public static int diskSpacePerNodes = 60;
    public static boolean isHomogeneous = true;

    public static double blockSize = 64;

    public static Scheduler scheduler = new FRAS();
}

import cluster.Cluster;
import cluster.Time;
import cluster.User;
import common.Functions;
import common.Log;
import common.MRConfigs;
import mapreduce.HDFS;
import mapreduce.MapReduce;

import java.util.LinkedList;
import java.util.Random;

public class MapRedRunner {
    public static void main(String[] args) {
        runMR();
    }

    public static void runMR() {
        // cluster init
        Cluster cluster = new Cluster();

        // randomly create user data, cpu and io loads
        // one job for one user, so we can put the characteristics of the user workload here
        double[] userData = new double[MRConfigs.numUsers];
        double[] cpuLoads = new double[MRConfigs.numUsers];
        double[] ioLoads = new double[MRConfigs.numUsers];

        for (int i=0; i< MRConfigs.numUsers; i++) {
            if (MRConfigs.randomData) {
                userData[i] = (1 + new Random().nextInt(4)) * (10 ^ new Random().nextInt(2));
                cpuLoads[i] = new Random().nextDouble();
                ioLoads[i] = new Random().nextDouble();
            } else {
                double[] initUserData = {10, 40, 5, 80, 2, 160, 20, 40};
                double[] initCPU = {0.5, 0.3, 0.8, 0.1, 0.9, 0.3, 0.4, 0.7};
                double[] initIO = {0.5, 0.7, 0.6, 0.3, 0.9, 0.1, 0.3, 0.9};

                userData[i] = initUserData[i];
                cpuLoads[i] = initCPU[i];
                ioLoads[i] = initIO[i];
            }
        }

        Log.debug("User data");
        Functions.printArray(userData);
        Functions.printArray(cpuLoads);
        Functions.printArray(ioLoads);

        for (int user=0; user < userData.length; user++) {
            Cluster.users[user] = new User(user, userData[user]);
            Cluster.users[user].setCpuLoad(cpuLoads[user]);
            Cluster.users[user].setIoLoad(ioLoads[user]);
        }

        // put the data into HDFS
        HDFS.put();

        // schedule and run MapReduce
        MapReduce.MRRun();
    }
}

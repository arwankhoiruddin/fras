package mapreduce;

import cluster.Block;
import cluster.Cluster;
import common.Functions;
import common.Log;
import common.MRConfigs;

import java.util.LinkedList;

public class MapReduce {
    public static void MRRun() {
        // next we must schedule the jobs before sending to nodes
        // for now, we just use FIFO

        // number of jobs is similar with the number of blocks
        int nodeNum = 1;
        Log.debug("Number of blocks: " + Cluster.numBlocks);

        int jobID = 0;
        for (int i=0; i<MRConfigs.numNodes; i++) {
            LinkedList<Block> data = Cluster.nodes[i].getData();

            // create job based on the block's userID
            for (int j=0; j<data.size(); j++) {
                int userID = data.get(j).getUserID();
                Job job = new Job(jobID++, userID, Cluster.users[userID].getCpuLoad(), Cluster.users[userID].getIoLoad());

                // we can work here to put the job
                Cluster.nodes[nodeNum].addJob(job);
            }

            nodeNum++;
            if (nodeNum == MRConfigs.numNodes) nodeNum = 1;
        }

        // run the jobs

        Log.debug("List of jobs and blocks in each node");
        int[] countData = new int[MRConfigs.numUsers];
        int[] countJobs = new int[MRConfigs.numUsers];

        for (int i=0; i<MRConfigs.numNodes; i++) {
            LinkedList<Job> jobs = Cluster.nodes[i].getJobs();
            LinkedList<Block> data = Cluster.nodes[i].getData();
            Log.debug("Node number: " + i + " has " + Cluster.nodes[i].getJobs().size() + " map jobs and " + Cluster.nodes[i].getData().size() + " data");

            int[] nData = new int[MRConfigs.numUsers];
            int[] nJobs = new int[MRConfigs.numUsers];

            for (int j=0; j<jobs.size(); j++) {
                countJobs[jobs.get(j).getUserID()]++;
                nJobs[jobs.get(j).getUserID()]++;
            }
            for (int j=0; j<data.size(); j++) {
                countData[data.get(j).getUserID()]++;
                nData[data.get(j).getUserID()]++;
            }

            Log.debug("Data");
            Functions.printArray(nData);
            Log.debug("Job");
            Functions.printArray(nJobs);
        }

        Functions.printArray(countData);
        Functions.printArray(countJobs);

    }
}

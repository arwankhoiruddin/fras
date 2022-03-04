package mapreduce;

import cluster.Block;
import cluster.Cluster;
import common.Functions;
import common.Log;
import common.MRConfigs;

import java.util.LinkedList;

public class MapReduce {
    /**
     * JobTracker (https://cwiki.apache.org/confluence/display/HADOOP2/JobTracker)
     *
     * Client applications submit jobs to the Job tracker.
     * The JobTracker talks to the NameNode to determine the location of the data
     * The JobTracker locates TaskTracker nodes with available slots at or near the data
     * The JobTracker submits the work to the chosen TaskTracker nodes.
     * The TaskTracker nodes are monitored. If they do not submit heartbeat signals often enough, they are deemed to have failed and the work is scheduled on a different TaskTracker.
     * A TaskTracker will notify the JobTracker when a task fails. The JobTracker decides what to do then: it may resubmit the job elsewhere, it may mark that specific record as something to avoid, and it may may even blacklist the TaskTracker as unreliable.
     * When the work is completed, the JobTracker updates its status.
     * Client applications can poll the JobTracker for information.
     */
    public static void MRRun() {
        // next we must schedule the jobs before sending to nodes
        // for now, we just use FIFO

        // number of jobs is similar with the number of blocks
        int nodeNum = 1;
        Log.debug("Number of blocks: " + Cluster.numBlocks);

        int jobID = 0;
        for (int i=0; i<MRConfigs.numNodes; i++) {
            LinkedList<Block> data = Cluster.nodes[i].getDisk().getBlocks();

            // jobtracker
            // https://www.hadoopinrealworld.com/jobtracker-and-tasktracker/

            // create job based on the block's userID
            for (int j=0; j<data.size(); j++) {
                int userID = data.get(j).getUserID();
                Job job = new Job(jobID++, userID, Cluster.users[userID].getCpuLoad(), Cluster.users[userID].getIoLoad());

                // should also model the heartbeat

                // we can work here to put the job
                // to do: schedule the job using Graph ANFIS
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
            LinkedList<Block> data = Cluster.nodes[i].getDisk().getBlocks();
            Log.debug("Node number: " + i + " has " + Cluster.nodes[i].getJobs().size() + " map jobs and " + Cluster.nodes[i].getDisk().getBlocks().size() + " data");

            // calculate number of jobs and data per user
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

            // run the jobs
        }

        Functions.printArray(countData);
        Functions.printArray(countJobs);

    }
}

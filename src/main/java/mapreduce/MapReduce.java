package mapreduce;

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
//                int userID = data.removeFirst().getUserID();
                int userID = data.get(j).getUserID();
                Job job = new Job(jobID++, userID, Cluster.users[userID].getCpuLoad(), Cluster.users[userID].getIoLoad(), 20);

                // should also model the heartbeat

                // we can work here to put the job
                // to do: schedule the job using Graph ANFIS
                Cluster.nodes[nodeNum].addJob(job);
            }

            nodeNum++;
            if (nodeNum == MRConfigs.numNodes) nodeNum = 1;
        }

        // schedule the job on each heartbeat
        HeartBeat heartBeat = new HeartBeat();

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

    public static void runMR() {

        // check blocks placement

        Log.debug("Cluster blockID: " + Cluster.blockID);

        // try to execute mappers on the original block
        for (int i=0; i<Cluster.blockID; i++) {
            int nodeHavingBlock = Cluster.blockPlacement.get(i);
            int userID = Cluster.blockUserID.get(i);
            Log.debug("Block ID: " + i + " belongs to user " + userID + " is placed in node " + nodeHavingBlock);

            // send the mapper in the node having block
            Mapper mapper = new Mapper(Cluster.taskID++, userID, Cluster.taskLengths[userID][0]);
            Shuffle shuffle = new Shuffle(Cluster.taskID++, userID, Cluster.taskLengths[userID][1]);
            Sort sort = new Sort(Cluster.taskID++, userID, Cluster.taskLengths[userID][2]);
            Reducer reducer = new Reducer(Cluster.taskID++, userID, Cluster.taskLengths[userID][3]);

            Job job = new Job(Cluster.jobID++, Cluster.blockUserID.get(i));

            job.addMapper(mapper);
            job.addShuffle(shuffle);
            job.addSort(sort);
            job.addReducer(reducer);

            Cluster.nodes[nodeHavingBlock].addJob(job);
        }

        // execute on each node
        for (int i=0; i<MRConfigs.numNodes; i++) {
            if (Cluster.nodes[i].isReachable()) {
                int numJobs = Cluster.nodes[i].getJobs().size();
                int numBlocks = Cluster.nodes[i].getDisk().getBlocks().size();

                Log.debug("Node number: " + i + " has " + numJobs + " jobs and " + numBlocks + " data");

                LinkedList jobs = Cluster.nodes[i].getJobs();
                LinkedList blocks = Cluster.nodes[i].getDisk().getBlocks();

                for (int j = 0; j < numJobs; j++) {
                    Job job = (Job) jobs.get(j);
                    Log.debug("Job number " + job.getJobID() + " belongs to user " + job.getUserID());
                }

                Cluster.nodes[i].runJob();
            }
        }

//        for (int i=0; i<Cluster.blockID; i++) {
//            int nodeHavingBlock = Cluster.blockPlacement.get(i);
//
//            // run mapper in the available block
////            Cluster.nodes[nodeHavingBlock].addJob(new Mapper(0, Cluster.blockUserID.get(i), 10));
//            Log.debug("Executing block " + i + " in node " + nodeHavingBlock);
//            Cluster.nodes[nodeHavingBlock].runJob();
//        }
        // if node having the original block failed, then execute the replica after next 3 heartbeat
        // generate intermediary result
        // execute shuffle
        // execute sort
        // execute reducer

    }
}

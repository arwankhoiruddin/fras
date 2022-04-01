package mapreduce;

import cluster.Cluster;
import common.Functions;
import common.Log;
import common.MRConfigs;

import java.util.LinkedList;
import java.util.Random;

public class HDFS {
    // how blocks are distributed in HDFS? This is in replica-based Hadoop
    // https://hadoopabcd.wordpress.com/2015/03/17/hdfs-file-blocks-distribution-in-datanodes/#:~:text=When%20writing%20data%20to%20an,of%20DataNodes%20from%20the%20NameNode.

    /**
     * We expect to see the first replica of all blocks to be local – on node hadoop22.
     *
     * We can see that:
     *
     * Block 0 of file file.txt is on hadoop22 (rack 2), hadoop33 (rack 3), hadoop32 (rack 3)
     * Block 1 of file file.txt is on hadoop22 (rack 2), hadoop33 (rack 3), hadoop32 (rack 3)
     * 2. The second replica is written to a different rack from the first, chosen at random.
     *
     * 3. The third replica is written to the same rack as the second replica, but on a different node.
     *
     * 4. If there are more replicas – spread them across the rest of the racks.
     */
    public static void put() {
        // split the data into blocks
        Log.debug("Block size: " + MRConfigs.blockSize + " MB");
        LinkedList<Block> blocks = new LinkedList<>();
        for (int userID=0; userID< Cluster.users.length; userID++) {
            int numBlocks = Functions.getNumberOfBlocks(Cluster.users[userID].getDataSize());

            if (MRConfigs.replicationStrategy == ReplicationStrategy.REPLICATION) numBlocks *= 3; // use the default number of block replica

            Log.debug("User ID: " + userID + ", Data size: " + Cluster.users[userID].getDataSize() + " GB, number of blocks: " + numBlocks);
            for (int blkNum=0; blkNum < numBlocks; blkNum++) {
                Block newBlock = new Block(userID);
                blocks.add(newBlock);
            }
        }

        Cluster.numBlocks = blocks.size();
        Log.debug("Total number of blocks: " + blocks.size());

        // spread the blocks into the cluster
        for (int blockNum=0; blockNum<blocks.size(); blockNum++) {
            int nodeNumber = 1 + new Random().nextInt(MRConfigs.numNodes - 1);
            Log.debug("Sending block number " + blockNum + " owned by user " + blocks.get(blockNum).getUserID() +  " to node number " + nodeNumber);

            // to be added
            // the parity is placed in different node/rack
            // here, we might be able to incorporate the Graph Neural Network to effectively place the blocks in the cluster
            Cluster.nodes[0].sendData(nodeNumber, blocks.get(blockNum));
        }
    }

    public static void put(int userID) {
        // split the data into blocks
        Log.debug("Block size: " + MRConfigs.blockSize + " MB");
        LinkedList<Block> blocks = new LinkedList<>();

        int numBlocks = Functions.getNumberOfBlocks(Cluster.users[userID].getDataSize());

        if (MRConfigs.replicationStrategy == ReplicationStrategy.REPLICATION) numBlocks *= 3; // use the default number of block replica

        Log.debug("User ID: " + userID + ", Data size: " + Cluster.users[userID].getDataSize() + " GB, number of blocks: " + numBlocks);
        for (int blkNum=0; blkNum < numBlocks; blkNum++) {
            Block newBlock = new Block(userID);
            blocks.add(newBlock);
        }

        Cluster.numBlocks = blocks.size();
        Log.debug("Total number of blocks: " + blocks.size());

        // spread the blocks into the cluster
        for (int blockNum=0; blockNum<blocks.size(); blockNum++) {
            int nodeNumber = 1 + new Random().nextInt(MRConfigs.numNodes - 1);
            Log.debug("Sending block number " + blockNum + " owned by user " + blocks.get(blockNum).getUserID() +  " to node number " + nodeNumber);

            // to be added
            // the parity is placed in different node/rack
            // here, we might be able to incorporate the Graph Neural Network to effectively place the blocks in the cluster
            Cluster.nodes[0].sendData(Cluster.nodes[nodeNumber], blocks.get(blockNum));
        }
    }
}

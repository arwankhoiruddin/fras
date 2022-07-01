package mapreduce;

import cluster.Cluster;
import cluster.Link;
import cluster.Node;
import cluster.Switch;
import common.Functions;
import common.Log;
import common.MRConfigs;
import fras.BlockPlacementStrategy;

import java.util.ArrayList;
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
//        LinkedList<Block> blocks = new LinkedList<>();

        for (int userID=0; userID< Cluster.users.length; userID++) {
            int numBlocks = Functions.getNumberOfBlocks(Cluster.users[userID].getDataSize());
            Log.debug("Number of blocks: " + numBlocks);

            Log.debug("User ID: " + userID + ", Data size: " + Cluster.users[userID].getDataSize() + " GB, " +
                    "Replication strategy: " + MRConfigs.replicationStrategy + " number of blocks: " + numBlocks);
            for (int blkNum=0; blkNum < numBlocks; blkNum++) {
                Block newBlock = new Block(userID, Cluster.blockID);
                distributeBlock(newBlock, 3);
            }
        }
    }

    public static int getRandomNode() {
        return Cluster.nodes[new Random().nextInt(Cluster.nodes.length - 1) + 1].getNodeID();
    }

    public static int getRandomNodeSameRack(int nodeNumber) {
        int differentNode = 0;

        Switch parentSwitch = Cluster.nodes[nodeNumber].getConnectedSwitch();
        LinkedList<Node> nodes = parentSwitch.nodes;

        int counter = 0;
        while (true) {
            int node = nodes.get(new Random().nextInt(nodes.size() - 1) + 1).getNodeID();
            if (node != nodeNumber) {
                differentNode = node;
                break;
            }
            if (counter == 10) break;
            counter++;
        }

        return differentNode;
    }

    public static int getRandomNodeDifferentRack(int nodeNumber) {
        int nodeFound = 0;

        // find switch of different rack
        Switch currentSwitch = Cluster.nodes[nodeNumber].getConnectedSwitch();
        Switch mainSwitch = currentSwitch.parentSwitch;

        LinkedList<Switch> rackSwitches = mainSwitch.switches;
        Switch otherRackSwitch = currentSwitch;

        // anticipate if there are more than one racks
        while (true) {
            Switch other = rackSwitches.get(new Random().nextInt(rackSwitches.size()));
            if (other.getSwitchID() != currentSwitch.getSwitchID()) {
                otherRackSwitch = other;
                break;
            }
        }

        return otherRackSwitch.nodes.get(new Random().nextInt(otherRackSwitch.nodes.size())).getNodeID();
    }

    private static void distributeBlock(Block block, int numReplication) {
        // https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HdfsBlockPlacementPolicies.html

        int nodeNumber = 0;

        switch (MRConfigs.blockPlacementStrategy) {
            case DEFAULT:
                nodeNumber = getRandomNodeSameRack(0);
                while (!Cluster.nodes[nodeNumber].isReachable())
                    nodeNumber = getRandomNodeSameRack(0);
                break;
            case FRAS:
                nodeNumber = Functions.randGNNRoulette();
                while (!Cluster.nodes[nodeNumber].isReachable())
                    nodeNumber = Functions.randGNNRoulette();
                break;
            case FAIR:
                nodeNumber = getRandomNode();
                while (!Cluster.nodes[nodeNumber].isReachable())
                    nodeNumber = getRandomNode();
                break;
        }

//        if (MRConfigs.blockPlacementStrategy == BlockPlacementStrategy.DEFAULT) {
//            nodeNumber = getRandomNodeSameRack(0);
//        } else if (MRConfigs.blockPlacementStrategy == BlockPlacementStrategy.FRAS) { // Fuzzy Resource Aware Block Placement
//            nodeNumber = Functions.randGNNRoulette();  // arwan todo: we can play around here
//        }

        Log.debug("Original block number " + block.getBlockID() + " distributed to node " + nodeNumber);

        Cluster.nodes[0].sendData(Cluster.nodes[nodeNumber], block);
        Cluster.blockPlacement.put(Cluster.blockID, nodeNumber);

        Log.debug("Block ID: " + Cluster.blockID);
        Cluster.blockUserID.put(Cluster.blockID, block.getUserID());

        // replicate the block and send to other nodes
        // send the first replica to other node in the same rack
        int rep1 = getRandomNodeSameRack(nodeNumber);
        while (!Cluster.nodes[rep1].isReachable())
            rep1 = getRandomNodeSameRack(nodeNumber);

        Cluster.nodes[0].sendData(Cluster.nodes[rep1], block);

        Log.debug("Distribute block number " + block.getBlockID() + " to the different rack");

        // send the second replica to other node in the different rack
        int nodeOtherRack = getRandomNodeDifferentRack(nodeNumber);
        while (!Cluster.nodes[nodeNumber].isReachable())
            nodeOtherRack = getRandomNodeDifferentRack(nodeNumber);

        Cluster.nodes[0].sendData(Cluster.nodes[nodeOtherRack], block);

        // send the third replica to other node in the different rack
        int rep2 = getRandomNodeSameRack(nodeOtherRack);
        while (!Cluster.nodes[nodeNumber].isReachable())
            rep2 = getRandomNodeSameRack(nodeOtherRack);

        Cluster.nodes[0].sendData(Cluster.nodes[rep2], block);

        // put in replication data
        Cluster.replications.put(Cluster.blockID, new ArrayList<>());

        Cluster.replications.get(Cluster.blockID).add(rep1);
        Cluster.replications.get(Cluster.blockID).add(nodeOtherRack);
        Cluster.replications.get(Cluster.blockID).add(rep2);

        // note the block distribution in NameNode
        Cluster.blockID++;
    }



    public static void put(int userID) {
        // split the data into blocks
        Log.debug("Block size: " + MRConfigs.blockSize + " MB");

        Cluster.numBlocks = Functions.getNumberOfBlocks(Cluster.users[userID].getDataSize());

        Log.debug("User ID: " + userID + ", Data size: " + Cluster.users[userID].getDataSize() + " GB, number of blocks: " + Cluster.numBlocks);
        for (int blkNum=0; blkNum < Cluster.numBlocks; blkNum++) {
            Block newBlock = new Block(userID, blkNum);
            distributeBlock(newBlock, 3);
        }
    }
}

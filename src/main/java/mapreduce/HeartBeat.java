package mapreduce;

import cluster.Cluster;
import common.Functions;
import common.MRConfigs;

import java.util.LinkedList;

public class HeartBeat {
    // heartbeat by default is sent per 3 seconds

    public void sendHeartBeat() {
        // monitor resources to see if nodes are alive and having enough resources needed for the works
        // if the node does not reply more than 10 minutes, then the node is considered dead
        // if this case happens, NameNode will replicate the blocks
        // in case of erasure encoding, NameNode will rebuild the blocks using information from parities and other blocks
        // NameNode that receives the Heartbeats from a DataNode also carries information like total storage capacity,
        // the fraction of storage in use, and the number of data transfers currently in progress.
        // For the NameNode’s block allocation and load balancing decisions, we use these statistics.

        // send heartbeat to all nodes. check if all nodes are reachable
        // when NameNode get reply from DataNode, then schedule job
        // when no reply, then perform data recovery in other live nodes

        LinkedList<Block> lostBlocks = new LinkedList<>();
        for (int i=0; i< MRConfigs.numNodes; i++) {
            if (Cluster.nodes[i].isReachable()) {
                Cluster.liveNodes.add(i);
            } else {
                LinkedList<Block> blocks = Cluster.nodes[i].getDisk().getBlocks();
                for (int j=0; j<blocks.size(); j++) {
                    lostBlocks.add(blocks.get(j));
                }
            }
        }

        // perform data recovery. replicate the blocks lost and put in the available nodes
        int nodeIdx = 0;
        for (int i=0; i<lostBlocks.size(); i++) {
            if (nodeIdx > Cluster.liveNodes.size()) nodeIdx = 0;
            else nodeIdx++;

            Cluster.nodes[nodeIdx].getDisk().addBlock(lostBlocks.get(i));
        }

        // do job scheduling
        // send the latest job in the queue to the available nodes


    }
}

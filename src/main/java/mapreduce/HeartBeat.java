package mapreduce;

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

        // send heartbeat to all nodes

        // when NameNode get reply, then schedule job
        // when no reply, then perform data recovery
    }
}

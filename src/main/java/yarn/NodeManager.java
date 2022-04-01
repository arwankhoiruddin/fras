package yarn;

import cluster.Cluster;
import mapreduce.Job;

import java.util.LinkedList;

public class NodeManager {
    private LinkedList<Integer> aliveNodes;
    public void setNodeAlive(int nodeId) {
        Cluster.nodes[nodeId].setReachable(true);
    }

    public boolean isNodeAlive(int nodeId) {
        return Cluster.nodes[nodeId].isReachable();
    }

}

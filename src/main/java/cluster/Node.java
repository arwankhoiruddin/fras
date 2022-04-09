package cluster;

import common.Log;
import common.MRConfigs;
import mapreduce.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Node {
    private int nodeID;
    private int ram;
    private int cpu;
    private Disk disk;
    private LinkedList<Job> jobs;
    private Map<Integer, Link> links = new HashMap<>();
    private LinkedList data = new LinkedList();
    private boolean reachable = true;
    private Switch connectedSwitch;

    // loads on certain time
    private double[] cpuLoads;
    private double memoryloads;

    public Node(int nodeID, int cpu, int ram, Disk disk) {
        this.nodeID = nodeID;
        this.ram = ram;
        this.cpu = cpu;
        this.disk = disk;
        this.jobs = new LinkedList<>();
        this.cpuLoads = new double[cpu];
    }

    public boolean isReachable() {
        return this.reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public int getNodeID() {
        return nodeID;
    }

    public double getRam() {
        return ram;
    }

    public double getCpu() {
        return cpu;
    }

    public Disk getDisk() {
        return disk;
    }

    public double getFreeRam() {
        return this.ram - memoryloads;
    }

    public double[] getFreeCPU() {
        // to do
        double[] free = new double[this.cpu];
        for (int i=0; i<this.cpu; i++) {
            free[i] = 1 - cpuLoads[i]; //
        }
        return free;
    }

    public double getFreeDisk() {
        // to do
        return 1;
    }

    public Link getLink(int destination) {
        return links.get(destination);
    }

    public void setLink(int destination, LinkType linkType) {
        // connect to switch

        links.put(destination, new Link(linkType));
        // two ways
        Cluster.nodes[destination].links.put(this.nodeID, new Link(linkType));
    }

    public void addJob(Job job) {
        jobs.add(job);
    }

    public LinkedList<Job> getJobs() {
        return jobs;
    }

    public void addBlock(Block block) {
        this.disk.addBlock(block);
    }

    public Switch getConnectedSwitch() {
        return this.connectedSwitch;
    }

    public void runJob() {
        // based on https://sci-hub.ru/https://ieeexplore.ieee.org/document/7019857
        // task runtime (T) can be formulated as
        // T = T_R + T_Q + T_D + T_E + T_O
        // T_R = resource preparation time
        // T_Q = queueing time
        // T_D = data transfer time
        // T_E = execution time
        // T_O = system overhead time
//        double timeToRun = this.cpu*1024*1024 * Cluster.users[this.jobs.removeFirst().getUserID()].getCpuLoad();
//        Log.display("Time to run the job: " + timeToRun);

        // each vCPU core can run a job
        double memLoads = 0;

        for (int i=0; i<this.cpu; i++) {
            if (jobs.size() > 0) {
                Job jobRun = jobs.removeLast();
                cpuLoads[i] = jobRun.getCpuLoad();
                memLoads += jobRun.getIOLoad();

                // add the job run to timeline, as much as the job time
                for (int t=0; t<jobRun.getJobLength(); t++) {
                    if (jobRun instanceof Mapper)
                        Time.times.get(this.nodeID).get(i).add(Status.RUN_MAP);
                    else if (jobRun instanceof Reducer)
                        Time.times.get(this.nodeID).get(i).add(Status.RUN_REDUCE);
                    else if (jobRun instanceof Shuffle)
                        Time.times.get(this.nodeID).get(i).add(Status.RUN_SHUFFLE);
                    else
                        Time.times.get(this.nodeID).get(i).add(Status.RUN_SORT);
                }
            }
        }

        this.memoryloads = memLoads;
    }

    public double ping(Node node) {
        double timeToTransfer = 0;
        int dataSize = 1;

        // find the destination node
        if (this.connectedSwitch.nodes.contains(node)) {
            timeToTransfer = 2 * dataSize / this.connectedSwitch.getLinkSpeed();
        } else {
            timeToTransfer = (dataSize / this.connectedSwitch.getLinkSpeed()) +
                    ( 2 * (dataSize / this.connectedSwitch.parentSwitch.getLinkSpeed())) +
                    (dataSize / node.connectedSwitch.getLinkSpeed());
        }
        return timeToTransfer;
    }

    public void sendData(Node node, MRData data) {
        double timeToTransfer = 0;
        double dataSize = 0;
        if (data instanceof Block) {
            dataSize = MRConfigs.blockSize;
            node.getDisk().addBlock((Block) data);
        } else if (data instanceof Parity) {
            dataSize = MRConfigs.blockSize / 2;
            node.getDisk().addParity((Parity) data);
        } else if (data instanceof Intermediary) {
            node.getDisk().addIntermediary((Intermediary) data);
            dataSize = ((Intermediary) data).getSize();
        }

        // find the destination node
        if (this.connectedSwitch.nodes.contains(node)) {
            timeToTransfer = 2 * dataSize / this.connectedSwitch.getLinkSpeed();
        } else {
            timeToTransfer = (dataSize / this.connectedSwitch.getLinkSpeed()) +
                    ( 2 * (dataSize / this.connectedSwitch.parentSwitch.getLinkSpeed())) +
                    (dataSize / node.connectedSwitch.getLinkSpeed());
        }

        Log.debug("Time to transfer from node " + this.nodeID + " to " + node.nodeID + " is " + timeToTransfer + " seconds");
    }

    public void sendData(int destination, MRData data) {

        double timeToTransfer = 0;

        if (data instanceof Block) {
            Block block = (Block) data;
            Log.debug("Data belongs to user number " + block.getUserID());
            Cluster.nodes[destination].getDisk().addBlock(block);
            timeToTransfer = Math.ceil(this.getLink(destination).getLinkSpeed() / MRConfigs.blockSize);
            Log.debug("Time to transfer from node " + this.nodeID + " to " + destination + " is " + timeToTransfer + " seconds");
        } else if (data instanceof Parity) {
            Parity parity = (Parity) data;
            Log.debug("Data belongs to user number " + parity.getUserID());
            Cluster.nodes[destination].getDisk().addParity(parity);
            timeToTransfer = Math.ceil(this.getLink(destination).getLinkSpeed() / (MRConfigs.blockSize * 2)); // size of parity is half of the size of block
            Log.debug("Time to transfer from node " + this.nodeID + " to " + destination + " is " + timeToTransfer + " seconds");
        } else if (data instanceof Intermediary) {
            Intermediary intermediary = (Intermediary) data;
            Cluster.nodes[destination].getDisk().addIntermediary(intermediary);
            timeToTransfer = Math.ceil(this.getLink(destination).getLinkSpeed() / intermediary.getSize());
            Log.debug("Time to transfer from node " + this.nodeID + " to " + destination + " is " + timeToTransfer + " seconds");
        }

        for (int i=0; i<timeToTransfer; i++) {
            Time.times.get(this.nodeID).get(0).add(Status.SEND_DATA);

            for (int j=0; j<this.cpu; j++) {
                Time.times.get(destination).get(j).add(Status.RECEIVE_DATA);
            }
        }
    }

    public void connectSwitch(Switch connectedSwitch) {
        this.connectedSwitch = connectedSwitch;
        connectedSwitch.connectNode(this);
    }
}

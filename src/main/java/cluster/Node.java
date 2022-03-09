package cluster;

import common.Log;
import common.MRConfigs;
import mapreduce.Block;
import mapreduce.Job;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Node {
    private int nodeID;
    private double ram;
    private int cpu;
    private Disk disk;
    private LinkedList<Job> jobs;
    private Map<Integer, Link> links = new HashMap<>();
    private LinkedList data = new LinkedList();
    private boolean reachable = true;

    // loads on certain time
    private double[] cpuLoads;
    private double memoryloads;

    public Node(int nodeID, double ram, int cpu, Disk disk) {
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
        links.put(destination, new Link(linkType));
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
            Job jobRun = jobs.removeFirst();
            cpuLoads[i] = jobRun.getCpuLoad();
            memLoads += jobRun.getIOLoad();
        }

        this.memoryloads = memLoads;

        // add the job run to timeline
    }

    public LinkedList getData() {
        return this.data;
    }

    public void sendData(int destination, Object data) {

        double timeToTransfer = Math.ceil(this.getLink(destination).getLinkSpeed() / MRConfigs.blockSize);
        Log.debug("Time to transfer from node " + this.nodeID + " to " + destination + " is " + timeToTransfer + " seconds");

        Block block = (Block) data;
        Log.debug("Data belongs to user number " + block.getUserID());
        Cluster.nodes[destination].getDisk().addBlock(block);

        for (int i=0; i<timeToTransfer; i++) {
            Time.times.get(this.nodeID).get(0).add(Status.SEND_DATA);

            for (int j=0; j<this.cpu; j++) {
                Time.times.get(destination).get(j).add(Status.RECEIVE_DATA);
            }
        }
    }
}

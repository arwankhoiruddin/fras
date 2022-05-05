package cluster;

import common.Log;
import common.MRConfigs;
import mapreduce.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Node {
    private int nodeID;
    private int ram; // influence multi processing
    private int cpu; // influence speed
    private Disk disk;
    private LinkedList<Job> jobs;
    private Map<Integer, Link> links = new HashMap<>();
    private LinkedList data = new LinkedList();
    private boolean reachable = true;
    private Switch connectedSwitch;

    // to mark each task run and heartbeat
    public LinkedList<Double> scheduled = new LinkedList<>();
    public LinkedList<Double> status = new LinkedList<>();
    public LinkedList<TaskType> taskType = new LinkedList();

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

    public double getProcessingSpeed(double length) {
        return length / this.cpu;
    }

    public void tryRunJob() {
        Log.debug("==================================");
        Log.debug("Running task in node number " + this.nodeID + " with job size: " + jobs.size());

        double totalRunTime = 0;
        for (Job job : jobs) {
            Log.debug("Job " + job.getJobID() + " is running");

            // run mappers
            for (int map=0; map < job.getMappers().size(); map++) {
                Mapper mapper = job.getMappers().get(map);
                Log.debug("Length of mapper of user " + mapper.getUserID() + ": " + mapper.getTaskLength());
                double mapTime = runTask(mapper);
                totalRunTime += mapTime;
            }
            // run shuffle
            for (int j=0; j < job.getShuffles().size(); j++) {
                Shuffle shuffle = job.getShuffles().get(j);
                Log.debug("Length of shuffle of user " + shuffle.getUserID() + ": " + shuffle.getTaskLength());
                double shuffleTime = runTask(shuffle);
                totalRunTime += shuffleTime;
            }

            // run sort
            for (int j=0; j < job.getSorts().size(); j++) {
                Sort sort = job.getSorts().get(j);
                Log.debug("Length of sort of user " + sort.getUserID() + ": " + sort.getTaskLength());
                double sortTime = runTask(sort);
                totalRunTime += sortTime;
            }

            // run reducer
            for (int j=0; j < job.getReducers().size(); j++) {
                Reducer reducer = job.getReducers().get(j);
                Log.debug("Length of reducer of user " + reducer.getUserID() + ": " + reducer.getTaskLength());
                double reduceTime = runTask(reducer);

                totalRunTime += reduceTime;
            }


        }
        Log.debug("Total time to run all jobs in the node: " + totalRunTime);

        if (totalRunTime > Cluster.totalMakeSpan) {
            Cluster.totalMakeSpan = totalRunTime;
        }
        printScheduled();
    }

    public void runJob() {

        Log.debug("==================================");
        Log.debug("Running task in node number " + this.nodeID + " with job size: " + jobs.size());

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

        // memory define parallelism, so
        // each core can run a job
        double memLoads = 0;
        double totalRunTime = 0;

//        for (int i=0; i < this.getJobs().size(); i++) {
            // one job can contain more than one mapper
            Job job = this.getJobs().removeFirst();
//            Job job = this.getJobs().get(i);


            for (int map=0; map < job.getMappers().size(); map++) {
                Mapper mapper = job.getMappers().get(map);
                Log.debug("Length of Mapper: " + mapper.getTaskLength());
                double mapTime = runTask(mapper);
                Log.debug("Size of Mappers: " + job.getMappers().size() + " Time to run mapper: " + mapTime);
                totalRunTime += mapTime;
            }
            // run shuffle
            for (int j=0; j < job.getShuffles().size(); j++) {
                Shuffle shuffle = job.getShuffles().get(j);
                Log.debug("Length of Shuffle: " + shuffle.getTaskLength());
                double shuffleTime = runTask(shuffle);
                Log.debug("Time to run shuffle: " + shuffleTime);
                totalRunTime += shuffleTime;
            }

            // run sort
            for (int j=0; j < job.getSorts().size(); j++) {
                Sort sort = job.getSorts().get(j);
                Log.debug("Length of Sort: " + sort.getTaskLength());
                double sortTime = runTask(sort);
                Log.debug("Time to run sort: " + sortTime);
                totalRunTime += sortTime;
            }

            // run reducer
            for (int j=0; j < job.getReducers().size(); j++) {
                Reducer reducer = job.getReducers().get(j);
                Log.debug("Length of Reducer: " + reducer.getTaskLength());
                double reduceTime = runTask(reducer);
                Log.debug("Time to run reducer: " + reduceTime);

                totalRunTime += reduceTime;
            }

//        }



        Log.debug("Total run time: " + totalRunTime);

        if (totalRunTime > Cluster.totalMakeSpan)
            Cluster.totalMakeSpan = totalRunTime;

        this.memoryloads = memLoads;
    }

    public void printScheduled() {
        for (int i=0; i<this.scheduled.size(); i++) {
            Log.debug(this.status.get(i) + " \t " + this.scheduled.get(i) + " \t " + this.taskType.get(i));
        }
    }

    public double runTask(MRTask mrTask) {
        // todo: fifo, capacity scheduler, and FRAS
        double totalRunTime = 0;

        double taskLength = mrTask.getTaskLength();
        double runTime = this.getProcessingSpeed(taskLength);
        Log.debug("Run time of task of user " + mrTask.getUserID() + ": " + runTime);

        int hbCount = (int) runTime / MRConfigs.heartbeat;

        for (int j=0; j<hbCount; j++) {
            this.scheduled.add((double) MRConfigs.heartbeat);
            this.status.add(1.);
            if (mrTask instanceof Mapper)
                this.taskType.add(TaskType.MAPPER);
            else if (mrTask instanceof Shuffle)
                this.taskType.add(TaskType.SHUFFLE);
            else if (mrTask instanceof Sort)
                this.taskType.add(TaskType.SORT);
            else
                this.taskType.add(TaskType.REDUCER);

            runTime -= MRConfigs.heartbeat;

            totalRunTime += MRConfigs.heartbeat;
        }

        totalRunTime += runTime;
        this.scheduled.add(runTime);
        this.status.add(1.);
        if (mrTask instanceof Mapper)
            this.taskType.add(TaskType.MAPPER);
        else if (mrTask instanceof Shuffle)
            this.taskType.add(TaskType.SHUFFLE);
        else if (mrTask instanceof Sort)
            this.taskType.add(TaskType.SORT);
        else
            this.taskType.add(TaskType.REDUCER);

        double remainder = MRConfigs.heartbeat - runTime;
        if (remainder > 0) {
            totalRunTime += remainder;
            this.scheduled.add(remainder);
            this.status.add(0.);
            this.taskType.add(TaskType.IDLE);
        }

        Log.debug("Total run time: " + totalRunTime);
        return totalRunTime;
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

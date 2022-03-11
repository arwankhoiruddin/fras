package mapreduce;

public class Job {
    private int jobID;
    private int userID;
    private double IOLoad; // refers to RAM IO Bound
    private double cpuLoad;
    private double jobLength;

    public Job(int jobID, int userID, double IOLoad, double cpuLoad, double jobLength) {
        this.jobID = jobID;
        this.userID = userID;
        this.IOLoad = IOLoad;
        this.cpuLoad = cpuLoad;
        this.jobLength = jobLength;
    }

    public int getJobID() {
        return jobID;
    }

    public int getUserID() {
        return userID;
    }

    public double getIOLoad() {
        return IOLoad;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public double getJobLength() { return this.jobLength; }
}

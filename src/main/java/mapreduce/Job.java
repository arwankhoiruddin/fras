package mapreduce;

public class Job {
    private int jobID;
    private int userID;
    private double IOLoad; // refers to RAM IO Bound
    private double cpuLoad;

    public Job(int jobID, int userID, double IOLoad, double cpuLoad) {
        this.jobID = jobID;
        this.userID = userID;
        this.IOLoad = IOLoad;
        this.cpuLoad = cpuLoad;
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
}

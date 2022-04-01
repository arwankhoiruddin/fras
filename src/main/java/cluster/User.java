package cluster;

public class User {
    private int userID;
    private double dataSize;

    // 1 job 1 user, so we can save the job characteristic for the user
    private double cpuLoad;
    private double ioLoad;

    public User(int userID, double dataSize) {
        this.userID = userID;
        this.dataSize = dataSize;
        Cluster.users[userID] = this;
    }

    public int getUserID() {
        return userID;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public double getCpuLoad() {
        return this.cpuLoad;
    }

    public void setIoLoad(double ioLoad) {
        this.ioLoad = ioLoad;
    }

    public double getIoLoad() {
        return this.ioLoad;
    }

    public double getDataSize() {
        return this.dataSize;
    }

    public void setDataSize(double dataSize) {
        this.dataSize = dataSize;
    }
}

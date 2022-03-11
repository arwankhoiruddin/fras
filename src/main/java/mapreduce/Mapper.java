package mapreduce;

public class Mapper extends Job {

    public Mapper(int jobID, int userID, double IOLoad, double cpuLoad, double jobLength) {
        super(jobID, userID, IOLoad, cpuLoad, jobLength);
    }

}

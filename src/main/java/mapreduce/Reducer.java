package mapreduce;

public class Reducer extends Job {

    public Reducer(int jobID, int userID, double IOLoad, double cpuLoad, double jobLength) {
        super(jobID, userID, IOLoad, cpuLoad, jobLength);
    }

}

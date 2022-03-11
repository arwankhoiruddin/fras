package mapreduce;

public class Shuffle extends Job {

    public Shuffle(int jobID, int userID, double IOLoad, double cpuLoad, double jobLength) {
        super(jobID, userID, IOLoad, cpuLoad, jobLength);
    }

}

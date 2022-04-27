package mapreduce;

public class Mapper extends MRTask {

//    public Mapper(int jobID, int userID, double IOLoad, double cpuLoad, double jobLength) {
//        super(jobID, userID, IOLoad, cpuLoad, jobLength);
//    }

    public Mapper(int taskID, int userID, double taskLength) {
        super(taskID, userID, taskLength);
    }

}

package mapreduce;

public class Reducer extends MRTask {

//    public Reducer(int jobID, int userID, double IOLoad, double cpuLoad, double jobLength) {
//        super(jobID, userID, IOLoad, cpuLoad, jobLength);
//    }

    public Reducer(int taskID, int userID, double taskLength) {
        super(taskID, userID, taskLength);
    }

}

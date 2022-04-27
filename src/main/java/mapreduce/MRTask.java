package mapreduce;

public class MRTask {
    private int taskID;
    private int userID;
    private double taskLength;

    public MRTask(int taskID, int userID, double taskLength) {
        this.taskID = taskID;
        this.userID = userID;
        this.taskLength = taskLength;
    }

    public int getTaskID() {
        return taskID;
    }

    public int getUserID() {
        return userID;
    }

    public double getTaskLength() {
        return taskLength;
    }

}

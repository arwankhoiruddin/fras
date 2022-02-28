package mapreduce;

public class Reducer {
    private int taskID;

    public Reducer(int taskID) {
        this.taskID = taskID;
    }

    public int getTaskID() {
        return taskID;
    }
}

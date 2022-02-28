package mapreduce;

public class Mapper {
    private int taskID;

    public Mapper(int taskID) {
        this.taskID = taskID;
    }

    public int getTaskID() {
        return taskID;
    }
}

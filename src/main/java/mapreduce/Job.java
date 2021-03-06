package mapreduce;

/*
Erp odoo


Aps development


AI
Rekomended sistem
Bigdata infra
Data crawlingp
Imsging
 */

import java.util.LinkedList;

public class Job {
    private int jobID;
    private int userID;
    private double IOLoad; // refers to RAM IO Bound
    private double cpuLoad;
    private double jobLength;
    private LinkedList<Mapper> mappers = new LinkedList();
    private LinkedList<Shuffle> shuffles = new LinkedList<>();
    private LinkedList<Sort> sorts = new LinkedList<>();
    private LinkedList<Reducer> reducers = new LinkedList<>();

    public Job(int jobID, int userID, double IOLoad, double cpuLoad, double jobLength) {
        this.jobID = jobID;
        this.userID = userID;
        this.IOLoad = IOLoad;
        this.cpuLoad = cpuLoad;
        this.jobLength = jobLength;
    }

    public Job(int jobID, int userID) {
        this.jobID = jobID;
        this.userID = userID;
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

    public double getJobLength() { return this.jobLength; }

    public void addMapper(Mapper mapper) {
        if (mapper.getUserID() == this.userID)
            this.mappers.add(mapper);
        else
            System.err.println("Incompatible user ID");
    }

    public LinkedList<Mapper> getMappers() {
        return this.mappers;
    }

    public void addShuffle(Shuffle shuffle) {
        if (shuffle.getUserID() == this.userID)
            this.shuffles.add(shuffle);
        else
            System.err.println("Incompatible user ID");
    }

    public LinkedList<Shuffle> getShuffles() {
        return this.shuffles;
    }

    public void addSort(Sort sort) {
        if (sort.getUserID() == this.getUserID())
            this.sorts.add(sort);
        else
            System.err.println("Incompatible user ID");
    }

    public LinkedList<Sort> getSorts() {
        return this.sorts;
    }

    public void addReducer(Reducer reducer) {
        if (reducer.getUserID() == this.userID)
            this.reducers.add(reducer);
        else
            System.err.println("Incompatible user ID");
    }

    public LinkedList<Reducer> getReducers() {
        return this.reducers;
    }
}

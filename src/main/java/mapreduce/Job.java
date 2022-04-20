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
        this.mappers.add(mapper);
    }

    public LinkedList<Mapper> getMappers() {
        return this.mappers;
    }

    public void addShuffle(Shuffle shuffle) {
        this.shuffles.add(shuffle);
    }

    public LinkedList<Shuffle> getShuffles() {
        return this.shuffles;
    }

    public void addSort(Sort sort) {
        this.sorts.add(sort);
    }

    public LinkedList<Sort> getSorts() {
        return this.sorts;
    }

    public void addReducer(Reducer reducer) {
        this.reducers.add(reducer);
    }

    public LinkedList<Reducer> getReducers() {
        return this.reducers;
    }
}

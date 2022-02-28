package yarn;

import mapreduce.Job;

import java.util.LinkedList;

public class Scheduler {
    private LinkedList<Job> jobs;
    public void addJob(Job job) {
        jobs.add(job);
    }

    public Job getNextJob() {
        return jobs.removeFirst();
    }
}

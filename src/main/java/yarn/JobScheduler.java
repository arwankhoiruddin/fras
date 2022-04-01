package yarn;

import mapreduce.Job;

import java.util.LinkedList;

public interface JobScheduler {
    public LinkedList<Job> scheduleJob(LinkedList<Job> jobs);

}

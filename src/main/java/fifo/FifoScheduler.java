package fifo;

import mapreduce.Job;
import yarn.JobScheduler;
import yarn.Scheduler;

import java.util.LinkedList;

public class FifoScheduler extends Scheduler implements JobScheduler {

    public LinkedList<Job> scheduleJob(LinkedList<Job> jobs) {
        // just return the jobs as is
        System.out.println("This is FIFO");
        return jobs;
    }

}

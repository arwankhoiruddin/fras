package late;

import mapreduce.Job;
import yarn.JobScheduler;
import yarn.Scheduler;

import java.util.LinkedList;

// shortest job first
public class LATEScheduler extends Scheduler implements JobScheduler {

    @Override
    public LinkedList<Job> scheduleJob(LinkedList<Job> jobs) {

        LinkedList<Job> sorted = jobs;

        for (int i=0; i<jobs.size() - 1; i++) {
            double minLength = sorted.get(i).getJobLength();
            for (int j=i + 1; j < jobs.size(); j++) {
                if (minLength > sorted.get(j).getJobLength()) {
                    minLength = sorted.get(j).getJobLength();
                    Job temp = sorted.get(i);
                    sorted.set(i, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }

        return sorted;
    }
}

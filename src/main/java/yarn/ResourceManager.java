package yarn;

import common.MRConfigs;
import mapreduce.Job;

import java.util.LinkedList;

public class ResourceManager {
    private JobSubmitter jobSubmitter;
    private NodeManager nodeManager;

    public LinkedList<Job> scheduleJob(LinkedList<Job> jobs) {
        return MRConfigs.scheduler.scheduleJob(jobs);
    }
}

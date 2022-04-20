package fifo;

import common.Functions;
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

    private LinkedList[] schedule(int[] jobs, int nProcessor, LinkedList[] scheduled, int heartBeat) {
        for (int i=0; i<jobs.length; i++) {

            // now we try to put the job in the shortest LinkedList
            int procNumber = 0;
            int minLength = 1000000;
            for (int j=0; j<nProcessor; j++) {
                if (scheduled[j].size() < minLength) {
                    minLength = scheduled[j].size();
                    procNumber = j;
                }
            }

            for (int j=0; j<jobs[i]; j++)
                scheduled[procNumber].add(1);

            if (jobs[i] % heartBeat != 0) {
                int reminder = heartBeat - (jobs[i] % heartBeat);
                for (int j=0; j<reminder; j++) {
                    scheduled[procNumber].add(0);
                }
            }
        }
        return scheduled;
    }

    private LinkedList[] fillZero(int nProcessor, LinkedList[] scheduled) {
        // find the max length from each processor
        int idxMax = 0;
        int maxLength = 0;
        for (int i=0; i<nProcessor; i++) {
            if (scheduled[i].size() > maxLength) {
                maxLength = scheduled[i].size();
                idxMax = i;
            }
            Functions.printList(scheduled[i]);
        }
        System.out.println("Max length: " + maxLength + " idx: " + idxMax);

        // fill the rest with zeros
        for (int i=0; i<nProcessor; i++) {
            for (int j=scheduled[i].size(); j<maxLength; j++) {
                scheduled[i].add(0);
            }
            Functions.printList(scheduled[i]);
        }
        return scheduled;
    }
}

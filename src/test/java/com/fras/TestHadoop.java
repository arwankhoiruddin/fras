package com.fras;

import cluster.*;
import common.MRConfigs;
import fifo.FifoScheduler;
import fras.FRAS;
import late.LATEScheduler;
import mapreduce.HDFS;
import mapreduce.HeartBeat;
import mapreduce.Job;
import mapreduce.MapReduce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TestHadoop {

    @BeforeEach
    public void setUp() {
        new Cluster();
    }

    @Test
    public void testHeartBeat() {
        HeartBeat heartBeat = new HeartBeat();
        heartBeat.sendHeartBeat();

        // now we can get the status of each node when the heartbeat is sent
    }

    @Test
    public void testHDFSPut() {
        User user = new User(0, 10);
        assert Cluster.nodes[1].getDisk().getBlocks().size() == 0;
        HDFS.put(0);
        assert Cluster.nodes[1].getDisk().getBlocks().size() > 0;
    }

    @Test
    public void testScheduleJobsFIFO() {
        LinkedList<Job> jobs = new LinkedList<Job>();
        Job job1 = new Job(0, 0, 0.5, 0.7,100);
        Job job2 = new Job(1, 0, 0.3, 0.5, 40);
        Job job3 = new Job(2, 0, 0.8, 0.3, 60);

        jobs.add(job1);
        jobs.add(job2);
        jobs.add(job3);

        MRConfigs.scheduler = new FifoScheduler();
        LinkedList<Job> scheduled = MRConfigs.scheduler.scheduleJob(jobs);

        assert scheduled.get(0).getJobLength() == 100;
        assert scheduled.get(1).getJobLength() == 40;
        assert scheduled.get(2).getJobLength() == 60;
    }

    @Test
    public void testScheduleJobsLate() {
        LinkedList<Job> jobs = new LinkedList<Job>();
        Job job1 = new Job(0, 0, 0.5, 0.7,100);
        Job job2 = new Job(1, 0, 0.3, 0.5, 40);
        Job job3 = new Job(2, 0, 0.8, 0.3, 60);

        jobs.add(job1);
        jobs.add(job2);
        jobs.add(job3);

        MRConfigs.scheduler = new LATEScheduler();
        LinkedList<Job> scheduled = MRConfigs.scheduler.scheduleJob(jobs);

        assert scheduled.get(0).getJobLength() == 40;
        assert scheduled.get(1).getJobLength() == 60;
        assert scheduled.get(2).getJobLength() == 100;
    }

    @Test
    public void testScheduleJobsFRAS() {
        LinkedList<Job> jobs = new LinkedList<Job>();
        Job job1 = new Job(0, 0, 0.5, 0.7,100);
        Job job2 = new Job(1, 0, 0.3, 0.5, 40);
        Job job3 = new Job(2, 0, 0.8, 0.3, 60);

        jobs.add(job1);
        jobs.add(job3);
        jobs.add(job2);

        MRConfigs.scheduler = new FRAS();
        LinkedList<Job> scheduled = MRConfigs.scheduler.scheduleJob(jobs);

        assert scheduled.get(0).getJobLength() == 60;
        assert scheduled.get(1).getJobLength() == 40;
        assert scheduled.get(2).getJobLength() == 100;

    }

    @Test
    public void testMap() {
        Map<Integer, Double> val = new HashMap<>();
        val.put(0, 0.9);
        val.put(1, 0.7);

        val.entrySet().stream().sorted(Map.Entry.<Integer, Double> comparingByValue())
                .forEach(System.out::println);

    }

    @Test
    public void testReverse() {
        for (int i=9; i>=0; i--) {
            System.out.println(i);
        }
    }

    @Test
    public void testLoadBalancing() {
        // here we realize that random distribution of HDFS may cause imbalance in the load of each node.
        // Here is one example
        // Node number: 1 has 3 map jobs and 2 data
        // Node number: 2 has 2 map jobs and 1 data
        // Node number: 3 has 1 map jobs and 3 data
        // so we may need load balance algorithm for this

        // only one user here
        User user1 = new User(0, 0.25);
        Cluster.users = new User[1];
        Cluster.users[0] = user1;

        HDFS.put();
        MapReduce.MRRun();
    }

    @Test
    public void testTaskStartToFinish() {
        // only one user here
        User user1 = new User(0, 0.25);
        Cluster.users = new User[1];
        Cluster.users[0] = user1;

        HDFS.put();
        MapReduce.MRRun();
    }
}

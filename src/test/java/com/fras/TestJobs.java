package com.fras;

import cluster.Cluster;
import common.MRConfigs;
import mapreduce.HeartBeat;
import mapreduce.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestJobs {

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
    public void testRunJob() {
    }
}

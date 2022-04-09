package com.fras;

import cluster.*;
import common.Functions;
import net.sourceforge.jFuzzyLogic.FIS;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestFunctions {
    @Test
    public void testMatrixConvolution() {
        int[][] b = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}; // kernel
        int[][] a = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        int[][] res = {{9, 18, 27}, {36, 45, 54}, {63, 72, 81}};

        int[][] conv = Functions.convolution(a, b);
        assert (Arrays.deepEquals(res, conv));
    }

    @Test
    public void testGNN() {
        Switch mainSwitch = new Switch(0, LinkType.TENGIGABIT);
        Switch switch1 = new Switch(1, LinkType.GIGABIT);
        Switch switch2 = new Switch(2, LinkType.FIVEGIGABIT);

        switch1.connectParentSwitch(mainSwitch);
        switch2.connectParentSwitch(mainSwitch);

        int numNodes = 8;
        Cluster.nodes = new Node[numNodes];

        int[] cpus = {1, 4, 2, 6, 12, 8, 10, 4};
        int[] rams = {4, 4, 16, 10, 8, 8, 20, 6};

        for (int i=0; i<numNodes; i++) {
            Cluster.nodes[i] = new Node(0, cpus[i], rams[i], new Disk(SataType.SATA1, 60));

            if (i < 4)
                Cluster.nodes[i].connectSwitch(switch1);
            else
                Cluster.nodes[i].connectSwitch(switch2);
        }

        // find the Graph Values
        double[] nodeVal = new double[numNodes];
        String fclFile = "gnn.fcl";
        FIS fis = FIS.load(fclFile, true);

        if (fis == null)
            System.err.println("Cannot load file");

        for (int i=0; i<numNodes; i++) {
            nodeVal[i] = 0;
            for (int j=0; j<numNodes; j++) {
                fis.setVariable("ping", Cluster.nodes[i].ping(Cluster.nodes[j]));
                fis.setVariable("cpu", Cluster.nodes[i].getCpu());
                fis.setVariable("ram", Cluster.nodes[i].getRam());

                fis.evaluate();
                nodeVal[i] += fis.getVariable("priority").getValue();
            }
            System.out.println("NodeVal " + i + ": " + nodeVal[i]);
        }

    }

    @Test
    public void testFuzzy() {
        Map<String, Double> fuzzyVar = new HashMap<>();
        fuzzyVar.put("ping", 0.5);
        fuzzyVar.put("cpu", 1.);
        fuzzyVar.put("ram", 4.);
        System.out.println("FIS Result: " + Functions.fuzzyInference("gnn.fcl", fuzzyVar, "priority"));
    }
}

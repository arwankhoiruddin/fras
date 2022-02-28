package com.fras;

import common.Functions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestCluster {
    @Test
    public void testMatrixConvolution() {
        int[][] b = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}; // kernel
        int[][] a = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        int[][] res = {{9, 18, 27}, {36, 45, 54}, {63, 72, 81}};

        int[][] conv = Functions.convolution(a, b);
        assert (Arrays.deepEquals(res, conv));
    }

    @Test
    public void testSumConvolution() {
        int[][] a = {{1, 4, 5}, {1, 1, 8}, {1, 8, 9}};
        int[][] res = {{9, 18, 27}, {36, 45, 54}, {63, 72, 81}};

        int[][] c = Functions.convolution(a);
        int[][] conv = Functions.convolution(c);
        Functions.printArray(conv);
        assert (Arrays.deepEquals(res, conv));
    }
}

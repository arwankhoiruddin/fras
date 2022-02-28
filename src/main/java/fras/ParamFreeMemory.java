package fras;

public class ParamFreeMemory {
    public static double[] freeMemoryLow = new double[2];
    public static double[] freeMemoryHigh = new double[2];

    public ParamFreeMemory(double lowA, double lowB, double highA, double highB) {
        this.freeMemoryLow[0] = lowA;
        this.freeMemoryLow[1] = lowB;

        this.freeMemoryHigh[0] = highA;
        this.freeMemoryHigh[1] = highB;
    }
}

package fras;

public class ParamFreeDisk {
    public static double[] freeDiskLow = new double[2];
    public static double[] freeDiskHigh = new double[2];

    public ParamFreeDisk(double lowA, double lowB, double highA, double highB) {
        this.freeDiskLow[0] = lowA;
        this.freeDiskLow[1] = lowB;

        this.freeDiskHigh[0] = highA;
        this.freeDiskHigh[1] = highB;
    }
}

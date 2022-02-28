package fras;

public class ParamFreeLink {
    public static double[] freeLinkLow = new double[2];
    public static double[] freeLinkHigh = new double[2];

    public ParamFreeLink(double lowA, double lowB, double highA, double highB) {
        this.freeLinkLow[0] = lowA;
        this.freeLinkLow[1] = lowB;

        this.freeLinkHigh[0] = highA;
        this.freeLinkHigh[1] = highB;
    }
}

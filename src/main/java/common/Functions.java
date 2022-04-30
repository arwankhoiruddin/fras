package common;

import cluster.Cluster;
import cluster.Link;
import mapreduce.Job;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;


import static common.MRConfigs.numNodes;

public class Functions {

    public static void printArray(int[] array) {
        for (int i=0; i<array.length; i++) {
            System.out.print(array[i] + "\t");
        }
        System.out.println();
    }

    public static void printArray(double[] array) {
        for (int i=0; i<array.length; i++) {
            System.out.print(array[i] + "\t");
        }
        System.out.println();
    }

    public static void printArray(Object[] array) {
        for (Object o : array) {
            System.out.print(o + "\t");
        }
    }

    public static void printArray(float[][] array) {
        for (int i=0; i<array.length; i++) {
            for (int j=0; j<array[i].length; j++) {
                System.out.print(array[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void printArray(int[][] array) {
        for (int i=0; i<array.length; i++) {
            for (int j=0; j<array[i].length; j++) {
                System.out.print(array[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void printArray(String arrayName, float[][] array) {
        System.out.println("Variable Name: " + arrayName);
        for (int i=0; i<array.length; i++) {
            for (int j=0; j<array[i].length; j++) {
                System.out.print(array[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void printArrayToFile(String fileName, float[][] array) {
        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<array.length; i++) {
                for (int j=0; j<array[i].length; j++) {
                    sb.append(array[i][j] + ",");
                }
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printList(LinkedList list) {
        System.out.println("Size: " + list.size());
        for (int i=0; i<list.size(); i++) {
            System.out.print(list.get(i) + " ");
        }
        System.out.println();
    }

    public static void printListToFile(String fileName, LinkedList list) {
        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            for (Object o : list) {
                writer.println(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printArrayToFile(String fileName, double[] array) {
        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            for (Object o : array) {
                writer.println(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printArrayToFile(String fileName, double[][] array) {
        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            for (int i=0; i<array.length; i++) {
                for (int j=0; j<array[i].length; j++) {
                    writer.print(array[i][j] + "\t");
                }
                writer.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void plotArray(double[] array, String dataName) {
        LineChart chart = new LineChart("MapRedSim");
        double[] xData = new double[array.length];
        for (int i=0; i<array.length; i++) {
            xData[i] = i;
        }
        chart.addData(dataName, xData, array);
        chart.showChart();
    }

    public static void plotList(LinkedList<Long> list, String dataName) {
        double[] xData = new double[list.size()];
        for (int i=0; i<xData.length; i++) {
            xData[i] = i;
        }

        // convert linkedList into array
        double[] yData = new double[list.size()];
        for (int i=0; i<list.size(); i++) {
            yData[i] = (double) list.get(i);
        }

        LineChart chart = new LineChart("MapRedSim");
        chart.addData(dataName, xData, yData);
        chart.showChart();
    }

    public static void plotList(LinkedList list, String dataName, String xLabel, String yLabel) {
        double[] xData = new double[list.size()];
        for (int i=0; i<xData.length; i++) {
            xData[i] = i;
        }

        // convert linkedList into array
        double[] yData = new double[list.size()];
        for (int i=0; i<list.size(); i++) {
            Long l = (Long) list.get(i);
            yData[i] = (double) l;
        }

        LineChart chart = new LineChart("MapRedSim");
        chart.addData(dataName, xData, yData);
        chart.setXAxisLabel(xLabel);
        chart.setYAxisLabel(yLabel);
        chart.showChart();
    }

    public static void plotListWithAverage(LinkedList list, String dataName) {
        double[] xData = new double[list.size()];
        double[] aveData = new double[list.size()];

        // convert linkedList into array
        double[] yData = new double[list.size()];
        double total = 0;

        for (int i=0; i<list.size(); i++) {
            Long l = (Long) list.get(i);
            yData[i] = (double) l;
            total += yData[i];
        }

        double average = total / yData.length;

        for (int i=0; i<xData.length; i++) {
            xData[i] = i;
            aveData[i] = average;
        }

        LineChart chart = new LineChart("MapRedSim");
        chart.addData(dataName, xData, yData);
        chart.addData("Average", xData, aveData);
        chart.showChart();
    }

    public static LinkedList ArrayToLinkedList(int[] array) {
        LinkedList list = new LinkedList();
        for (double a:array) {
            list.add(a);
        }
        return list;
    }

    public static LinkedList ArrayToLinkedList(double[] array) {
        LinkedList list = new LinkedList();
        for (double a:array) {
            list.add(a);
        }
        return list;
    }

    public static int getNumberOfBlocks(double dataSize) {
        System.out.println("Size of data: " + dataSize + " block size: " + MRConfigs.blockSize);
        int numBlocks = (int) Math.ceil(dataSize*1024 / MRConfigs.blockSize);
        System.out.println("Number of blocks: " + numBlocks);
        return numBlocks;
    }

    public static double fuzzyInference(String fclFile, Map<String, Double> fuzzyVariables, String resultVarName) {
        double value = 0;

        FIS fis = FIS.load(fclFile, true);

        if (fis == null)
            System.err.println("Cannot load file");

        for (Map.Entry<String, Double> entry: fuzzyVariables.entrySet()) {
            fis.setVariable(entry.getKey(), entry.getValue());
        }

        fis.evaluate();
        return fis.getVariable(resultVarName).getValue();

    }

    public static int maxNodeWeight() {
        double[] nodeVal = GNN();
        int idxMax = 0;
        double max = -100;
        for (int i=0; i<nodeVal.length; i++) {
            if (nodeVal[i] > max) {
                max = nodeVal[i];
                idxMax = i;
            }
        }
        return idxMax;
    }

    public static int randGNNRoulette() {
        double[] vals = GNN();
        double total = 0;

        for (int i=0; i<vals.length; i++) {
            total += vals[i];
//            System.out.println(i + "\t" + vals[i]);
        }

        double rand = new Random().nextDouble();
        double portion = 0;
        double temp = 0;

        int i=0;
        for (i=0; i<vals.length; i++) {
            temp += vals[i];
            portion = temp / total;
            if (rand < portion) break;
        }

        return i;
    }

    public static double[] GNN() {
        // find the Graph Values
        double[] nodeVal = new double[MRConfigs.numNodes];
        String fclFile = "gnn.fcl";
        FIS fis = FIS.load(fclFile, true);

        if (fis == null)
            System.err.println("Cannot load file");

        for (int i=0; i<MRConfigs.numNodes; i++) {
            nodeVal[i] = 0;
            for (int j=0; j<MRConfigs.numNodes; j++) {
                fis.setVariable("ping", Cluster.nodes[i].ping(Cluster.nodes[j]));
                fis.setVariable("cpu", Cluster.nodes[i].getCpu());
                fis.setVariable("ram", Cluster.nodes[i].getRam());
                fis.setVariable("neighCPU", Cluster.nodes[j].getCpu());
                fis.setVariable("neighRAM", Cluster.nodes[j].getRam());

                fis.evaluate();
                Variable priority = fis.getVariable("priority");

                nodeVal[i] += fis.getVariable("priority").getValue();
            }
        }
        return nodeVal;
    }

    public static double[][] GNNMatrix() {
        // find the Graph Values
        double[] nodeVal = new double[MRConfigs.numNodes];
        double[][] matrix = new double[MRConfigs.numNodes][MRConfigs.numNodes];

        String fclFile = "gnn.fcl";
        FIS fis = FIS.load(fclFile, true);

        if (fis == null)
            System.err.println("Cannot load file");

        for (int i=0; i<MRConfigs.numNodes; i++) {
            nodeVal[i] = 0;
            for (int j=0; j<MRConfigs.numNodes; j++) {
                fis.setVariable("ping", Cluster.nodes[i].ping(Cluster.nodes[j]));
                fis.setVariable("cpu", Cluster.nodes[i].getCpu());
                fis.setVariable("ram", Cluster.nodes[i].getRam());
                fis.setVariable("neighCPU", Cluster.nodes[j].getCpu());
                fis.setVariable("neighRAM", Cluster.nodes[j].getRam());

                fis.evaluate();
                Variable priority = fis.getVariable("priority");
                matrix[i][j] = priority.getValue();

                System.out.println("Node " + i + " to Node " + j + ": " + priority.getValue());
                System.out.println("CPU: " + Cluster.nodes[i].getCpu() + " RAM: " + Cluster.nodes[i].getRam());
                System.out.println("Ping: " + Cluster.nodes[i].ping(Cluster.nodes[j]));
                System.out.println("Neighbor CPU: " + Cluster.nodes[j].getCpu() + " RAM: " + Cluster.nodes[j].getRam());

                nodeVal[i] += fis.getVariable("priority").getValue();
            }
        }
        return matrix;
    }

    public static int[][] convolution(int[][] a) {
        int width = a.length;
        int[][] c = new int[width][width];
        int[][] kernel = new int[width][width];

        for (int i=0; i<width; i++) {
            for (int j=0; j<width; j++) {
                kernel[i][j] = 1;
            }
        }

        for (int i=0; i<width; i++) {
            for (int j=0; j<width; j++) {
                // sum with values around it
                int sum = 0;
                for (int k=i-1; k<i+1; k++) {
                    for (int l=j-1; l<j+1; l++) {
                        if ((k >= 0 && l >= 0) && (k < width) && l < width)
                            sum = sum + a[k][l] + (a[i][j] * kernel[k][l]);
                    }
                }
                c[i][j] = sum;
            }
        }
        return c;
    }

    public static int[][] convolution(int[][] a, int[][] kernel) {
        int width = a.length;
        int[][] c = new int[width][width];

        for (int i=0; i<width; i++) {
            for (int j=0; j<width; j++) {
                // sum with all values in the kernel
                int sum = 0;
                for (int k=0; k<width; k++) {
                    for (int l=0; l<width; l++) {
                        sum = sum + (a[i][j] * kernel[k][l]);
                    }
                }
                c[i][j] = sum;
            }
        }
        return c;
    }

//    public static double[][] convolution2D(double[][] input,
//                                           int width, int height,
//                                           double[][] kernel,
//                                           int kernelWidth,
//                                           int kernelHeight) {
//        int smallWidth = width - kernelWidth + 1;
//        int smallHeight = height - kernelHeight + 1;
//        double[][] output = new double[smallWidth][smallHeight];
//        for (int i = 0; i < smallWidth; ++i) {
//            for (int j = 0; j < smallHeight; ++j) {
//                output[i][j] = 0;
//            }
//        }
//        for (int i = 0; i < smallWidth; ++i) {
//            for (int j = 0; j < smallHeight; ++j) {
//                output[i][j] = singlePixelConvolution(input, i, j, kernel,
//                        kernelWidth, kernelHeight);
//            }
//        }
//        return output;
//    }

}

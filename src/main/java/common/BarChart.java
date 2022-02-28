package common;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;

public class BarChart extends JFrame {

    private static final long serialVersionUID = 1L;
    DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
    private String title;
    private String xAxis;
    private String yAxis;

    public BarChart(String appTitle) {
        super(appTitle);
        this.title = appTitle;
    }

    public void addData(Comparable columnKey, double value, Comparable rowKey) {
        categoryDataset.addValue(value, rowKey, columnKey);
    }

    public void setXAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public void setYAxis(String yAxis) {
        this.yAxis = yAxis;
    }

    public void showChart() throws Exception {
        CategoryDataset dataset = categoryDataset;

        //Create chart
        JFreeChart chart=ChartFactory.createBarChart(
                this.title, //Chart Title
                this.xAxis, // Category axis
                this.yAxis, // Value axis
                dataset,
                PlotOrientation.VERTICAL,
                true,true,false
        );

        ChartPanel panel=new ChartPanel(chart);
        setContentPane(panel);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

}

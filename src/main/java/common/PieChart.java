package common;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.text.DecimalFormat;

public class PieChart extends JFrame {
    private static final long serialVersionUID = 6294689542092367723L;
    DefaultPieDataset dataset=new DefaultPieDataset();
    String title;

    public PieChart(String title) {
        super(title);
        this.title = title;
    }

    public void addData(String label, double value) {
        dataset.setValue(label, value);
    }

    public void showChart() {
        PieDataset dataset = this.dataset;

        // Create chart
        JFreeChart chart = ChartFactory.createPieChart(
                this.title,
                dataset,
                true,
                true,
                false);

        //Format Label
        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                "Marks {0} : ({2})", new DecimalFormat("0"), new DecimalFormat("0%"));
        ((PiePlot) chart.getPlot()).setLabelGenerator(labelGenerator);

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

}
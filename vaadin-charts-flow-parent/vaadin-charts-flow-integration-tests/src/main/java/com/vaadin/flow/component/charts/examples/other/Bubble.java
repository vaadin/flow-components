package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DataSeriesItem3d;
import com.vaadin.flow.component.charts.model.PlotOptionsBubble;

public class Bubble extends AbstractChartExample {

    @Override
    public void initDemo() {

        Chart chart = new Chart(ChartType.BUBBLE);

        Configuration conf = chart.getConfiguration();
        conf.setTitle((String) null);

        DataSeries dataSeries = new DataSeries("All bubbles shown");
        dataSeries.add(item(9, 81, 13));
        dataSeries.add(item(98, 5, 39));
        dataSeries.add(item(51, 50, 23));
        dataSeries.add(item(41, 22, -36));
        dataSeries.add(item(58, 24, -30));
        dataSeries.add(item(78, 37, -16));
        dataSeries.add(item(55, 56, 3));
        dataSeries.add(item(18, 45, 20));
        dataSeries.add(item(42, 44, -22));
        dataSeries.add(item(3, 52, 9));
        dataSeries.add(item(31, 18, 47));
        dataSeries.add(item(79, 91, 13));
        dataSeries.add(item(93, 23, -27));
        dataSeries.add(item(44, 83, -28));

        PlotOptionsBubble opts = new PlotOptionsBubble();
        opts.setMaxSize("120");
        opts.setMinSize("3");

        conf.setPlotOptions(opts);

        conf.addSeries(dataSeries);

        DataSeries dataSeries2 = new DataSeries("Negative bubbles hidden");
        dataSeries2.add(item(13, 30, 10));
        dataSeries2.add(item(23, 20, -10));
        dataSeries2.add(item(23, 40, 10));
        opts = new PlotOptionsBubble();
        opts.setDisplayNegative(false);
        dataSeries2.setPlotOptions(opts);
        conf.addSeries(dataSeries2);

        add(chart);

    }

    public DataSeriesItem item(int x, int y, int z) {
        DataSeriesItem3d dataSeriesItem = new DataSeriesItem3d();
        dataSeriesItem.setX(x);
        dataSeriesItem.setY(y);
        dataSeriesItem.setZ(z);
        return dataSeriesItem;
    }

}

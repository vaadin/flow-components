package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItemBullet;
import com.vaadin.flow.component.charts.model.PlotBand;
import com.vaadin.flow.component.charts.model.PlotOptionsBullet;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;

public class Bullet extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart revenue = createBulletChart(0, 150, 225, 9e9, 275, 250,
                "<span style=\"font-size: 13px; font-weight: bold;\">Revenue</span><br/>U.S. $ (1,000s)");
        revenue.setHeight("115px");
        revenue.getConfiguration().getChart().setMarginTop(40);
        revenue.getConfiguration().setTitle("2017 YTD");
        Chart profit = createBulletChart(0, 20, 25, 100, 22, 27,
                "<span style=\"font-size: 13px; font-weight: bold;\">Profit</span><br/>%");
        Chart newCustomers = createBulletChart(0, 1400, 2000, 9e9, 1650, 2100,
                "<span style=\"font-size: 13px; font-weight: bold;\">New Customers</span><br/>Count");
        add(revenue);
        add(profit);
        add(newCustomers);
    }

    /**
     * Create a bullet chart with shared configuration
     *
     * @param plotBandY1
     *            "from" value for the first plotband
     * @param plotBandY2
     *            "to" value for the first plotband and "from" value for the
     *            second plotband
     * @param plotBandY3
     *            "to" value for the second plotband and "from" value for the
     *            third plotband
     * @param plotBandY4
     *            "to" value for the third plotband
     * @param y
     *            Y value for the series item
     * @param target
     *            Target value for the series item
     * @param category
     *            Title to be used in the category
     * @return Chart with shared configuration
     */
    private Chart createBulletChart(Number plotBandY1, Number plotBandY2,
            Number plotBandY3, Number plotBandY4, Number y, Number target,
            String category) {
        Chart chart = new Chart(ChartType.BULLET);
        chart.setHeight("85px");
        Configuration conf = chart.getConfiguration();
        conf.getChart().setInverted(true);
        conf.getChart().setMarginLeft(135);
        conf.getLegend().setEnabled(false);
        YAxis yAxis = conf.getyAxis();
        yAxis.setGridLineWidth(0);
        yAxis.setTitle("");
        yAxis.addPlotBand(new PlotBand(plotBandY1, plotBandY2,
                new SolidColor("#666666")));
        yAxis.addPlotBand(new PlotBand(plotBandY2, plotBandY3,
                new SolidColor("#999999")));
        yAxis.addPlotBand(new PlotBand(plotBandY3, plotBandY4,
                new SolidColor("#bbbbbb")));
        conf.getxAxis().addCategory(category);
        conf.getTooltip().setPointFormat(
                "<b>{point.y}</b> (with target at {point.target})");
        PlotOptionsBullet options = new PlotOptionsBullet();
        options.setPointPadding(0.25);
        options.setBorderWidth(0);
        options.setColor(SolidColor.BLACK);
        options.getTargetOptions().setWidth("200%");
        conf.setExporting(false);
        DataSeries series = new DataSeries();
        series.add(new DataSeriesItemBullet(y, target));
        series.setPlotOptions(options);
        conf.addSeries(series);
        return chart;

    }

}

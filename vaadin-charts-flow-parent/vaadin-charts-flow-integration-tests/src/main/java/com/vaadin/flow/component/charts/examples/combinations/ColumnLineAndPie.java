package com.vaadin.flow.component.charts.examples.combinations;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.HTMLLabelItem;
import com.vaadin.flow.component.charts.model.HTMLLabels;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.PlotOptionsSpline;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.style.Style;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport(value = "./styles/ColumnLineAndPie.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
public class ColumnLineAndPie extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart();

        Configuration conf = chart.getConfiguration();

        conf.setTitle("Combined Chart");

        conf.setExporting(true);

        XAxis x = new XAxis();
        x.setCategories(new String[] { "Apples", "Oranges", "Pears", "Bananas",
                "Plums" });
        conf.addxAxis(x);

        Style labelStyle = new Style();
        labelStyle.setTop("8px");
        labelStyle.setLeft("40px");
        conf.setLabels(new HTMLLabels(labelStyle,
                new HTMLLabelItem("Total fruit consumption")));

        DataSeries series = new DataSeries();
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        series.setPlotOptions(plotOptions);
        series.setName("Jane");
        series.setData(3, 2, 1, 3, 4);
        conf.addSeries(series);

        series = new DataSeries();
        plotOptions = new PlotOptionsColumn();
        series.setPlotOptions(plotOptions);
        series.setName("John");
        series.setData(2, 3, 5, 7, 6);
        conf.addSeries(series);

        series = new DataSeries();
        plotOptions = new PlotOptionsColumn();
        series.setPlotOptions(plotOptions);
        series.setName("Joe");
        series.setData(4, 3, 3, 9, 0);
        conf.addSeries(series);

        series = new DataSeries();
        PlotOptionsSpline splinePlotOptions = new PlotOptionsSpline();
        series.setPlotOptions(splinePlotOptions);
        series.setName("Average");
        series.setData(3, 2.67, 3, 6.33, 3.33);
        conf.addSeries(series);

        series = new DataSeries();
        series.setPlotOptions(new PlotOptionsPie());
        series.setName("Total consumption");
        DataSeriesItem item = new DataSeriesItem("Jane", 13);
        series.add(item);
        item = new DataSeriesItem("John", 23);
        series.add(item);
        item = new DataSeriesItem("Joe", 19);
        series.add(item);

        PlotOptionsPie plotOptionsPie = new PlotOptionsPie();
        plotOptionsPie.setSize("100px");
        plotOptionsPie.setCenter("100px", "80px");
        plotOptionsPie.setShowInLegend(false);
        series.setPlotOptions(plotOptionsPie);
        conf.addSeries(series);

        add(chart);
    }
}

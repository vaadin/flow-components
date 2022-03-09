package com.vaadin.flow.component.charts.examples.column;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Cursor;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DrilldownCallback;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.html.Div;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ColumnWithLazyMultiLevelDrilldown extends AbstractChartExample {

    private Map<String, DataSeries> drillSeries;
    private Div log;

    @Override
    public void initDemo() {
        log = new Div();
        log.setId("log");
        Div layout = new Div();
        final Chart chart = new Chart(ChartType.COLUMN);
        chart.setId("chart");

        final Configuration conf = chart.getConfiguration();

        conf.setTitle("Global happiness index");
        conf.setSubTitle("Source: www.happyplanetindex.org");
        conf.getLegend().setEnabled(false);

        XAxis x = new XAxis();
        x.setType(AxisType.CATEGORY);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Total");
        conf.addyAxis(y);

        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setCursor(Cursor.POINTER);
        column.setDataLabels(new DataLabels(true));

        conf.setPlotOptions(column);

        DataSeries series = new DataSeries();
        series.setName("Regions");
        PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
        plotOptionsColumn.setColorByPoint(true);
        series.setPlotOptions(plotOptionsColumn);

        DataSeriesItem item = new DataSeriesItem("Latin America and Caribbean",
                60);
        item.setId("Latin America and Caribbean");
        series.addItemWithDrilldown(item);

        item = new DataSeriesItem("Europe", 50);
        item.setId("Europe");
        series.addItemWithDrilldown(item);

        conf.addSeries(series);

        drillSeries = new HashMap<String, DataSeries>();

        DataSeries drill = new DataSeries(
                "Latin America and Caribbean Countries");
        drill.setId("Latin America and Caribbean Countries");

        item = new DataSeriesItem("Costa Rica", 64);
        item.setId("Costa Rica");
        drill.addItemWithDrilldown(item);

        item = new DataSeriesItem("Colombia", 59.8);
        item.setId("Colombia");
        drill.addItemWithDrilldown(item);

        item = new DataSeriesItem("Belize", 59.3);
        item.setId("Belize");
        drill.addItemWithDrilldown(item);

        drillSeries.put("Latin America and Caribbean", drill);

        drill = new DataSeries("Europe");
        drill.setId("European Countries");

        item = new DataSeriesItem("Norway", 51.4);
        item.setId("Norway");
        drill.addItemWithDrilldown(item);

        item = new DataSeriesItem("Switzerland", 50.3);
        item.setId("Switzerland");
        drill.addItemWithDrilldown(item);

        item = new DataSeriesItem("Portugal", 38.7);
        item.setId("Portugal");
        drill.addItemWithDrilldown(item);

        drillSeries.put("Europe", drill);

        drill = new DataSeries("Details Costa Rica");
        drill.setId("Details Costa Rica");
        final String[] categories = new String[] { "Life Expectancy",
                "Well-being (0-10)", "Footprint (gha/capita)" };
        Number[] ys = new Number[] { 79.3, 7.3, 2.5 };
        drill.setData(categories, ys);
        drillSeries.put("Costa Rica", drill);

        drill = new DataSeries("Details Colombia");
        drill.setId("Details Colombia");
        ys = new Number[] { 73.7, 6.4, 1.8 };
        drill.setData(categories, ys);
        drillSeries.put("Colombia", drill);

        drill = new DataSeries("Details Belize");
        drill.setId("Details Belize");
        ys = new Number[] { 76.1, 6.5, 2.1 };
        drill.setData(categories, ys);
        drillSeries.put("Belize", drill);

        drill = new DataSeries("Details Norway");
        drill.setId("Details Norway");
        ys = new Number[] { 81.1, 7.6, 4.8 };
        drill.setData(categories, ys);
        drillSeries.put("Norway", drill);

        drill = new DataSeries("Details Switzerland");
        drill.setId("Details Switzerland");
        ys = new Number[] { 82.3, 7.5, 5.0 };
        drill.setData(categories, ys);
        drillSeries.put("Switzerland", drill);

        drill = new DataSeries("Details Portugal");
        drill.setId("Details Portugal");
        ys = new Number[] { 79.5, 4.9, 4.1 };
        drill.setData(categories, ys);
        drillSeries.put("Portugal", drill);

        chart.setDrilldownCallback((DrilldownCallback) event -> {
            log("DrilldownEvent: " + event.getItem().getId());
            return getPointDrilldown(event.getItem());
        });

        chart.addChartDrillupListener(event -> log("ChartDrillupEvent"));

        layout.add(chart, log);
        add(layout);
    }

    private void log(String newStringValue) {
        log.removeAll();
        log.add(new Text(newStringValue));
    }

    private Series getPointDrilldown(DataSeriesItem point) {
        String pointId = point.getId();
        DataSeries series = drillSeries.get(pointId);
        return series;
    }

}

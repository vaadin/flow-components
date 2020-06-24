package com.vaadin.flow.component.charts.examples.column;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.events.DrilldownEvent;
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
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.SkipFromDemo;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
@SkipFromDemo
public class ColumnWithNativeLazyDrilldownMultipleSeries
    extends AbstractChartExample {

    private Map<String, DataSeries> drillSeries;
    private Configuration conf;

    @Override
    public void initDemo() {
        final Chart chart = new Chart(ChartType.COLUMN);
        chart.setId("chart");

        conf = chart.getConfiguration();

        conf.setTitle("Browser market share, April, 2011");
        conf.setSubTitle(
            "Click the columns to view versions. Click again to view brands.");
        conf.getLegend().setEnabled(false);

        XAxis x = new XAxis();
        x.setType(AxisType.CATEGORY);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Total percent market share");
        conf.addyAxis(y);

        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setCursor(Cursor.POINTER);
        column.setDataLabels(new DataLabels(true));

        conf.setPlotOptions(column);

        Tooltip tooltip = new Tooltip();
        tooltip.setHeaderFormat(
            "<span style=\"font-size:11px\">{series.name}</span><br>");
        tooltip.setPointFormat(
            "<span style=\"color:{point.color}\">{point.name}</span>: <b>{point.y:.2f}%</b> of total<br/>");
        conf.setTooltip(tooltip);

        createSeries(1);
        createSeries(2);

        drillSeries = new HashMap<>();

        addDrillSeries(1);
        addDrillSeries(2);

        chart.setDrilldownCallback(event -> getPointDrilldown(event.getItem()));
        add(chart);
    }

    private void createSeries(int index) {
        DataSeries series = new DataSeries();
        DataSeriesItem item = new DataSeriesItem("MSIE",
            55.11 - index - index - index);
        item.setId("MSIE" + index);
        series.addItemWithDrilldown(item);

        item = new DataSeriesItem("Firefox", 21.63 - index);
        item.setId("Firefox" + index);
        series.addItemWithDrilldown(item);

        item = new DataSeriesItem("Chrome", 11.94 + index);
        item.setId("Chrome" + index);
        series.addItemWithDrilldown(item);

        item = new DataSeriesItem("Safari", 7.15 + index + index);
        item.setId("Safari" + index);
        series.addItemWithDrilldown(item);

        item = new DataSeriesItem("Opera", 2.14 + index);
        item.setId("Opera" + index);
        series.addItemWithDrilldown(item);

        conf.addSeries(series);

    }

    private void addDrillSeries(int index) {
        DataSeries drill = new DataSeries("MSIE versions" + index);
        String[] categories = new String[] { "MSIE 6.0", "MSIE 7.0", "MSIE 8.0",
            "MSIE 9.0" };
        Number[] ys = new Number[] { 10.85, 7.35, 33.06, 2.81 };
        drill.setData(categories, ys);
        drill.setId("MSIE" + index);
        drillSeries.put("MSIE" + index, drill);

        drill = new DataSeries("Firefox versions" + index);
        categories = new String[] { "Firefox 2.0", "Firefox 3.0", "Firefox 3.5",
            "Firefox 3.6", "Firefox 4.0" };
        ys = new Number[] { 0.20, 0.83, 1.58, 13.12, 5.43 };
        drill.setData(categories, ys);
        drill.setId("Firefox" + index);
        drillSeries.put("Firefox" + index, drill);

        drill = new DataSeries("Chrome versions" + index);
        categories = new String[] { "Chrome 5.0", "Chrome 6.0", "Chrome 7.0",
            "Chrome 8.0", "Chrome 9.0", "Chrome 10.0", "Chrome 11.0",
            "Chrome 12.0" };
        ys = new Number[] { 0.12, 0.19, 0.12, 0.36, 0.32, 9.91, 0.50, 0.22 };
        drill.setData(categories, ys);
        drill.setId("Chrome" + index);
        drillSeries.put("Chrome" + index, drill);

        drill = new DataSeries("Safari versions" + index);
        categories = new String[] { "Safari 5.0", "Safari 4.0",
            "Safari Win 5.0", "Safari 4.1", "Safari/Maxthon", "Safari 3.1",
            "Safari 4.1" };
        ys = new Number[] { 4.55, 1.42, 0.23, 0.21, 0.20, 0.19, 0.14 };
        drill.setData(categories, ys);
        drill.setId("Safari" + index);
        drillSeries.put("Safari" + index, drill);

        drill = new DataSeries("Opera versions" + index);
        categories = new String[] { "Opera 9.x", "Opera 10.x", "Opera 11.x" };
        ys = new Number[] { 0.12, 0.37, 1.65 };
        drill.setData(categories, ys);
        drill.setId("Opera" + index);
        drillSeries.put("Opera" + index, drill);

    }

    private Series getPointDrilldown(DataSeriesItem item) {
        return drillSeries.get(item.getId());
    }
}

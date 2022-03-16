package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsErrorbar;
import com.vaadin.flow.component.charts.model.RangeSeries;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.model.SeriesTooltip;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

public class ErrorBar extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Monthly Rainfall");

        XAxis x = new XAxis();
        x.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec");
        configuration.addxAxis(x);

        YAxis y = new YAxis();
        Labels yLabels = new Labels();
        yLabels.setFormat("{value} mm");
        y.setLabels(yLabels);
        y.setTitle("Rainfall");
        configuration.addyAxis(y);

        Tooltip tooltip = new Tooltip();
        tooltip.setShared(true);
        configuration.setTooltip(tooltip);

        Series rainfall = new ListSeries("Rainfall", 49.9, 71.5, 106.4, 129.2,
                144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4);
        PlotOptionsColumn rainfallOptions = new PlotOptionsColumn();
        SeriesTooltip rainfallTooltip = new SeriesTooltip();
        rainfallTooltip.setPointFormat(
                "<span style=\"font-weight: bold; color: {series.color}\">"
                        + "{series.name}</span>: <b>{point.y:.1f} mm</b> ");
        rainfallOptions.setTooltip(rainfallTooltip);
        rainfall.setPlotOptions(rainfallOptions);
        configuration.addSeries(rainfall);

        Series error = new RangeSeries("Rainfall error",
                new Number[] { 48, 51 }, new Number[] { 68, 73 },
                new Number[] { 92, 110 }, new Number[] { 128, 136 },
                new Number[] { 140, 150 }, new Number[] { 171, 179 },
                new Number[] { 135, 143 }, new Number[] { 142, 149 },
                new Number[] { 204, 220 }, new Number[] { 189, 199 },
                new Number[] { 95, 110 }, new Number[] { 52, 56 });
        PlotOptionsErrorbar errorOptions = new PlotOptionsErrorbar();
        SeriesTooltip errorTooltip = new SeriesTooltip();
        errorTooltip.setPointFormat(
                "(error range: {point.low}-{point.high} mm)<br/>");
        errorOptions.setTooltip(errorTooltip);
        error.setPlotOptions(errorOptions);
        configuration.addSeries(error);

        add(chart);
    }
}

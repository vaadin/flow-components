/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.Background;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsSolidgauge;
import com.vaadin.flow.component.charts.model.Position;
import com.vaadin.flow.component.charts.model.TextAlign;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.charts.model.style.Style;
import com.vaadin.flow.router.Route;

/**
 * Reproduces the Highcharts "Multiple KPI gauge" demo
 * (https://www.highcharts.com/demo/highcharts/gauge-multiple-kpi) using the
 * Flow API.
 * <p>
 * The FontAwesome icons from the original demo are omitted: they are drawn in a
 * {@code chart.events.render} callback via the SVG renderer, which Flow does
 * not expose.
 */
@Route("vaadin-charts/other/solid-gauge-multiple-kpi")
public class SolidGaugeMultipleKPI extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.SOLIDGAUGE);
        // Fixed size so the gauge does not grow with the viewport width
        chart.setWidth("500px");
        chart.setHeight("500px");

        Configuration configuration = chart.getConfiguration();
        configuration.getChart().setHeight("110%");
        configuration.setTitle("Multiple KPI gauge");

        // Full-circle pane with three concentric tracks
        Pane pane = configuration.getPane();
        pane.setStartAngle(0);
        pane.setEndAngle(360);

        pane.setBackground(
                track("100%", "78%", new SolidColor(124, 181, 236, 0.3)),
                track("77%", "56%", new SolidColor(67, 67, 72, 0.3)),
                track("55%", "34%", new SolidColor(144, 237, 125, 0.3)));

        YAxis yAxis = configuration.getyAxis();
        yAxis.setMin(0);
        yAxis.setMax(100);
        yAxis.setLineWidth(0);
        yAxis.setTickPositions(new Number[0]);

        PlotOptionsSolidgauge plotOptions = new PlotOptionsSolidgauge();
        plotOptions.getDataLabels().setEnabled(false);
        plotOptions.setLinecap("round");
        plotOptions.setStickyTracking(false);
        plotOptions.setRounded(true);
        configuration.setPlotOptions(plotOptions);

        Tooltip tooltip = new Tooltip();
        tooltip.setValueSuffix("%");
        tooltip.setBackgroundColor(new SolidColor("none"));
        tooltip.setBorderWidth(0);
        tooltip.setShadow(false);
        tooltip.setFixed(true);

        Position position = new Position();
        position.setHorizontalAlign(HorizontalAlign.CENTER);
        position.setVerticalAlign(VerticalAlign.MIDDLE);
        tooltip.setPosition(position);

        tooltip.setPointFormat("{series.name}<br>"
                + "<span style=\"font-size:2em; color:{point.color}; font-weight:bold\">{point.y}</span>");

        Style style = new Style();
        style.setFontSize("16px");
        style.setTextAlign(TextAlign.CENTER);
        tooltip.setStyle(style);

        configuration.setTooltip(tooltip);

        configuration.addSeries(kpiSeries("Conversion", 80, "100%", "78%",
                new SolidColor(124, 181, 236)));
        configuration.addSeries(kpiSeries("Engagement", 65, "77%", "56%",
                new SolidColor(67, 67, 72)));
        configuration.addSeries(kpiSeries("Feedback", 50, "55%", "34%",
                new SolidColor(144, 237, 125)));

        add(chart);
    }

    private static Background track(String outerRadius, String innerRadius,
            SolidColor color) {
        Background background = new Background();
        background.setOuterRadius(outerRadius);
        background.setInnerRadius(innerRadius);
        background.setBackgroundColor(color);
        background.setBorderWidth(0);
        return background;
    }

    private static DataSeries kpiSeries(String name, Number value,
            String radius, String innerRadius, SolidColor color) {
        DataSeriesItem item = new DataSeriesItem();
        item.setY(value);
        item.setColor(color);

        // Ring placement now comes from per-series PlotOptionsSolidgauge
        PlotOptionsSolidgauge seriesOptions = new PlotOptionsSolidgauge();
        seriesOptions.setRadius(radius);
        seriesOptions.setInnerRadius(innerRadius);

        DataSeries series = new DataSeries(name);
        series.setPlotOptions(seriesOptions);
        series.add(item);
        return series;
    }
}

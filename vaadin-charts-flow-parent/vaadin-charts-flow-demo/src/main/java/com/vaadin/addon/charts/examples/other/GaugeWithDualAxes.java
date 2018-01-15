package com.vaadin.addon.charts.examples.other;

import java.util.Random;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.AbstractChartExample;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataLabels;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotOptionsGauge;
import com.vaadin.addon.charts.model.SeriesTooltip;
import com.vaadin.addon.charts.model.TickPosition;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;

public class GaugeWithDualAxes extends AbstractChartExample {

    @Override
    public void initDemo() {
        final Random random = new Random(0);
        final Chart chart = new Chart();

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.GAUGE);
        configuration.getChart().setAlignTicks(false);
        configuration.setTitle("Speedometer with dual axes");
        configuration.getChart().setWidth(500);

        configuration.getPane().setStartAngle(-150);
        configuration.getPane().setEndAngle(150);

        YAxis yAxis = new YAxis();
        yAxis.setClassName("kmh");
        yAxis.setMin(0);
        yAxis.setMax(200);
        yAxis.setOffset(-25);
        Labels labels = new Labels();
        labels.setDistance(-20);
        labels.setRotationPerpendicular();
        yAxis.setLabels(labels);
        yAxis.setTickLength(5);
        yAxis.setMinorTickLength(5);
        yAxis.setEndOnTick(false);

        YAxis yAxis2 = new YAxis();
        yAxis2.setClassName("mph");
        yAxis2.setMin(0);
        yAxis2.setMax(124);
        yAxis2.setOffset(-20);
        labels = new Labels();
        labels.setDistance(12);
        labels.setRotationPerpendicular();
        yAxis2.setLabels(labels);
        yAxis2.setTickLength(5);
        yAxis2.setMinorTickLength(5);
        yAxis2.setEndOnTick(false);
        yAxis2.setTickPosition(TickPosition.OUTSIDE);
        yAxis2.setMinorTickPosition(TickPosition.OUTSIDE);

        configuration.addyAxis(yAxis);
        configuration.addyAxis(yAxis2);

        final ListSeries series = new ListSeries("Speed", 80);

        PlotOptionsGauge plotOptionsGauge = new PlotOptionsGauge();
        plotOptionsGauge.setDataLabels(new DataLabels());
        plotOptionsGauge
                .getDataLabels()
                .setFormatter(
                        "function() {return '<span class=\"kmh\">'+ this.y + ' km/h</span><br/>' + '<span class=\"mph\">' + Math.round(this.y * 0.621) + ' mph</span>';}");
        plotOptionsGauge.setTooltip(new SeriesTooltip());
        plotOptionsGauge.getTooltip().setValueSuffix(" km/h");
        series.setPlotOptions(plotOptionsGauge);

        configuration.setSeries(series);

        runWhileAttached(chart, () -> {
            Integer oldValue = series.getData()[0].intValue();
            Integer newValue = (int) (oldValue + (random.nextDouble() - 0.5) * 20.0);
            series.updatePoint(0, newValue);
        }, 5000, 12000);

        chart.drawChart();

        add(chart);
    }
}

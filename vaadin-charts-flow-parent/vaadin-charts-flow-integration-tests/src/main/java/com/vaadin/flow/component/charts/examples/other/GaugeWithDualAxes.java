package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsGauge;
import com.vaadin.flow.component.charts.model.SeriesTooltip;
import com.vaadin.flow.component.charts.model.TickPosition;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;

import java.util.Random;

@CssImport(value = "./styles/GaugeWithDualAxes.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
public class GaugeWithDualAxes extends AbstractChartExample {

    @Override
    public void initDemo() {
        final Random random = new Random(0); // NOSONAR
        final Chart chart = new Chart();

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.GAUGE);
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
        labels.setRotation("auto");
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
        labels.setRotation("auto");
        yAxis2.setLabels(labels);
        yAxis2.setTickPosition(TickPosition.OUTSIDE);
        yAxis2.setMinorTickPosition(TickPosition.OUTSIDE);
        yAxis2.setTickLength(5);
        yAxis2.setMinorTickLength(5);
        yAxis2.setEndOnTick(false);

        configuration.addyAxis(yAxis);
        configuration.addyAxis(yAxis2);

        final ListSeries series = new ListSeries("Speed", 80);

        PlotOptionsGauge plotOptionsGauge = new PlotOptionsGauge();
        plotOptionsGauge.setDataLabels(new DataLabels());
        plotOptionsGauge.getDataLabels().setFormatter(
                "function() {return '<span class=\"kmh\">'+ this.y + ' km/h</span><br/>' + '<span class=\"mph\">' + Math.round(this.y * 0.621) + ' mph</span>';}");
        plotOptionsGauge.setTooltip(new SeriesTooltip());
        plotOptionsGauge.getTooltip().setValueSuffix(" km/h");
        series.setPlotOptions(plotOptionsGauge);

        configuration.addSeries(series);

        runWhileAttached(chart, () -> {
            Integer oldValue = series.getData()[0].intValue();
            Integer newValue = (int) (oldValue
                    + (random.nextDouble() - 0.5) * 20.0);
            series.updatePoint(0, newValue);
        }, 5000, 12000);

        chart.drawChart();

        add(chart);
    }
}

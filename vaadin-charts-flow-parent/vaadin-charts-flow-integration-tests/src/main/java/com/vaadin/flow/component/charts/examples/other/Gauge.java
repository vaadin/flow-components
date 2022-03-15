package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotBand;
import com.vaadin.flow.component.charts.model.PlotOptionsGauge;
import com.vaadin.flow.component.charts.model.SeriesTooltip;
import com.vaadin.flow.component.charts.model.TickPosition;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;

import java.util.Random;

@CssImport(value = "./styles/Gauge.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
public class Gauge extends AbstractChartExample {

    @Override
    public void initDemo() {
        final Random random = new Random(0); // NOSONAR
        final Chart chart = new Chart();

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.GAUGE);
        configuration.setTitle("Speedometer");
        configuration.getChart().setWidth(500);

        Pane pane = configuration.getPane();
        pane.setStartAngle(-150);
        pane.setEndAngle(150);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("km/h");
        yAxis.setMin(0);
        yAxis.setMax(200);
        yAxis.setTickLength(10);
        yAxis.setTickPixelInterval(30);
        yAxis.setTickPosition(TickPosition.INSIDE);
        yAxis.setMinorTickLength(10);
        yAxis.setMinorTickInterval("auto");
        yAxis.setMinorTickPosition(TickPosition.INSIDE);

        Labels labels = new Labels();
        labels.setStep(2);
        labels.setRotation("auto");
        yAxis.setLabels(labels);

        PlotBand[] bands = new PlotBand[3];
        bands[0] = new PlotBand();
        bands[0].setFrom(0);
        bands[0].setTo(120);
        bands[0].setClassName("band-0");
        bands[1] = new PlotBand();
        bands[1].setFrom(120);
        bands[1].setTo(160);
        bands[1].setClassName("band-1");
        bands[2] = new PlotBand();
        bands[2].setFrom(160);
        bands[2].setTo(200);
        bands[2].setClassName("band-2");
        yAxis.setPlotBands(bands);

        configuration.addyAxis(yAxis);

        final ListSeries series = new ListSeries("Speed", 89);

        PlotOptionsGauge plotOptionsGauge = new PlotOptionsGauge();
        SeriesTooltip tooltip = new SeriesTooltip();
        tooltip.setValueSuffix(" km/h");
        plotOptionsGauge.setTooltip(tooltip);
        series.setPlotOptions(plotOptionsGauge);

        configuration.addSeries(series);

        runWhileAttached(chart, () -> {
            Integer oldValue = series.getData()[0].intValue();
            Integer newValue = (int) (oldValue
                    + (random.nextDouble() - 0.5) * 20.0);
            series.updatePoint(0, newValue);
        }, 5000, 12000);

        add(chart);
    }
}

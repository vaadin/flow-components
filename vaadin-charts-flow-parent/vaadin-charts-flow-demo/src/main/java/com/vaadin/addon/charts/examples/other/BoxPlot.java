package com.vaadin.addon.charts.examples.other;

import com.vaadin.addon.charts.AbstractChartExample;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.BoxPlotItem;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.Label;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.PlotLine;
import com.vaadin.addon.charts.model.PlotOptionsBoxplot;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.flow.component.checkbox.Checkbox;

public class BoxPlot extends AbstractChartExample {

    @Override
    public void initDemo() {
        final Chart chart = new Chart();

        chart.getConfiguration().setTitle("Box Plot Example");

        Legend legend = new Legend();
        legend.setEnabled(false);
        chart.getConfiguration().setLegend(legend);

        XAxis xaxis = chart.getConfiguration().getxAxis();
        xaxis.setTitle("Experiment No.");
        xaxis.setCategories("1", "2", "3", "4", "5");

        YAxis yAxis = chart.getConfiguration().getyAxis();
        yAxis.setTitle("Observations");

        PlotLine plotLine = new PlotLine();
        plotLine.setValue(932);
        plotLine.setZIndex(0);
        Label label = new Label("Theoretical mean: 932");
        label.setAlign(HorizontalAlign.CENTER);
        plotLine.setLabel(label);

        PlotLine plotLine2 = new PlotLine();
        plotLine2.setValue(800);
        plotLine2.setZIndex(500);
        Label label2 = new Label("Second plotline: 800");
        label2.setAlign(HorizontalAlign.CENTER);
        plotLine2.setLabel(label2);

        yAxis.setPlotLines(plotLine, plotLine2);

        final DataSeries observations = new DataSeries();
        observations.setName("Observations");

        // Add PlotBoxItems contain all fields relevant for plot box chart
        observations.add(new BoxPlotItem(760, 801, 848, 895, 965));

        // Example with no arg constructor
        BoxPlotItem plotBoxItem = new BoxPlotItem();
        plotBoxItem.setLow(733);
        plotBoxItem.setLowerQuartile(853);
        plotBoxItem.setMedian(939);
        plotBoxItem.setUpperQuartile(980);
        plotBoxItem.setHigh(1080);
        observations.add(plotBoxItem);

        observations.add(new BoxPlotItem(714, 762, 817, 870, 918));
        observations.add(new BoxPlotItem(724, 802, 806, 871, 950));
        observations.add(new BoxPlotItem(834, 836, 864, 882, 910));
        observations.setPlotOptions(new PlotOptionsBoxplot());
        chart.getConfiguration().addSeries(observations);

        Checkbox useCustomStyles = new Checkbox("Use custom styling");
        useCustomStyles.addValueChangeListener(e -> {

            PlotOptionsBoxplot options = new PlotOptionsBoxplot();
            if (e.getValue()) {
                options.setClassName("custom-style");
                options.setWhiskerLength("70");
            }
            observations.setPlotOptions(options);

            chart.drawChart(true);
        });

        add(chart, useCustomStyles);
    }
}

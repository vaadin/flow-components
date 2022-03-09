package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.BoxPlotItem;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.Label;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.PlotLine;
import com.vaadin.flow.component.charts.model.PlotOptionsBoxplot;
import com.vaadin.flow.component.charts.model.PlotOptionsScatter;
import com.vaadin.flow.component.charts.model.SeriesTooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport(value = "./styles/BoxPlot.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
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
        yAxis.setPlotLines(plotLine);

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
        PlotOptionsBoxplot plotOptions = new PlotOptionsBoxplot();
        SeriesTooltip observationsTooltip = new SeriesTooltip();
        observationsTooltip
                .setHeaderFormat("<em>Experiment No {point.key}</em><br/>");
        plotOptions.setTooltip(observationsTooltip);
        observations.setPlotOptions(plotOptions);
        chart.getConfiguration().addSeries(observations);

        final DataSeries outlier = new DataSeries();
        outlier.setName("Outlier");

        outlier.add(new DataSeriesItem(0, 644));
        outlier.add(new DataSeriesItem(4, 718));
        outlier.add(new DataSeriesItem(4, 951));
        outlier.add(new DataSeriesItem(4, 969));

        PlotOptionsScatter outlierOptions = new PlotOptionsScatter();
        SeriesTooltip outlierTooltip = new SeriesTooltip();
        outlierTooltip.setPointFormat("Observation: {point.y}");
        outlierOptions.setTooltip(outlierTooltip);
        outlier.setPlotOptions(outlierOptions);

        chart.getConfiguration().addSeries(outlier);

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

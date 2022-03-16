package com.vaadin.flow.component.charts.examples.bar;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

public class BarChart extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Historic World Population by Region");
        configuration.setSubTitle(
                "Source: <a href=\"https://en.wikipedia.org/wiki/World_population\">Wikipedia.org</a>");
        chart.getConfiguration().getChart().setType(ChartType.BAR);

        configuration
                .addSeries(new ListSeries("Year 1800", 107, 31, 635, 203, 2));
        configuration
                .addSeries(new ListSeries("Year 1900", 133, 156, 947, 408, 6));
        configuration.addSeries(
                new ListSeries("Year 2000", 814, 841, 3714, 727, 31));
        configuration.addSeries(
                new ListSeries("Year 2016", 1216, 1001, 4436, 738, 40));

        XAxis x = new XAxis();
        x.setCategories("Africa", "America", "Asia", "Europe", "Oceania");
        configuration.addxAxis(x);

        YAxis y = new YAxis();
        y.setMin(0);
        AxisTitle yTitle = new AxisTitle();
        yTitle.setText("Population (millions)");
        yTitle.setAlign(VerticalAlign.HIGH);
        y.setTitle(yTitle);
        configuration.addyAxis(y);

        Tooltip tooltip = new Tooltip();
        tooltip.setValueSuffix(" millions");
        configuration.setTooltip(tooltip);

        PlotOptionsBar plotOptions = new PlotOptionsBar();
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        plotOptions.setDataLabels(dataLabels);
        configuration.setPlotOptions(plotOptions);

        add(chart);
    }
}

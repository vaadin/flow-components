package com.vaadin.flow.component.charts.examples.lineandscatter;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.Shape;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport(value = "./styles/BasicLineWithCallouts.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
public class BasicLineWithCallouts extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.LINE);

        configuration.getTitle()
                .setText("CALLOUT: Monthly Average Temperature");
        configuration.getSubTitle().setText("Source: WorldClimate.com");

        configuration.getxAxis().setCategories("Jan", "Feb", "Mar", "Apr",
                "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle(new AxisTitle("Temperature (°C)"));

        configuration.getTooltip().setFormatter(
                "'<b>'+ this.series.name +'</b><br/>'+this.x +': '+ this.y +'°C'");

        PlotOptionsLine plotOptions = new PlotOptionsLine();
        plotOptions.setEnableMouseTracking(false);
        configuration.setPlotOptions(plotOptions);

        DataSeries ds = new DataSeries();
        ds.setName("Tokyo");
        ds.setData(7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3,
                13.9, 9.6);
        DataLabels callout = new DataLabels(true);
        callout.setShape(Shape.CALLOUT);
        callout.setY(-12);
        ds.get(5).setDataLabels(callout);
        configuration.addSeries(ds);

        ds = new DataSeries();
        ds.setName("London");
        ds.setData(3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6,
                4.8);
        ds.get(6).setDataLabels(callout);
        configuration.addSeries(ds);

        add(chart);
    }

}

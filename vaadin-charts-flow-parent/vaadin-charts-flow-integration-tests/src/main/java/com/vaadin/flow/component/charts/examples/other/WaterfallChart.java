package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsWaterfall;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.WaterFallSum;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport(value = "./styles/WaterfallChart.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
public class WaterfallChart extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.WATERFALL);

        DataSeries dataSeries = new DataSeries();

        dataSeries.add(new DataSeriesItem("Start", 120000));
        dataSeries.add(new DataSeriesItem("Product Revenue", 569000));
        dataSeries.add(new DataSeriesItem("Service Revenue", 231000));
        WaterFallSum positiveBalance = new WaterFallSum("Positive Balance");
        positiveBalance.setIntermediate(true);
        dataSeries.add(positiveBalance);

        dataSeries.add(new DataSeriesItem("Fixed Costs", -342000));
        dataSeries.add(new DataSeriesItem("Variable Costs", -233000));
        WaterFallSum balance = new WaterFallSum("Balance");
        dataSeries.add(balance);

        PlotOptionsWaterfall opts = new PlotOptionsWaterfall();
        DataLabels dataLabels = new DataLabels(true);
        dataLabels.setVerticalAlign(VerticalAlign.TOP);
        dataLabels.setY(-30);
        dataLabels.setFormatter("function() { return this.y / 1000 + 'k'; }");
        opts.setDataLabels(dataLabels);

        dataSeries.setPlotOptions(opts);

        Configuration configuration = chart.getConfiguration();
        configuration.addSeries(dataSeries);
        configuration.getxAxis().setType(AxisType.CATEGORY);

        add(chart);
    }
}

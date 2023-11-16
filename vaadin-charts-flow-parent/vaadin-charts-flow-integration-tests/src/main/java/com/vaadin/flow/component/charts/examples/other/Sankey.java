package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItemSankey;
import com.vaadin.flow.component.charts.model.PlotOptionsSankey;

public class Sankey extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.SANKEY);

        Configuration conf = chart.getConfiguration();
        conf.setTitle("Reporting");
        conf.setSubTitle("data");

        DataSeries series = createSeries();
        conf.addSeries(series);

        PlotOptionsSankey plotOptions = new PlotOptionsSankey();
        plotOptions.setCurveFactor(0.5);
        conf.setPlotOptions(plotOptions);

        add(chart);
    }

    private DataSeries createSeries() {
        DataSeries series = new DataSeries("Import/Export");
        series.add(createItem("Brazil", "Portugal", 5));
        series.add(createItem("Brazil", "France", 1));
        series.add(createItem("Brazil", "Spain", 1));
        series.add(createItem("Brazil", "England", 1));
        series.add(createItem("Canada", "Portugal", 1));
        series.add(createItem("Canada", "France", 5));
        series.add(createItem("Canada", "England", 1));
        series.add(createItem("Mexico", "Portugal", 1));
        series.add(createItem("Mexico", "France", 1));
        series.add(createItem("Mexico", "Spain", 5));
        series.add(createItem("Mexico", "England", 1));
        series.add(createItem("USA", "Portugal", 1));
        series.add(createItem("USA", "France", 1));
        series.add(createItem("USA", "Spain", 1));
        series.add(createItem("USA", "England", 5));
        series.add(createItem("Portugal", "Angola", 2));
        series.add(createItem("Portugal", "Senegal", 1));
        series.add(createItem("Portugal", "Morocco", 1));
        series.add(createItem("Portugal", "South Africa", 3));
        series.add(createItem("France", "Angola", 1));
        series.add(createItem("France", "Senegal", 3));
        series.add(createItem("France", "Mali", 3));
        series.add(createItem("France", "Morocco", 3));
        series.add(createItem("France", "South Africa", 1));
        series.add(createItem("Spain", "Senegal", 1));
        series.add(createItem("Spain", "Morocco", 3));
        series.add(createItem("Spain", "South Africa", 1));
        series.add(createItem("England", "Angola", 1));
        series.add(createItem("England", "Senegal", 1));
        series.add(createItem("England", "Morocco", 2));
        series.add(createItem("England", "South Africa", 7));
        series.add(createItem("South Africa", "China", 5));
        series.add(createItem("South Africa", "India", 1));
        series.add(createItem("South Africa", "Japan", 3));
        series.add(createItem("Angola", "China", 5));
        series.add(createItem("Angola", "India", 1));
        series.add(createItem("Angola", "Japan", 3));
        series.add(createItem("Senegal", "China", 5));
        series.add(createItem("Senegal", "India", 1));
        series.add(createItem("Senegal", "Japan", 3));
        series.add(createItem("Mali", "China", 5));
        series.add(createItem("Mali", "India", 1));
        series.add(createItem("Mali", "Japan", 3));
        series.add(createItem("Morocco", "China", 5));
        series.add(createItem("Morocco", "India", 1));
        series.add(createItem("Morocco", "Japan", 3));

        return series;
    }

    private DataSeriesItemSankey createItem(String from, String to,
            Number weight) {
        DataSeriesItemSankey seriesItem = new DataSeriesItemSankey(from, to,
                weight);
        DataLabels labels = new DataLabels();
        labels.setEnabled(true);
        seriesItem.setDataLabels(labels);
        return seriesItem;
    }
}

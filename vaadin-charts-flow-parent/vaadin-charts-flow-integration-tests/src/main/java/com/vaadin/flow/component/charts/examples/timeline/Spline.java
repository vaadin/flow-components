package com.vaadin.flow.component.charts.examples.timeline;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.timeline.util.StockPrices;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.RangeSelector;

public class Spline extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.SPLINE);
        chart.setTimeline(true);

        Configuration configuration = chart.getConfiguration();
        configuration.getTitle().setText("AAPL Stock Price");
        configuration.getTooltip().setEnabled(true);

        DataSeries dataSeries = new DataSeries();

        for (StockPrices.PriceData data : StockPrices.fetchAaplPrice()) {
            DataSeriesItem item = new DataSeriesItem();
            item.setX(data.getDate());
            item.setY(data.getPrice());
            dataSeries.add(item);
        }

        configuration.addSeries(dataSeries);

        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setSelected(1);
        configuration.setRangeSelector(rangeSelector);

        add(chart);
    }
}

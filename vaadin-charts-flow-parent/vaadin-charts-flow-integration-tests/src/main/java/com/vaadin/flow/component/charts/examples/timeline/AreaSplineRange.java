package com.vaadin.flow.component.charts.examples.timeline;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.timeline.util.StockPrices;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Tooltip;

public class AreaSplineRange extends AbstractChartExample {

    @Override
    public void initDemo() {
        final Chart chart = new Chart(ChartType.AREASPLINERANGE);

        Configuration configuration = chart.getConfiguration();
        configuration.getTitle().setText("Temperature variation by day");

        Tooltip tooltip = configuration.getTooltip();
        tooltip.setValueSuffix("°C");

        DataSeries dataSeries = new DataSeries("Temperatures");
        for (StockPrices.RangeData data : StockPrices.fetchDailyTempRanges()) {
            dataSeries.add(new DataSeriesItem(data.getDate(), data.getMin(),
                    data.getMax()));
        }
        configuration.setSeries(dataSeries);

        chart.setTimeline(true);
        add(chart);
    }
}
package com.vaadin.flow.component.charts.examples.timeline;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.timeline.util.StockPrices;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataGrouping;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.OhlcItem;
import com.vaadin.flow.component.charts.model.PlotOptionsCandlestick;
import com.vaadin.flow.component.charts.model.RangeSelector;
import com.vaadin.flow.component.charts.model.TimeUnit;
import com.vaadin.flow.component.charts.model.TimeUnitMultiples;

public class Candlestick extends AbstractChartExample {

    @Override
    public void initDemo() {
        final Chart chart = new Chart(ChartType.CANDLESTICK);

        Configuration configuration = chart.getConfiguration();
        configuration.getTitle().setText("AAPL Stock Price");

        DataSeries dataSeries = new DataSeries();
        PlotOptionsCandlestick plotOptionsCandlestick = new PlotOptionsCandlestick();
        DataGrouping grouping = new DataGrouping();
        grouping.addUnit(new TimeUnitMultiples(TimeUnit.WEEK, 1));
        grouping.addUnit(new TimeUnitMultiples(TimeUnit.MONTH, 1, 2, 3, 4, 6));
        plotOptionsCandlestick.setDataGrouping(grouping);
        dataSeries.setPlotOptions(plotOptionsCandlestick);
        for (StockPrices.OhlcData data : StockPrices.fetchAaplOhlcPrice()) {
            OhlcItem item = new OhlcItem();
            item.setX(data.getDate());
            item.setLow(data.getLow());
            item.setHigh(data.getHigh());
            item.setClose(data.getClose());
            item.setOpen(data.getOpen());
            dataSeries.add(item);
        }
        configuration.setSeries(dataSeries);

        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setSelected(4);
        configuration.setRangeSelector(rangeSelector);

        chart.setTimeline(true);
        add(chart);
    }
}
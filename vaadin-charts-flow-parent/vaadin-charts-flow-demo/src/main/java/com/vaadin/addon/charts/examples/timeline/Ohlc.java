package com.vaadin.addon.charts.examples.timeline;

import com.vaadin.addon.charts.AbstractChartExample;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.examples.timeline.util.StockPrices;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataGrouping;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.OhlcItem;
import com.vaadin.addon.charts.model.PlotOptionsOhlc;
import com.vaadin.addon.charts.model.RangeSelector;
import com.vaadin.addon.charts.model.TimeUnit;
import com.vaadin.addon.charts.model.TimeUnitMultiples;

public class Ohlc extends AbstractChartExample {

    @Override
    public void initDemo() {
        final Chart chart = new Chart(ChartType.OHLC);

        Configuration configuration = chart.getConfiguration();
        configuration.getTitle().setText("AAPL Stock Price");

        DataSeries dataSeries = new DataSeries();
        PlotOptionsOhlc plotOptionsOhlc = new PlotOptionsOhlc();
        DataGrouping grouping = new DataGrouping();
        grouping.addUnit(new TimeUnitMultiples(TimeUnit.WEEK, 1));
        grouping.addUnit(new TimeUnitMultiples(TimeUnit.MONTH, 1, 2, 3, 4, 6));
        plotOptionsOhlc.setDataGrouping(grouping);
        dataSeries.setPlotOptions(plotOptionsOhlc);
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
        rangeSelector.setSelected(1);
        configuration.setRangeSelector(rangeSelector);

        chart.setTimeline(true);
        add(chart);
    }
}
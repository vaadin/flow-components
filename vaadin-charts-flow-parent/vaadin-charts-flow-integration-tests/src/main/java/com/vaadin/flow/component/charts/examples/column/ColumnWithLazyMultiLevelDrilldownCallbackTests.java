package com.vaadin.flow.component.charts.examples.column;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Cursor;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DrilldownCallback;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.function.SerializableRunnable;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("serial")
@SkipFromDemo
public class ColumnWithLazyMultiLevelDrilldownCallbackTests
        extends AbstractChartExample {

    private OrderedList log;
    private int count;

    @Override
    public void initDemo() {
        log = new OrderedList();
        log.setId("log");
        Div layout = new Div();
        final Chart chart = new Chart(ChartType.COLUMN);
        chart.setId("chart");

        final Configuration conf = chart.getConfiguration();
        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setCursor(Cursor.POINTER);
        column.setAnimation(false);
        conf.getDrilldown().setAnimation(false);
        column.setDataLabels(new DataLabels(true));
        conf.setPlotOptions(column);
        DataSeries series = new DataSeries();
        series.setName("Top");

        createItems("Item").forEach(series::addItemWithDrilldown);

        conf.addSeries(series);

        chart.addChartDrillupListener(event -> log("ChartDrillupEvent"));
        final NativeButton setNew = new NativeButton("set new callback",
                e -> chart.setDrilldownCallback(getDrilldownCallback()));
        setNew.setId("setNew");
        final NativeButton setSame = new NativeButton("set same callback",
                e -> chart.setDrilldownCallback(chart.getDrilldownCallback()));
        setSame.setId("setSame");
        final NativeButton setNull = new NativeButton("set null callback",
                e -> chart.setDrilldownCallback(null));
        setNull.setId("setNull");
        layout.add(chart, setNew, setSame, setNull, log);
        add(layout);
    }

    private DrilldownCallback getDrilldownCallback() {
        return (DrilldownCallback) event -> {
            log("DrilldownEvent: " + event.getItem().getId());
            return getPointDrilldown(event.getItem());
        };
    }

    private DataSeriesItem createDataSeriesItem(String nameId, Number value) {
        final DataSeriesItem result = new DataSeriesItem(nameId, value);
        result.setId(nameId);
        return result;
    }

    private Component createButton(String id, String text,
            SerializableRunnable runnable) {
        final NativeButton button = new NativeButton(text, e -> runnable.run());
        button.setId(id);
        return button;
    }

    private void log(String newStringValue) {
        log.add(new ListItem(newStringValue));
    }

    private Series getPointDrilldown(DataSeriesItem point) {
        String prefix = point.getId() + "_";
        DataSeries series = new DataSeries(prefix + "drill");
        createItems(prefix).forEach(series::addItemWithDrilldown);
        return series;
    }

    private Stream<DataSeriesItem> createItems(String prefix) {
        return IntStream.range(1, 4)
                .mapToObj(j -> createDataSeriesItem(prefix + j, j));
    }
}
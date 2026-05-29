/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * Exercises the reactive series/point sync (experimental {@code reactiveCharts}
 * feature flag). Every button mutates the {@link Configuration} and
 * deliberately does NOT call {@link Chart#drawChart()}; with the flag on, the
 * changes propagate to the client via the id-keyed
 * {@code $connector.syncSeries}.
 */
@Route("vaadin-charts/dynamic/reactive-series")
public class ReactiveSeriesUpdate extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.LINE);
        chart.setId("reactiveChart");

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Reactive series");

        DataSeries first = new DataSeries("Series 1");
        first.add(new DataSeriesItem("A", 1));
        first.add(new DataSeriesItem("B", 2));
        first.add(new DataSeriesItem("C", 3));
        configuration.addSeries(first);

        AtomicInteger seriesCounter = new AtomicInteger(1);
        AtomicInteger pointCounter = new AtomicInteger(3);
        AtomicBoolean firstVisible = new AtomicBoolean(true);

        NativeButton addSeries = new NativeButton("Add series", e -> {
            int n = seriesCounter.incrementAndGet();
            DataSeries series = new DataSeries("Series " + n);
            series.add(new DataSeriesItem("A", n));
            series.add(new DataSeriesItem("B", n + 1));
            configuration.addSeries(series);
        });
        addSeries.setId("addSeriesButton");

        NativeButton removeSeries = new NativeButton("Remove last series",
                e -> {
                    List<Series> all = new ArrayList<>(
                            configuration.getSeries());
                    if (all.size() > 1) {
                        all.remove(all.size() - 1);
                        configuration.setSeries(all);
                    }
                });
        removeSeries.setId("removeSeriesButton");

        NativeButton addPoint = new NativeButton("Add point to first series",
                e -> first.add(new DataSeriesItem("P",
                        pointCounter.incrementAndGet())));
        addPoint.setId("addPointButton");

        NativeButton removePoint = new NativeButton(
                "Remove first point of first series", e -> {
                    if (!first.getData().isEmpty()) {
                        first.remove(first.get(0));
                    }
                });
        removePoint.setId("removePointButton");

        NativeButton updatePoint = new NativeButton(
                "Update first point of first series", e -> {
                    if (!first.getData().isEmpty()) {
                        DataSeriesItem item = first.get(0);
                        item.setY(pointCounter.incrementAndGet());
                        first.update(item);
                    }
                });
        updatePoint.setId("updatePointButton");

        NativeButton toggleVisibility = new NativeButton(
                "Toggle first series visibility", e -> {
                    boolean visible = !firstVisible.get();
                    firstVisible.set(visible);
                    first.setVisible(visible);
                });
        toggleVisibility.setId("toggleVisibilityButton");

        NativeButton renameSeries = new NativeButton("Rename first series",
                e -> {
                    first.setName("Renamed " + seriesCounter.get());
                    first.updateSeries();
                });
        renameSeries.setId("renameSeriesButton");

        add(chart, addSeries, removeSeries, addPoint, removePoint, updatePoint,
                toggleVisibility, renameSeries);
    }
}

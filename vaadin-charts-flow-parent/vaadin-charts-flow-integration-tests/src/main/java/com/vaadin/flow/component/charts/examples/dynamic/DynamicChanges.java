package com.vaadin.flow.component.charts.examples.dynamic;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.html.Input;

@SkipFromDemo
public class DynamicChanges extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.PIE);

        Configuration conf = chart.getConfiguration();
        conf.setTitle("Browser market shares at a specific website, 2010");

        DataSeries series = new DataSeries(getInitialData());
        conf.setSeries(series);

        Input addPointButton = new Input();
        addPointButton.setValue("Add Point");
        addPointButton.setType("button");
        addPointButton.setId("addPointButton");
        ComponentUtil.addListener(addPointButton, ClickEvent.class,
                (ComponentEventListener) e -> {
                    String randomName = "Random browser "
                            + Math.floor(Math.random() * 20); // NOSONAR
                    double randomValue = Math.random() * 20; // NOSONAR
                    series.add(new DataSeriesItem(randomName, randomValue));
                });

        Input removePointButton = new Input();
        removePointButton.setValue("Remove Point");
        removePointButton.setId("removePointButton");
        removePointButton.setType("button");
        ComponentUtil.addListener(removePointButton, ClickEvent.class,
                (ComponentEventListener) e -> {
                    if (!series.getData().isEmpty()) {
                        series.remove(series.getData().get(0));
                    }
                });

        Input updatePointButton = new Input();
        updatePointButton.setValue("Update Point");
        updatePointButton.setId("updatePointButton");
        updatePointButton.setType("button");
        ComponentUtil.addListener(updatePointButton, ClickEvent.class,
                (ComponentEventListener) e -> {
                    if (!series.getData().isEmpty()) {
                        DataSeriesItem item = series.getData().get(0);
                        item.setY(Math.random() * 20); // NOSONAR
                        series.update(item);
                    }
                });

        Input slicePointButton = new Input();
        slicePointButton.setValue("Slice Point");
        slicePointButton.setId("slicePointButton");
        slicePointButton.setType("button");
        ComponentUtil.addListener(slicePointButton, ClickEvent.class,
                (ComponentEventListener) e -> {
                    if (!series.getData().isEmpty()) {
                        DataSeriesItem item = series.getData().get(0);
                        item.setX(Math.random() * 20); // NOSONAR
                        series.setItemSliced(0, !item.getSliced());
                    }
                });

        Input disableSeriesButton = new Input();
        disableSeriesButton.setValue("Toggle Series Visibility");
        disableSeriesButton.setId("disableSeriesButton");
        disableSeriesButton.setType("button");
        ComponentUtil.addListener(disableSeriesButton, ClickEvent.class,
                (ComponentEventListener) e -> {
                    series.setVisible(!series.isVisible());
                });

        Input resetSeriesButton = new Input();
        resetSeriesButton.setValue("Reset Series");
        resetSeriesButton.setId("resetSeriesButton");
        resetSeriesButton.setType("button");
        ComponentUtil.addListener(resetSeriesButton, ClickEvent.class,
                (ComponentEventListener) e -> {
                    series.setData(getInitialData());
                    series.updateSeries();
                });
        add(chart, addPointButton, removePointButton, updatePointButton,
                slicePointButton, disableSeriesButton, resetSeriesButton);
    }

    private List<DataSeriesItem> getInitialData() {
        List<DataSeriesItem> data = new ArrayList<>();
        data.add(new DataSeriesItem("Firefox", 45.0));
        data.add(new DataSeriesItem("IE", 26.8));
        DataSeriesItem chrome = new DataSeriesItem("Chrome", 12.8);
        chrome.setSliced(true);
        chrome.setSelected(true);
        data.add(chrome);
        data.add(new DataSeriesItem("Safari", 8.5));
        data.add(new DataSeriesItem("Opera", 6.2));
        data.add(new DataSeriesItem("Others", 0.7));
        return data;
    }
}

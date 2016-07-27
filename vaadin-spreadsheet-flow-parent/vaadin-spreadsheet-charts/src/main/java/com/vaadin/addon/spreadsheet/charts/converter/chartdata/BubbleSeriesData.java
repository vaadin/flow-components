package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import java.util.Collections;
import java.util.List;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.BubbleSeriesDataWriter;

public class BubbleSeriesData extends AbstractSeriesData {

    public List<Number> bubbleSizes;

    public BubbleSeriesData() {
        super();
        bubbleSizes = Collections.emptyList();
    }

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new BubbleSeriesDataWriter(this);
    }

}

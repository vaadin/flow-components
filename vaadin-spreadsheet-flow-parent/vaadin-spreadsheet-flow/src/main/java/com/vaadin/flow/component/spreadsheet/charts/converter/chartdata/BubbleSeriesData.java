package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.BubbleSeriesDataWriter;

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

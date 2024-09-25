/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

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

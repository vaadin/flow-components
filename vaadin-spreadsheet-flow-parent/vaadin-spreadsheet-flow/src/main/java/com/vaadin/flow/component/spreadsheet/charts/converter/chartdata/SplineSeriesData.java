/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.SplineSeriesDataWriter;

public class SplineSeriesData extends LineSeriesData {

    @Override
    public SplineSeriesDataWriter getSeriesDataWriter() {
        return new SplineSeriesDataWriter(this);
    }
}

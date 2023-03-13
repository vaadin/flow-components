/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.ScatterSeriesDataWriter;

public class ScatterSeriesData extends LineSeriesData {

    @Override
    public ScatterSeriesDataWriter getSeriesDataWriter() {
        return new ScatterSeriesDataWriter(this);
    }
}

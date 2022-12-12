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

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.ScatterSeriesDataWriter;

public class ScatterSeriesData extends LineSeriesData {

    @Override
    public ScatterSeriesDataWriter getSeriesDataWriter() {
        return new ScatterSeriesDataWriter(this);
    }
}

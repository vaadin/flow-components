/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.PieSeriesDataWriter;

public class PieSeriesData extends AbstractSeriesData {

    public boolean isExploded;
    public boolean isDonut;
    public short donutHoleSizePercent;

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new PieSeriesDataWriter(this);
    }
}

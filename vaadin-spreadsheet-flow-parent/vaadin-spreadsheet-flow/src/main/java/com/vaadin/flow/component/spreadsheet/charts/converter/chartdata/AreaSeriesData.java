/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.AreaSeriesDataWriter;

/**
 * @since 24.0
 */
public class AreaSeriesData extends LineSeriesData {

    public Stacking stacking = Stacking.NONE;

    public boolean filled = true;

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new AreaSeriesDataWriter(this);
    }
}

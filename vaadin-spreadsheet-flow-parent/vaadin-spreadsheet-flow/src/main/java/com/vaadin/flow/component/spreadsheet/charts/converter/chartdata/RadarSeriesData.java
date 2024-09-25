/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.AreaSeriesDataWriter;
import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.RadarSeriesWriter;

public class RadarSeriesData extends AreaSeriesData {

    @Override
    public AreaSeriesDataWriter getSeriesDataWriter() {
        return new RadarSeriesWriter(this);
    }

}

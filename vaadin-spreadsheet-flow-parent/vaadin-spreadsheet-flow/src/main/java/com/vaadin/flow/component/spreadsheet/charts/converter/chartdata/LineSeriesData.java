package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

/*
 * #%L
 * Vaadin Spreadsheet Charts Integration
 * %%
 * Copyright (C) 2016 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.LineSeriesDataWriter;

public class LineSeriesData extends AbstractSeriesData {

    /*
     * This file should not have dependencies on
     * "com.vaadin.flow.component.charts" and I found it too clumsy creating
     * (i.e. copy-pasting) the same enums here.
     */

    /**
     * Currently this string value corresponds to
     * com.vaadin.flow.component.charts.model.DashStyle.
     */
    public String dashStyle = "";

    public Integer lineWidth;

    /**
     * Currently this string value corresponds to
     * com.vaadin.flow.component.charts.model.MarkerSymbolEnum.
     */
    public String markerSymbol = "";

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new LineSeriesDataWriter(this);
    }
}

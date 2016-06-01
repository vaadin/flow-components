package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

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

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.PieSeriesDataWriter;

public class PieSeriesData extends AbstractSeriesData {

    public boolean isExploded;
    public boolean isDonut;
    public short donutHoleSizePercent;

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new PieSeriesDataWriter(this);
    }
}

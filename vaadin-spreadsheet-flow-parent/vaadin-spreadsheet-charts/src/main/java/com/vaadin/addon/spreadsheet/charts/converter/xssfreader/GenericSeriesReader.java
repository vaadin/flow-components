package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

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

import org.apache.xmlbeans.XmlObject;

import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;

/**
 * Can be used to test reading of a new chart type or as a temporary means to
 * see the data.
 */
@SuppressWarnings("rawtypes")
public class GenericSeriesReader extends AbstractSeriesReader {

    public GenericSeriesReader(XmlObject ctChart, Spreadsheet spreadsheet,
            boolean showDataInHiddenCells) {
        super(ctChart, spreadsheet, showDataInHiddenCells);
    }

    @Override
    protected AbstractSeriesData createSeriesDataObject(XmlObject serie) {
        return new AbstractSeriesData() {
            @Override
            public AbstractSeriesDataWriter getSeriesDataWriter() {
                return new AbstractSeriesDataWriter(this) {
                    @Override
                    protected AbstractPlotOptions createPlotOptions() {
                        return new PlotOptionsLine();
                    }
                };
            }
        };
    }
}

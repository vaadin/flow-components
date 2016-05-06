package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

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

    public GenericSeriesReader(XmlObject ctChart, Spreadsheet spreadsheet) {
        super(ctChart, spreadsheet);
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

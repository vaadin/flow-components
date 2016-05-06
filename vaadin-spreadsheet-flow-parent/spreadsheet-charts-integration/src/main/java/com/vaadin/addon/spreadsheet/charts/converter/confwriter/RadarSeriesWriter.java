package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.Pane;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.RadarSeriesData;

public class RadarSeriesWriter extends LineSeriesDataWriter {

    public RadarSeriesWriter(RadarSeriesData series) {
        super(series);
    }

    @Override
    protected void configureChart(Configuration conf) {
        super.configureChart(conf);

        conf.getChart().setPolar(true);
        Pane pane = new Pane();
        pane.setSize("60%");
        conf.addPane(pane);

        YAxis yaxs = new YAxis();
        conf.addyAxis(yaxs);
        yaxs.setGridLineInterpolation("polygon");
        
        Style style = new Style();
        style.setFontSize("75%");
        conf.getxAxis().getLabels().setStyle(style);
    }

    @Override
    protected void configureDataSeries(DataSeries dataSeriesForWriting) {
        super.configureDataSeries(dataSeriesForWriting);

        getPlotOptions().setDashStyle(
                LineSeriesWriterUtils.getDashStyle(getSeriesData().dashStyle));
        getPlotOptions().setMarker(
                LineSeriesWriterUtils.getMarker(getSeriesData().markerSymbol));
    }
}

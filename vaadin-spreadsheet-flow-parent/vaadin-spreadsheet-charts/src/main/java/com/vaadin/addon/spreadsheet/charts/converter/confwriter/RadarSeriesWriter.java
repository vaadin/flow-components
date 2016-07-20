package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

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

import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.Pane;
import com.vaadin.addon.charts.model.Stacking;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.RadarSeriesData;

public class RadarSeriesWriter extends AreaSeriesDataWriter {

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
        if(!getSeriesData().filled){
            getPlotOptions().setFillColor(new SolidColor(0,0,0,0));
            /*The code line:
            *   getPlotOptions().setFillColor(new SolidColor(0,0,0,0));
            * is because of bug HighChart bug #4888 and it is fixed in 4.2.2 version and can be replaced by the following line of code when migrate to new HighChart version:
            *   getPlotOptions().setFillOpacity(0);*/
        }

        getPlotOptions().setDashStyle(
                LineSeriesWriterUtils.getDashStyle(getSeriesData().dashStyle));
        getPlotOptions().setMarker(
                LineSeriesWriterUtils.getMarker(getSeriesData().markerSymbol));
    }
}

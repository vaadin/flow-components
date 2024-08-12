/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.xssfreader;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellReference;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterSer;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.charts.converter.Utils;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.AbstractSeriesData.DataSelectListener;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.AbstractSeriesData.SeriesPoint;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.ScatterSeriesData;

public class ScatterSeriesReader
        extends AbstractSeriesReader<CTScatterSer, ScatterSeriesData> {

    public ScatterSeriesReader(CTScatterChart ctChart, Spreadsheet spreadsheet,
            boolean showDataInHiddenCells) {
        super(ctChart, spreadsheet, showDataInHiddenCells);
    }

    @Override
    protected ScatterSeriesData createSeriesDataObject(CTScatterSer serie) {
        return new ScatterSeriesData();
    }

    @Override
    protected void fillSeriesData(ScatterSeriesData seriesData,
            CTScatterSer serie) {
        seriesData.name = tryGetSeriesName(serie.getTx());

        if (serie.getXVal() == null) {
            createSeriesDataPoints(serie.getYVal(), seriesData);
        } else {
            createSeriesDataPointsForScatter(serie.getXVal(), serie.getYVal(),
                    seriesData);
        }

        if (serie.getMarker() != null) {
            LineSeriesReaderUtils.setMarkerForData(seriesData,
                    serie.getMarker());
        }

        if (serie.getSpPr() != null) {
            LineSeriesReaderUtils.setDashStyleForData(seriesData,
                    serie.getSpPr());
            LineSeriesReaderUtils.setLineWidthForData(seriesData,
                    serie.getSpPr());
        }
    }

    /**
     * Scatter requires x and y values, for other charts we use only X
     */
    protected void createSeriesDataPointsForScatter(CTAxDataSource xVal,
            CTNumDataSource yVal, ScatterSeriesData seriesData) {
        final List<CellReference> ptListX = Utils.getAllReferencedCells(
                xVal.getNumRef().getF(), getSpreadsheet(),
                showDataInHiddenCells);

        final String formulaY = yVal.getNumRef().getF();
        final List<CellReference> ptListY = Utils.getAllReferencedCells(
                formulaY, getSpreadsheet(), showDataInHiddenCells);

        final List<SeriesPoint> list = new ArrayList<SeriesPoint>();

        for (int i = 0; i < ptListY.size(); i++) {
            list.add(new SeriesPoint(getNumericValueFromCellRef(ptListX.get(i)),
                    getNumericValueFromCellRef(ptListY.get(i))));
        }

        seriesData.seriesData = list;

        // TODO: fix interaction, handle updates has to be made compatible with
        // this type, requires figuring out how to set selection to two ranges.

        handleReferencedValueUpdates(ptListY, seriesData,
                ValueUpdateMode.Y_VALUES);
        handleReferencedValueUpdates(ptListX, seriesData,
                ValueUpdateMode.X_VALUES);

        seriesData.dataSelectListener = new DataSelectListener() {
            @Override
            public void dataSelected() {
                getSpreadsheet().setSelection(formulaY);
            }
        };
    }
}

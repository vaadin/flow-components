/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.confwriter;

import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DataSeriesItem3d;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.AbstractSeriesData;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.AbstractSeriesData.DataUpdateListener;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.AbstractSeriesData.SeriesPoint;

public abstract class AbstractSeriesDataWriter {
    private final AbstractSeriesData series;
    private AbstractPlotOptions plotOptions;

    public AbstractSeriesDataWriter(AbstractSeriesData series) {
        this.series = series;
    }

    public Series convertSeries(final boolean blanksAsZeros) {
        /*
         * We use SelectListeningDataSeries to notify the spreadsheet component
         * when series is selected. ChartConverter adds a the listener to the
         * chart object, AbstractSeriesDataReader passes it to the spreadsheet.
         * another option would be to pass the chart object from ChartConverter
         * all the way down here and eliminate the need of ChartConverter
         * setting the listener, but it would require modifying a lot of
         * intermediate classes, TODO think about a better way of doing this
         */
        final DataSeries dataSeries = new SelectListeningDataSeries(
                getSeriesData().name,
                () -> getSeriesData().dataSelectListener.dataSelected());

        configureDataSeries(dataSeries);

        dataSeries.setyAxis(series.yAxis);

        for (SeriesPoint point : series.seriesData) {
            dataSeries.add(createDataSeriesItem(point, blanksAsZeros));
        }

        series.dataUpdateListener = new DataUpdateListener() {

            @Override
            public void xDataModified(int i, Double cellValue) {
                DataSeriesItem item = dataSeries.get(i);

                if (blanksAsZeros && cellValue == null) {
                    item.setX(0d);
                } else {
                    item.setX(cellValue);
                }

                dataSeries.update(item);
            }

            @Override
            public void yDataModified(int i, Double cellValue) {
                DataSeriesItem item = dataSeries.get(i);

                if (blanksAsZeros && cellValue == null) {
                    item.setY(0d);
                } else {
                    item.setY(cellValue);
                }

                dataSeries.update(item);
            }

            @Override
            public void zDataModified(int i, Double cellValue) {
                DataSeriesItem3d item = (DataSeriesItem3d) dataSeries.get(i);

                if (blanksAsZeros && cellValue == null) {
                    item.setZ(0d);
                } else {
                    item.setZ(cellValue);
                }

                dataSeries.update(item);
            }

            @Override
            public void categoryModified(int i, String cellValue) {
                DataSeriesItem item = dataSeries.get(i);

                item.setName(cellValue);

                dataSeries.update(item);
            }

        };

        return dataSeries;
    }

    protected AbstractSeriesData getSeriesData() {
        return series;
    }

    protected AbstractPlotOptions getPlotOptions() {
        if (plotOptions == null) {
            plotOptions = createPlotOptions();
        }

        return plotOptions;
    }

    protected DataSeriesItem createDataSeriesItem(SeriesPoint point,
            boolean blanksAsZeros) {
        DataSeriesItem result = null;
        if (point.yValue == null && blanksAsZeros) {
            result = new DataSeriesItem(point.xValue, 0d);
        } else {
            result = new DataSeriesItem(point.xValue, point.yValue);
        }

        if (getSeriesData().categories.size() > point.xValue.intValue()) {
            result.setName(
                    getSeriesData().categories.get(point.xValue.intValue()));
        }
        return result;
    }

    protected void configureDataSeries(DataSeries dataSeriesForWriting) {
        dataSeriesForWriting.setPlotOptions(getPlotOptions());
    }

    /**
     * This should only instantiate the object, configuration is done in
     * configureDataSeries.
     */
    protected abstract AbstractPlotOptions createPlotOptions();

    protected void configureChart(Configuration conf) {
        // default NOP
    }
}

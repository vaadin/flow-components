/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;

public abstract class AbstractSeriesData {
    public static class SeriesPoint {

        public SeriesPoint(Number x, Number y) {
            xValue = x;
            yValue = y;
        }

        public SeriesPoint(Number x, Number y, Number z) {
            this(x, y);
            zValue = z;
        }

        public Number xValue = 0;
        public Number yValue = 0;
        public Number zValue = 0;
    }

    public String name = "";
    public List<SeriesPoint> seriesData = Collections.emptyList();
    public List<String> categories = Collections.emptyList();
    public int tooltipDecimals = -1;

    public boolean is3d = false;

    public abstract AbstractSeriesDataWriter getSeriesDataWriter();

    public interface DataUpdateListener {
        void xDataModified(int i, Double cellValue);

        void yDataModified(int i, Double cellValue);

        void zDataModified(int i, Double cellValue);

        void categoryModified(int i, String cellValue);
    }

    public DataUpdateListener dataUpdateListener;

    public interface DataSelectListener {
        void dataSelected();
    }

    public DataSelectListener dataSelectListener;

    /**
     * Refers to ChartData.yAxesProperties index
     */
    public int yAxis;
}

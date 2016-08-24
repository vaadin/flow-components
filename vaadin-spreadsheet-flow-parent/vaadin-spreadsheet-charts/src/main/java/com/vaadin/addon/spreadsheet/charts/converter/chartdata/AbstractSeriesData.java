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

import java.util.Collections;
import java.util.List;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;

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
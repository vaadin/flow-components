package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import java.util.Collections;
import java.util.List;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;

public abstract class AbstractSeriesData {
    public static class SeriesPoint {
        public SeriesPoint(Number x, Number y) {
            xValue = x;
            yValue = y;
        }

        public Number xValue = 0;
        public Number yValue = 0;
    }
    
    public String name = "";
    public List<SeriesPoint> seriesData = Collections.emptyList();
    public List<String> categories = Collections.emptyList();

    public boolean is3d = false;

    public abstract AbstractSeriesDataWriter getSeriesDataWriter();

    public interface DataUpdateListener {
        void dataModified(int i, Double cellValue);
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
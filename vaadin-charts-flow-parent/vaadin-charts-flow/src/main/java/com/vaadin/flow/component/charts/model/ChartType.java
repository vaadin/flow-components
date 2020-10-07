package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */


import java.util.Objects;

/**
 * The default series type for the chart. Can be one of LINE, SPLINE, AREA,
 * AREASPLINE, COLUMN, BAR, PIE, SCATTER, AREARANGE, AREASPLINERANGE, and
 * COLUMNRANGE. Defaults to LINE.
 * <p>
 * Own custom extensions can be made by extending this class.
 * <p>
 * Configuration specific to each ChartType can be set using
 * {@link Configuration#setPlotOptions(AbstractPlotOptions...)} or for a
 * single series using
 * {@link AbstractSeries#setPlotOptions(AbstractPlotOptions)}
 */
public class ChartType implements ChartEnum {

    /**
     * @see PlotOptionsArea
     */
    public static final ChartType AREA = new ChartType("area");
    /**
     * @see PlotOptionsLine
     */
    public static final ChartType LINE = new ChartType("line");
    /**
     * @see PlotOptionsSpline
     */
    public static final ChartType SPLINE = new ChartType("spline");
    /**
     * @see PlotOptionsAreaspline
     */
    public static final ChartType AREASPLINE = new ChartType("areaspline");
    /**
     * @see PlotOptionsColumn
     */
    public static final ChartType COLUMN = new ChartType("column");
    /**
     * @see PlotOptionsBar
     */
    public static final ChartType BAR = new ChartType("bar");
    /**
     * @see PlotOptionsPie
     */
    public static final ChartType PIE = new ChartType("pie");
    /**
     * @see PlotOptionsScatter
     */
    public static final ChartType SCATTER = new ChartType("scatter");
    /**
     * @see PlotOptionsGauge
     */
    public static final ChartType GAUGE = new ChartType("gauge");
    /**
     * @see PlotOptionsArearange
     */
    public static final ChartType AREARANGE = new ChartType("arearange");
    /**
     * @see PlotOptionsColumnrange
     */
    public static final ChartType COLUMNRANGE = new ChartType("columnrange");
    /**
     * @see PlotOptionsAreasplinerange
     */
    public static final ChartType AREASPLINERANGE = new ChartType(
            "areasplinerange");
    /**
     * @see PlotOptionsBoxplot
     */
    public static final ChartType BOXPLOT = new ChartType("boxplot");
    /**
     * @see PlotOptionsErrorbar
     */
    public static final ChartType ERRORBAR = new ChartType("errorbar");
    /**
     * @see PlotOptionsBubble
     */
    public static final ChartType BUBBLE = new ChartType("bubble");
    /**
     * @see PlotOptionsFunnel
     */
    public static final ChartType FUNNEL = new ChartType("funnel");
    /**
     * @see PlotOptionsWaterfall
     */
    public static final ChartType WATERFALL = new ChartType("waterfall");
    /**
     * @see PlotOptionsPyramid
     */
    public static final ChartType PYRAMID = new ChartType("pyramid");
    /**
     * @see PlotOptionsSolidgauge
     */
    public static final ChartType SOLIDGAUGE = new ChartType("solidgauge");
    /**
     * @see PlotOptionsHeatmap
     */
    public static final ChartType HEATMAP = new ChartType("heatmap");
    /**
     * @see PlotOptionsTreemap
     */
    public static final ChartType TREEMAP = new ChartType("treemap");
    /**
     * @see PlotOptionsPolygon
     */
    public static final ChartType POLYGON = new ChartType("polygon");

    /**
     * @see PlotOptionsCandlestick
     */
    public static final ChartType CANDLESTICK = new ChartType("candlestick");

    /**
     * @see PlotOptionsFlags
     */
    public static final ChartType FLAGS = new ChartType("flags");

    /**
     * @see PlotOptionsOhlc
     */
    public static final ChartType OHLC = new ChartType("ohlc");

    public static ChartType getDefault() {
        return ChartType.LINE;
    }

    private final String type;

    /**
     * Constructs a new Chart type.
     * 
     * @param type
     *            the actual type string passed for client side
     */
    protected ChartType(String type) {
        Objects.requireNonNull(type, "Chart type required");
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChartType)) return false;

        ChartType chartType = (ChartType) o;

        return type.equals(chartType.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}

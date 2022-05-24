package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

/**
 * A DataSeriesItem implementation suitable for
 * <a href="http://en.wikipedia.org/wiki/Box_plot">box plot charts</a>. Plot box
 * charts visualize well some statistical data.
 *
 * @see PlotOptionsBoxPlot
 *
 */
public class BoxPlotItem extends DataSeriesItem {
    // high/low already defined in DataSeriesItem

    private Number q1;
    private Number q3;
    private Number median;

    /**
     * Constructs an empty data item suitable for box plot chart
     */
    public BoxPlotItem() {
        makeCustomized();
    }

    /**
     * Constructs an item for box plot with given values.
     *
     * @param low
     * @param q1
     *            lower quartile
     * @param median
     * @param q3
     *            upper quartile
     * @param high
     */
    public BoxPlotItem(Number low, Number q1, Number median, Number q3,
            Number high) {
        this();
        setLow(low);
        setHigh(high);
        setMedian(median);
        setLowerQuartile(q1);
        setUpperQuartile(q3);
    }

    /**
     * @see #setLowerQuartile(Number)
     * @return
     */
    public Number getLowerQuartile() {
        return q1;
    }

    /**
     * Sets the lower quartile of the item. Often also referred as q1 value.
     *
     * @param lowerQuartile
     *            the lower quartile
     */
    public void setLowerQuartile(Number lowerQuartile) {
        q1 = lowerQuartile;
    }

    /**
     * @see #setUpperQuartile(Number)
     */
    public Number getUpperQuartile() {
        return q3;
    }

    /**
     * Sets the upper quartile of the item. Often also referred as q3 value.
     *
     * @param upperQuartile
     *            the upper quartile
     */
    public void setUpperQuartile(Number upperQuartile) {
        q3 = upperQuartile;
    }

    /**
     * @see #setMedian(Number)
     */
    public Number getMedian() {
        return median;
    }

    /**
     * Sets the median of the item. Often referred as q2 value.
     *
     * @param median
     */
    public void setMedian(Number median) {
        this.median = median;
    }

}

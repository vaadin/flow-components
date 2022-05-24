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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A series consisting of a list of numerical values. In this case, the
 * numerical values will be interpreted as Y values, and X values will be
 * automatically calculated, either starting at 0 and incrementing by 1, or from
 * pointStart and pointInterval given in the plotOptions. If the axis has
 * categories, these will be used. This option is not available for range
 * series.
 */
public class ListSeries extends AbstractSeries {

    private List<Number> data = new ArrayList<>();

    public ListSeries() {
    }

    /**
     * Constructs a ListSeries with the given series name.
     *
     * @param name
     */
    public ListSeries(String name) {
        super(name);
    }

    /**
     * Constructs a ListSeries with the given array of values.
     *
     * @param values
     */
    public ListSeries(Number... values) {
        Collections.addAll(data, values);
    }

    /**
     * Constructs a ListSeries with the given collection of values.
     *
     * @param values
     *            the values to use
     */
    public ListSeries(Collection<Number> values) {
        data.addAll(values);
    }

    /**
     * Constructs a ListSeries with the given series name and array of values.
     *
     * @param name
     * @param values
     */
    public ListSeries(String name, Number... values) {
        this(name);
        setData(values);
    }

    /**
     * Constructs a ListSeries with the given series name and collection of
     * values.
     *
     * @param name
     *            the name of the series
     * @param values
     *            the values to use
     */
    public ListSeries(String name, Collection<Number> values) {
        this(name);
        data.addAll(values);
    }

    /**
     * @return An array of the numeric values
     */
    public Number[] getData() {
        return data.toArray(new Number[data.size()]);
    }

    /**
     * Sets the values in the list series to the ones provided.
     *
     * @param values
     */
    public void setData(Number... values) {
        data.clear();
        Collections.addAll(data, values);
    }

    /**
     * Sets the given list of numeric values as the values in this list series.
     *
     * @param data
     */
    public void setData(List<Number> data) {
        this.data = data;
    }

    /**
     * Adds a given number to the series and immediately updates the chart if it
     * already has been drawn. If the chart has not yet been drawn all items are
     * added to the chart when it is drawn the first time.
     *
     * @param number
     *            the number to be added to the series
     */
    public void addData(Number number) {
        addData(number, true, false);
    }

    /**
     * Adds a given number to the series and optionally immediately updates the
     * chart if it has been drawn.
     *
     * This method is useful if you want to add many items without a
     * client/server round-trip for each item added. Do this by specifying false
     * for the updateChartImmediately parameter.
     *
     * @param number
     *            the number to be added to the series
     * @param updateChartImmediately
     *            if true the chart will be dynamically updated, using animation
     *            if enabled.
     * @param shift
     *            If true, the first item from the series is removed. Handy if
     *            dynamically adjusting adding points and fixed amount of points
     *            should be kept visible.
     */
    public void addData(Number number, boolean updateChartImmediately,
            boolean shift) {
        if (shift) {
            data.remove(0);
        }
        data.add(number);
        if (updateChartImmediately && getConfiguration() != null) {
            getConfiguration().fireDataAdded(this,
                    new DataSeriesItem(data.size() - 1, number), shift);
        }
    }

    /**
     * Updates the value of the data point at pointIndex to newValue and
     * immediately updates it on the chart using animation if enabled.
     *
     * @param pointIndex
     *            the index of the point to update
     * @param newValue
     *            the new value of the point
     */
    public void updatePoint(int pointIndex, Number newValue) {
        data.remove(pointIndex);
        data.add(pointIndex, newValue);
        if (getConfiguration() != null) {
            getConfiguration().fireDataUpdated(this, newValue, pointIndex);
        }
    }
}

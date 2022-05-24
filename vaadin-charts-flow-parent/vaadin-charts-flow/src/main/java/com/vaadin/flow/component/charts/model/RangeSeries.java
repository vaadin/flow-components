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
 * Series for range type data
 */
@SuppressWarnings("serial")
public class RangeSeries extends DataSeries {
    public RangeSeries() {
    }

    /**
     * Constructs a RangeSeries with the given name
     *
     * @param name
     */
    public RangeSeries(String name) {
        setName(name);
    }

    /**
     * Constructs a RangeSeries with the given values
     *
     * @param values
     *            low-high pairs, or x-low-high triplets
     */
    public RangeSeries(Number[]... values) {
        addRangeData(values);
    }

    private void addRangeData(Number[][] values) {
        if (values == null || values.length == 0) {
            return;
        }
        Number[] firstItem = values[0];
        if (firstItem.length == 2) {
            addLowHighPairs(values);
        } else if (firstItem.length == 3) {
            addTriples(values);
        } else {
            throw new IllegalArgumentException("Unregognized data format");
        }
    }

    private void addTriples(Number[][] values) {
        for (int i = 0; i < values.length; i++) {
            Number[] numbers = values[i];
            add(new DataSeriesItem(numbers[0], numbers[1], numbers[2]));
        }
    }

    private void addLowHighPairs(Number[][] values) {
        for (int i = 0; i < values.length; i++) {
            Number[] numbers = values[i];
            DataSeriesItem dataSeriesItem = new DataSeriesItem();
            dataSeriesItem.setLow(numbers[0]);
            dataSeriesItem.setHigh(numbers[1]);
            add(dataSeriesItem);
        }
    }

    /**
     * Constructs a RangeSeries with the given name and values
     *
     * @param name
     *            the name of the series
     * @param values
     *            low-high pairs, or x-low-high triplets
     */
    public RangeSeries(String name, Number[]... values) {
        this(name);
        addRangeData(values);
    }

    /**
     * Sets the numeric data for this series.
     *
     * @param values
     *            low-high pairs, or x-low-high triplets
     */
    public void setRangeData(Number[]... data) {
        clear();
        addRangeData(data);
    }

}

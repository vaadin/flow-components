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
 * A specialized series for use with HeatMaps
 */
public class HeatSeries extends AbstractSeries {

    Number[][] data;

    public HeatSeries() {
    }

    /**
     * Constructs a HeatSeries with the given name
     *
     * @param name
     *            The name of this data series.
     */
    public HeatSeries(String name) {
        setName(name);
    }

    /**
     * Constructs a HeatSeries with the given name and values
     *
     * @param name
     *            the name of the series
     * @param values
     *            x-y-heatScore triplets
     */
    public HeatSeries(String name, Number[]... values) {
        this(name);
        setData(values);
    }

    /**
     * Sets the numeric data for this series.
     *
     * @param values
     *            x-y-heatScore triplets
     */
    public void setData(Number[]... values) {
        clear();
        addHeatData(values);
    }

    /**
     * @see #setData(Number[]...)
     * @return the raw data in this series
     */
    public Number[][] getData() {
        return data;
    }

    /**
     * Add a single data point to the heat series
     *
     * @param x
     *            the x coordinate of the point
     * @param y
     *            the y coordinate of the point
     * @param heatScore
     *            the heat score of the point
     */
    public void addHeatPoint(int x, int y, Number heatScore) {
        addHeatData(new Number[][] { { x, y, heatScore } });
    }

    public void clear() {
        data = null;
    }

    private void addHeatData(Number[][] values) {
        if (values == null || values.length == 0) {
            return;
        }
        Number[] firstItem = values[0];
        if (firstItem.length == 3) {
            if (data == null) {
                data = values;
            } else { // Append
                Number[][] newData = new Number[data.length + values.length][3];
                System.arraycopy(data, 0, newData, 0, data.length);
                System.arraycopy(values, 0, newData, data.length,
                        values.length);
                data = newData;
            }
        } else {
            throw new IllegalArgumentException(
                    "The data should be x,y,heatScore triplets");
        }
    }
}

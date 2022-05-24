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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.style.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An array of data points to be displayed in a chart.
 * <p>
 * The class uses {@link DataSeriesItem} to represent individual data points.
 * The class also has various helper methods and constructors that allow passing
 * data as arrays or lists.
 *
 * @see ListSeries
 * @see RangeSeries
 * @see HeatSeries
 */
public class DataSeries extends AbstractSeries {

    private List<DataSeriesItem> data = new ArrayList<>();

    @JsonIgnore
    private List<Series> drilldownSeries = new ArrayList<>();

    /**
     * Constructs an empty {@link DataSeries}. Developers should then populate
     * the series with various addData and setData methods.
     */
    public DataSeries() {
    }

    /**
     * Constructs a DataSeries instance containing the given category name, Y
     * value pairs.
     *
     * @param categories
     * @param ys
     */
    public DataSeries(String[] categories, Number[] ys) {
        for (int i = 0; i < categories.length; i++) {
            add(new DataSeriesItem(categories[i], ys[i]));
        }
    }

    /**
     * Constructs a new DataSeries instance with the given name.
     *
     */
    public DataSeries(String name) {
        setName(name);
    }

    /**
     * Constructs a new DataSeries instance with the given items.
     *
     * @param items
     *            items to be contained in the constructed DataSeries
     */
    public DataSeries(List<DataSeriesItem> items) {
        setData(items);
    }

    /**
     * Constructs a new DataSeries with the given items.
     *
     * @param items
     *            items to be contained in the constructed DataSeries
     */
    public DataSeries(DataSeriesItem... items) {
        setData(Arrays.asList(items));
    }

    /**
     * Adds a list of (x,y) data pairs
     *
     * e.g. <code>[[0, 15], [10, -50], [20, -56.5]...</code>
     *
     * could be inserted as follows
     *
     * <code>new Number[][] { { 0, 15 }, { 10, -50 }, { 20, -56.5 }</code>
     *
     * @param entries
     *            An array of Numbers representing the (x,y) data pairs.
     */
    public void addData(Number[][] entries) {
        for (Number[] entry : entries) {
            data.add(new DataSeriesItem(entry[0], entry[1]));
        }
    }

    /**
     * Sets the data entries, first clearing the old ones. Uses the given
     * category names and numeric values.
     *
     * The categoryNames and values arrays must be of equal length.
     *
     * @param categoryNames
     *            An array of the category names.
     * @param values
     *            An array of the values for each category in the categoryNames
     *            parameter.
     */
    public void setData(String[] categoryNames, Number[] values) {
        assert (categoryNames.length == values.length);
        data.clear();
        for (int i = 0; i < categoryNames.length; i++) {
            data.add(new DataSeriesItem(categoryNames[i], values[i]));
        }
    }

    /**
     * Sets the data entries, first clearing the old ones. Uses the given
     * category names, numeric values, and colors.
     *
     * The categoryNames, values and colors arrays must be of equal length.
     *
     * @param categoryNames
     *            An array of the category names.
     * @param values
     *            An array of the values for each category in the categoryNames
     *            parameter.
     * @param colors
     *            An array of colors for each category name, value pair.
     */
    public void setData(String[] categoryNames, Number[] values,
            Color[] colors) {
        assert (categoryNames.length == values.length);
        assert (categoryNames.length == colors.length);
        data.clear();
        for (int i = 0; i < categoryNames.length; i++) {
            DataSeriesItem item = new DataSeriesItem(categoryNames[i],
                    values[i]);
            item.setColor(colors[i]);
            data.add(item);
        }
    }

    /**
     * Sets the data entries, first clearing the old ones. Uses the same numeric
     * value for names (value.toString) and Y-values.
     *
     * @param values
     */
    public void setData(Number... values) {
        data.clear();
        for (int i = 0; i < values.length; i++) {
            data.add(new DataSeriesItem("" + values[i], values[i]));
        }
    }

    /**
     * Sets the data to the provided list of data items.
     *
     * @param data
     */
    public void setData(List<DataSeriesItem> data) {
        this.data = data;
    }

    /**
     * @param name
     *            The name of the data item to find.
     * @return The first {@link DataSeriesItem} identified by the specified
     *         name. Returns null if no matching item is found.
     */
    public DataSeriesItem get(String name) {
        for (DataSeriesItem item : data) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Adds a data item and immediately updates the chart if it already has been
     * drawn. If the chart has not yet been drawn, all items added will be added
     * to the chart when the chart is drawn.
     *
     * @see #add(DataSeriesItem, boolean, boolean)
     * @param item
     *            the data item to be added
     */
    public void add(DataSeriesItem item) {
        add(item, true, false);
    }

    /**
     * Adds a new item to the series data. And sets the series as its drilldown.
     * Used for eager loading drilldown. Series must have an id.
     *
     * The remaining drilldown configurations can be set in
     * {@link Configuration#getDrilldown()}
     *
     * @param item
     * @param series
     */
    public void addItemWithDrilldown(DataSeriesItem item, Series series) {
        add(item);
        if (series.getId() == null) {
            throw new IllegalArgumentException("Series ID may not be null");
        }
        item.setDrilldown(series.getId());
        addSeriesToDrilldownConfiguration(series);
    }

    /**
     * Adds a new item to the series data and enables drilldown for it. Used for
     * lazy loading drilldown. Using async drilldown requires setting
     * {@link Chart#setDrilldownCallback(DrilldownCallback)} to return a
     * {@link Series} when drilldown is done.
     *
     * The remaining drilldown configurations can be set in
     * {@link Configuration#getDrilldown()}
     *
     * @param item
     */
    public void addItemWithDrilldown(DataSeriesItem item) {
        add(item);
        item.setDrilldown(true);
    }

    private void addSeriesToDrilldownConfiguration(Series series) {
        if (getConfiguration() != null) {
            Drilldown drilldown = getConfiguration().getDrilldown();
            drilldown.addSeries(series);
        } else {
            drilldownSeries.add(series);
        }
    }

    boolean hasDrilldownSeries() {
        return !drilldownSeries.isEmpty();
    }

    List<Series> getDrilldownSeries() {
        return drilldownSeries;
    }

    /**
     * Adds a data item and immediately sends an update to the chart if so
     * specified. Immediately updating the chart causes it to dynamically add
     * the data point.
     * <p>
     * This method is useful if you want to add many items without a
     * client/server round-trip for each item added. Do this by setting the
     * updateChartImmediately parameter to false.
     *
     * @param item
     *            The item to add.
     * @param updateChartImmediately
     *            Updates the chart immediately if true.
     * @param shift
     *            If true, the first item from the series is removed. Handy if
     *            dynamically adjusting adding points and fixed amount of points
     *            should be kept visible.
     */
    public void add(DataSeriesItem item, boolean updateChartImmediately,
            boolean shift) {
        if (shift) {
            data.remove(0);
        }
        data.add(item);
        if (updateChartImmediately && getConfiguration() != null) {
            getConfiguration().fireDataAdded(this, item, shift);
        }
    }

    /**
     * Removes a given item and immediately removes it from the chart.
     *
     * @param item
     *            The item to remove.
     */
    public void remove(DataSeriesItem item) {
        int index = data.indexOf(item);
        data.remove(index);
        if (getConfiguration() != null) {
            getConfiguration().fireDataRemoved(this, index);
        }
    }

    /**
     * Return an unmodifiable list of the data items in this series.
     */
    public List<DataSeriesItem> getData() {
        return Collections.unmodifiableList(data);
    }

    /**
     * Triggers an update of the chart for the specified data item. Only the Y
     * value of the DataSeriesItem is updated.
     *
     * @param item
     *            The item to update.
     */
    public void update(DataSeriesItem item) {
        if (getConfiguration() != null) {
            getConfiguration().fireDataUpdated(this, item, data.indexOf(item));
        }
    }

    /**
     * Returns {@link DataSeriesItem} at given index
     *
     * @param index
     * @return the Item
     * @throws IndexOutOfBoundsException
     *             if data series don't have item at given index
     */
    public DataSeriesItem get(int index) {
        return data.get(index);
    }

    /**
     * @return the number of data items in the series
     */
    public int size() {
        return data.size();
    }

    /**
     * Removes all items from the series.
     */
    public void clear() {
        data.clear();
    }

    /**
     * Sets a new sliced value to the item with the specified index
     *
     * @param index
     *            Index of the Item to modify
     * @param sliced
     *            When true, the point is sliced out. When false, the point is
     *            set in. When null the sliced state is toggled
     */
    public void setItemSliced(int index, boolean sliced) {
        setItemSliced(index, sliced, true, true);
    }

    /**
     * Sets a new sliced value to the item with the specified index
     *
     * @param index
     *            Index of the Item to modify
     * @param sliced
     *            When true, the point is sliced out. When false, the point is
     *            set in. When null the sliced state is toggled
     * @param redraw
     *            Whether to redraw the chart after the point is altered.
     */
    public void setItemSliced(int index, boolean sliced, boolean redraw) {
        setItemSliced(index, sliced, redraw, true);
    }

    /**
     * Sets a new sliced value to the item with the specified index
     *
     * @param index
     *            Index of the Item to modify
     * @param sliced
     *            When true, the point is sliced out. When false, the point is
     *            set in. When null the sliced state is toggled
     * @param redraw
     *            Whether to redraw the chart after the point is altered.
     * @param animation
     *            When true, the move will be animated with default animation
     *            options
     */
    public void setItemSliced(int index, boolean sliced, boolean redraw,
            boolean animation) {
        DataSeriesItem item = get(index);
        item.setSliced(sliced);
        getConfiguration().fireItemSliced(this, index, sliced, redraw,
                animation);
    }
}

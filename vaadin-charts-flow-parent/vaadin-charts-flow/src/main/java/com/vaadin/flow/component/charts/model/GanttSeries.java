/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A series of tasks to be used in the Gantt chart. Each task (represented by
 * {@link GanttSeriesItem}) has a start and an end date.
 */
@SuppressWarnings("unused")
public class GanttSeries extends AbstractSeries {

    private List<GanttSeriesItem> data = new ArrayList<>();

    public GanttSeries() {
    }

    /**
     * Constructs a GanttSeries with the given name
     *
     * @param name
     *            The name of this series.
     */
    public GanttSeries(String name) {
        super(name);
    }

    /**
     * Constructs a GanttSeries with the given name and data
     *
     * @param name
     *            The name of this series
     * @param data
     *            The data of this series
     */
    public GanttSeries(String name, Collection<GanttSeriesItem> data) {
        this(name);
        setData(data);
    }

    /**
     * Return an unmodifiable copy of the items in this series.
     *
     * @return
     * @see #setData(Collection)
     */
    public Collection<GanttSeriesItem> getData() {
        return Collections.unmodifiableCollection(data);
    }

    /**
     * Set the list of {@link GanttSeriesItem} in this series.
     *
     * @param data
     */
    public void setData(Collection<GanttSeriesItem> data) {
        this.data = new LinkedList<>(data);
    }

    /**
     * Remove all items in the series.
     */
    public void clearSeries() {
        data.clear();
    }

    /**
     * Add given item to the series
     *
     * @param item
     */
    public void add(GanttSeriesItem item) {
        data.add(item);
    }

    /**
     * Add all the given items to the series
     *
     * @param items
     */
    public void addAll(GanttSeriesItem... items) {
        data.addAll(Arrays.asList(items));
    }

    /**
     * Returns {@link GanttSeriesItem} at given index
     *
     * @param index
     * @return the Item
     * @throws IndexOutOfBoundsException
     *             if data series don't have item at given index
     */
    public GanttSeriesItem get(int index) {
        return data.get(index);
    }

    /**
     * @return the number of data items in the series
     */
    public int size() {
        return data.size();
    }

}

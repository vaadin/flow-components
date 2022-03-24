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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of {@link TreeSeriesItem TreeSeriesItems} that compose a
 * multi-root tree. This series is meant to be used with
 * {@link ChartType#TREEMAP} charts.
 */
public class TreeSeries extends AbstractSeries {

    private List<TreeSeriesItem> data = new LinkedList<>();

    public TreeSeries() {
    }

    /**
     * Constructs a TreeSeries with the given name
     *
     * @param name
     *            The name of this series.
     */
    public TreeSeries(String name) {
        super(name);
    }

    /**
     * Constructs a TreeSeries with the given name and data
     *
     * @param name
     *            The name of this series
     * @param data
     *            The data of this series
     */
    public TreeSeries(String name, Collection<TreeSeriesItem> data) {
        this(name);
        setData(data);
    }

    /**
     * Return an unmodifiable copy of the items in this series.
     *
     * @return
     * @see #setData(Collection)
     */
    public Collection<TreeSeriesItem> getData() {
        return Collections.unmodifiableCollection(data);
    }

    /**
     * Set the list of {@link TreeSeriesItem} in this series. The items are the
     * whole tree, and the list is not ordered.
     *
     * @param data
     */
    public void setData(Collection<TreeSeriesItem> data) {
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
    public void add(TreeSeriesItem item) {
        data.add(item);
    }

    /**
     * Add all the given items to the series
     *
     * @param items
     */
    public void addAll(TreeSeriesItem... items) {
        data.addAll(Arrays.asList(items));
    }
}

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

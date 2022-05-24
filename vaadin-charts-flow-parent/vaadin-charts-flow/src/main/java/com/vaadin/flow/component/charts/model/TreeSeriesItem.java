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
 * This class represents one node in a {@link TreeSeries}.
 */
public class TreeSeriesItem extends AbstractSeriesItem {

    private String parent;
    private Number value;
    private Number colorValue;

    public TreeSeriesItem() {
    }

    public TreeSeriesItem(String id, String name) {
        setId(id);
        setName(name);
    }

    public TreeSeriesItem(String name, TreeSeriesItem parent, int value) {
        setName(name);
        setParent(parent);
        setValue(value);
    }

    public TreeSeriesItem(String name, int value) {
        setName(name);
        setValue(value);
    }

    /**
     * @see #setParent(String)
     * @return
     */
    public String getParent() {
        return parent;
    }

    /**
     * The value should be the id of the point which is the parent. If no points
     * has a matching id, or this option is null, then the parent will be set to
     * the root. Defaults to null.
     *
     * @param parent
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * Set the parent of this node.
     *
     * @param parent
     */
    public void setParent(TreeSeriesItem parent) {
        String parentString = (parent != null ? parent.getId() : null);
        setParent(parentString);
    }

    /**
     * @see #setValue(Number)
     * @return
     */
    public Number getValue() {
        return value;
    }

    /**
     * Set the numeric value of this node
     *
     * @param value
     */
    public void setValue(Number value) {
        this.value = value;
    }

    /**
     * @see #setColorValue(Number)
     * @return
     */
    public Number getColorValue() {
        return colorValue;
    }

    /**
     * This value will decide which color the point gets from the scale of the
     * colorAxis.
     *
     * @param colorValue
     */
    public void setColorValue(Number colorValue) {
        this.colorValue = colorValue;
    }
}

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

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.util.Util;

import java.time.Instant;
import java.util.Date;

/**
 * Abstract superclass for chart series items
 *
 */
public class AbstractSeriesItem extends AbstractConfigurationObject {

    private String name;
    private Number y;
    private Number x;
    private Boolean sliced;
    private Number colorIndex;
    private Color color;
    private Number legendIndex;
    private Marker marker;
    private String id;
    private String className;

    /**
     * Returns the name of the item.
     *
     * @see #setName(String)
     * @return The name of the data item or null if not defined.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the data item as shown in the legend, tooltip, dataLabel
     * etc. Defaults to "".
     *
     * @param name
     *            Name of the item.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the Y-value of the item.
     *
     * @see #setY(Number)
     * @return The Y value of this data item.
     */
    public Number getY() {
        return y;
    }

    /**
     * Sets the Y value of this data item. Defaults to null.
     *
     * @param y
     *            Y-value of the item.
     */
    public void setY(Number y) {
        this.y = y;
    }

    /**
     * Returns the X-value of the item.
     *
     * @see #setX(Number)
     * @return The X value of this data item.
     */
    public Number getX() {
        return x;
    }

    /**
     * Sets the X value of this data item. Defaults to null.
     *
     * @param x
     *            X-value of the item.
     */
    public void setX(Number x) {
        this.x = x;
    }

    /**
     * Sets the given instant as the x value.
     *
     * @param instant
     *            Instant to set.
     */
    public void setX(Instant instant) {
        setX(Util.toHighchartsTS(instant));
    }

    /**
     * @deprecated as of 4.0. Use {@link #setX(Instant)}
     */
    @Deprecated
    public void setX(Date date) {
        setX(Util.toHighchartsTS(date));
    }

    /**
     * Checks whether or not the item is sliced. Makes sense only in pie charts.
     *
     * @see #setSliced(boolean)
     * @return <b>true</b> when this data item is displayed offset from the
     *         center in a pie chart, <b>false</b> otherwise.
     */
    public boolean getSliced() {
        return sliced != null && sliced;
    }

    /**
     * Sets whether to display a slice offset from the center. Defaults to
     * false.
     *
     * <em>Note</em>: This applies to pie charts only.
     *
     * @param sliced
     *            When <b>true</b>, this item should be displayed with a small
     *            offset from the centre of the pie chart; when <b>false</b>,
     *            this item will be rendered normally.
     */
    public void setSliced(boolean sliced) {
        this.sliced = sliced;
    }

    /**
     * Returns the color of the item.
     *
     * @see #setColor(Color)
     * @return The color of the item.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the individual color for the point. Defaults to null. This might not
     * work for all chart types.
     *
     * @param color
     *            Color of the item.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Returns the colorIndex of the item.
     *
     * @see #setColorIndex(Number)
     * @return The colorIndex of the item.
     */
    public Number getColorIndex() {
        return colorIndex;
    }

    /**
     * A specific color index to use for the point, so its graphic
     * representations are given the class name highcharts-color-{n}.
     *
     * @param colorIndex
     *            Color of the item.
     */
    public void setColorIndex(Number colorIndex) {
        this.colorIndex = colorIndex;
    }

    /**
     * Returns the index of the legend. Applicable only to pie charts.
     *
     * @see #setLegendIndex(Number)
     * @return The index of the legend or null if not defined. Only applicable
     *         for pie charts.
     */
    public Number getLegendIndex() {
        return legendIndex;
    }

    /**
     * Sets the sequential index of the pie slice in the legend. Defaults to
     * undefined.
     *
     * <em>Note</em> This applies to pie charts only.
     *
     * @param legendIndex
     *            Index in the legend.
     */
    public void setLegendIndex(Number legendIndex) {
        this.legendIndex = legendIndex;
    }

    /**
     * Sets the marker of this data series item
     *
     * @param marker
     *            Marker of the item.
     */
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    /**
     * Returns the marker of the item.
     *
     * @see #setMarker(Marker)
     * @return The marker of this data series item. If none is specified a new
     *         {@link Marker} will be created.
     */
    public Marker getMarker() {
        if (marker == null) {
            marker = new Marker();
            marker.setEnabled(true);
        }
        return marker;
    }

    /**
     * Returns the id of the item.
     *
     * @see #setId(String)
     * @return The ID of the item.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID for the point. This can be used after rendering to get a
     * reference to the point object. Defaults to null.
     *
     * @param id
     *            New id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the class name of the item
     *
     * @see #setClassName(String)
     * @return The class name of the item
     */
    public String getClassName() {
        return className;
    }

    /**
     * An additional, individual class name for the data point's graphic
     * representation.
     *
     * @param className
     *            new class name of the item
     */
    public void setClassName(String className) {
        this.className = className;
    }
}

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
import com.vaadin.flow.component.charts.model.style.Color;

import java.time.Instant;
import java.util.Date;

/**
 * The DataSeriesItem class represents a single entry in a {@link DataSeries}.
 */
@SuppressWarnings("serial")
public class DataSeriesItem extends AbstractSeriesItem {

    private Number low;
    private Number high;
    private Boolean selected;
    private Dial dial;
    private Object drilldown;
    private DataLabels dataLabels;
    private String cursor;
    private String description;

    /*
     * Flag to indicate if this item can be passed in optimized form to
     * rendering library.
     */
    @JsonIgnore
    private boolean customized = false;

    /**
     * Creates an empty item, without values, colors, etc.
     */
    public DataSeriesItem() {
    }

    /**
     * Constructs an item with a name and a Y value
     *
     * @param name
     *            Name of the item.
     * @param y
     *            Y-value of the item.
     */
    public DataSeriesItem(String name, Number y) {
        setName(name);
        setY(y);
        makeCustomized();
    }

    /**
     * Constructs an item with a name and a value on the Y-axis and assigns the
     * specified color to the item.
     *
     * @param name
     *            Name of the item.
     * @param y
     *            Y-value of the item.
     * @param color
     *            Color of the item.
     */
    public DataSeriesItem(String name, Number y, Color color) {
        setName(name);
        setY(y);
        setColor(color);
        makeCustomized();
    }

    /**
     * Constructs an item with X and Y values
     *
     * @param x
     *            X-value of the item.
     * @param y
     *            Y-value of the item.
     */
    public DataSeriesItem(Number x, Number y) {
        setX(x);
        setY(y);
    }

    /**
     * Constructs an item with numerical values for the X and Y axes and assigns
     * the specified color to the item.
     *
     * @param x
     *            X-value of the item.
     * @param y
     *            Y-value of the item.
     * @param color
     *            Color of the item.
     */
    public DataSeriesItem(Number x, Number y, Color color) {
        setX(x);
        setY(y);
        setColor(color);
        makeCustomized();
    }

    /**
     * Constructs a DataSeriesItem with the given instant as X value and Y
     * value.
     *
     * @param instant
     *            Instant of the item, as its X-value.
     * @param y
     *            Y-value of the item.
     */
    public DataSeriesItem(Instant instant, Number y) {
        setX(instant);
        setY(y);
    }

    /**
     * @deprecated as of 4.0. Use {@link #DataSeriesItem(Instant, Number)}
     */
    @Deprecated
    public DataSeriesItem(Date date, Number y) {
        setX(date);
        setY(y);
    }

    /**
     * Constructs a DataSeriesItem with the given instant as X value with min
     * and max values for use in range visualizations.
     *
     * @param instant
     *            Instant of the item, as its X-value.
     * @param low
     *            Lower value for range visualization.
     * @param high
     *            Upper value for range visualization.
     */
    public DataSeriesItem(Instant instant, Number low, Number high) {
        setX(instant);
        setLow(low);
        setHigh(high);
    }

    /**
     * @deprecated as of 4.0. Use
     *             {@link #DataSeriesItem(Instant, Number,Number)}
     */
    @Deprecated
    public DataSeriesItem(Date date, Number low, Number high) {
        setX(date);
        setLow(low);
        setHigh(high);
    }

    /**
     * Constructs a DataSeriesItem with the given X, min and max values for use
     * in range visualizations.
     *
     * @param x
     *            X-value of the item.
     * @param low
     *            Lower value for range visualization.
     * @param high
     *            Upper value for range visualization.
     */
    public DataSeriesItem(Number x, Number low, Number high) {
        setX(x);
        setLow(low);
        setHigh(high);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        super.setName(name);
        makeCustomized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSliced(boolean sliced) {
        super.setSliced(sliced);
        makeCustomized();
    }

    /**
     * Checks whether or not the item is selected.
     *
     * @return <b>true</b> if the item is selected, <b>false</b> otherwise.
     * @see #setSelected(Boolean)
     */
    public boolean isSelected() {
        return selected == null ? false : selected;
    }

    /**
     * Sets whether the data item is selected or not.
     *
     * @param selected
     *            Whether or not the item should be selected.
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
        makeCustomized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(String id) {
        super.setId(id);
        makeCustomized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLegendIndex(Number legendIndex) {
        super.setLegendIndex(legendIndex);
        makeCustomized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMarker(Marker marker) {
        super.setMarker(marker);
        makeCustomized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(Color color) {
        super.setColor(color);
        makeCustomized();
    }

    /**
     * Sets the dial or arrow pointer of the gauge.
     * <p/>
     * <em>Note</em> This is only applicable for gauge charts.
     *
     * @param dial
     *            Dial to use.
     */
    public void setDial(Dial dial) {
        this.dial = dial;
        makeCustomized();
    }

    /**
     * Returns the current dial. This is only applicable for gauge charts.
     *
     * @return The dial or arrow pointer of a gauge chart. Only applicable for
     *         gauge charts.
     * @see #setDial(Dial)
     */
    public Dial getDial() {
        return dial;
    }

    /**
     * Checks if the data can be rendered in an optimized manner.
     *
     * @return <b>true</b> if the data series item can be rendered in optimized
     *         manner, <b>false</b> otherwise.
     */
    public boolean isCustomized() {
        return customized;
    }

    /**
     * Marks the item as customized, so that it can be rendered in a more
     * optimal way.
     */
    protected void makeCustomized() {
        customized = true;
    }

    /**
     * Returns the lower range for visualizations.
     *
     * @return The lower range.
     */
    public Number getLow() {
        return low;
    }

    /**
     * Sets the lower range for visualizations.
     *
     * @param low
     *            New lower range.
     */
    public void setLow(Number low) {
        this.low = low;
    }

    /**
     * Returns the upper range for visualizations.
     *
     * @return The upper range.
     */
    public Number getHigh() {
        return high;
    }

    /**
     * Sets the upper range for visualizations.
     *
     * @param high
     *            New upper range.
     */
    public void setHigh(Number high) {
        this.high = high;
    }

    /**
     * The ID of a series in the {@link Drilldown#addSeries(Series)} list to use
     * for a drilldown for this point. If the value doesn't correspond to the ID
     * of a series the point will be shown as if drilldown was enabled and a
     * {@link DrilldownCallback} will be triggered when user clicks in a point.
     *
     * @param drilldown
     */
    void setDrilldown(String drilldown) {
        this.drilldown = drilldown;
    }

    /**
     * True to enable drilldown and a {@link DrilldownCallback} will be
     * triggered when user clicks in a point.
     *
     * @param drilldown
     */
    void setDrilldown(Boolean drilldown) {
        this.drilldown = drilldown;
    }

    /**
     * @see #setDataLabels(DataLabels)
     * @return dataLabels
     */
    public DataLabels getDataLabels() {
        return dataLabels;
    }

    /**
     * Set the label configuration for this item
     *
     * @param dataLabels
     */
    public void setDataLabels(DataLabels dataLabels) {
        this.dataLabels = dataLabels;
        makeCustomized();
    }

    /**
     * @see #setCursor(String)
     * @return cursor
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Sets the <code>cursor</code> CSS attribute to be shown on mouse over
     * <p>
     * Accepts CSS <code>cursor</code> values like: alias, all-scroll, auto,
     * cell, context-menu, col-resize, copy, crosshair, default, e-resize,
     * ew-resize, grab, grabbing, help, move, n-resize, ne-resize, nesw-resize,
     * ns-resize, nw-resize, nwse-resize, no-drop, none, not-allowed, pointer,
     * progress, row-resize, s-resize, se-resize, sw-resize, text,
     * vertical-text, w-resize, wait, zoom-in, zoom-out
     * <p>
     * Note that not all browsers have support for all values.
     *
     * @param cursor
     */
    public void setCursor(String cursor) {
        this.cursor = cursor;
        makeCustomized();
    }

    /**
     * @see #setDescription(String)
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * <i>Requires Accessibility module</i>
     * </p>
     * <p>
     * A description of the series to add to the screen reader information about
     * the series.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setDescription(String description) {
        this.description = description;
        makeCustomized();
    }

}

/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.controls;

import java.util.Objects;

import com.vaadin.flow.component.map.configuration.Constants;

/**
 * A control that displays a scale indicator on the map. The control can be
 * displayed as a simple scale line or as a more detailed scale bar with
 * segments.
 *
 * @see DisplayMode
 */
public class ScaleControl extends Control {
    private int minWidth = 64;
    private Integer maxWidth;
    private Unit units = Unit.METRIC;
    private DisplayMode displayMode = DisplayMode.LINE;
    private int scaleBarSteps = 4;
    private boolean scaleBarTextVisible = false;

    @Override
    public String getType() {
        return Constants.OL_CONTROL_SCALE_LINE;
    }

    /**
     * Returns the minimum width of the scale control in pixels.
     *
     * @return the minimum width in pixels
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * Sets the minimum width of the scale control in pixels. Default value is
     * {@code 64}.
     *
     * @param minWidth
     *            the minimum width in pixels
     */
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        markAsDirty();
    }

    /**
     * Returns the maximum width of the scale control in pixels, or {@code null}
     * if no maximum is set.
     *
     * @return the maximum width in pixels, or {@code null}
     */
    public Integer getMaxWidth() {
        return maxWidth;
    }

    /**
     * Sets the maximum width of the scale control in pixels. Set to
     * {@code null} to remove the limit. Default value is {@code null}.
     *
     * @param maxWidth
     *            the maximum width in pixels, or {@code null} for no limit
     */
    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        markAsDirty();
    }

    /**
     * Returns the units used for the scale control.
     *
     * @return the units
     */
    public Unit getUnits() {
        return units;
    }

    /**
     * Sets the units to use for the scale control. Default value is
     * {@link Unit#METRIC}.
     *
     * @param units
     *            the units, not null
     * @throws NullPointerException
     *             if units is null
     */
    public void setUnits(Unit units) {
        this.units = Objects.requireNonNull(units, "Units cannot be null");
        markAsDirty();
    }

    /**
     * Returns the display mode of the scale control.
     *
     * @return the display mode
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Sets the display mode of the scale control. Default value is
     * {@link DisplayMode#LINE}.
     *
     * @param displayMode
     *            the display mode, not null
     * @throws NullPointerException
     *             if displayMode is null
     */
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = Objects.requireNonNull(displayMode,
                "Display mode cannot be null");
        markAsDirty();
    }

    /**
     * Returns the number of steps (segments) in the scale bar. This is only
     * used when {@link #getDisplayMode()} returns {@link DisplayMode#BAR}.
     *
     * @return the number of scale bar steps
     */
    public int getScaleBarSteps() {
        return scaleBarSteps;
    }

    /**
     * Sets the number of steps (segments) for the scale bar. This is only used
     * when {@link #setDisplayMode(DisplayMode)} is set to
     * {@link DisplayMode#BAR}. Default value is {@code 4}.
     *
     * @param scaleBarSteps
     *            the number of scale bar steps
     */
    public void setScaleBarSteps(int scaleBarSteps) {
        this.scaleBarSteps = scaleBarSteps;
        markAsDirty();
    }

    /**
     * Returns whether to show the scale as proportion below the scale bar. This
     * is only used when {@link #getDisplayMode()} returns
     * {@link DisplayMode#BAR}.
     *
     * @return {@code true} if text is shown below the scale bar
     */
    public boolean isScaleBarTextVisible() {
        return scaleBarTextVisible;
    }

    /**
     * Sets whether to show the scale as proportion below. This is only used
     * when {@link #setDisplayMode(DisplayMode)} is set to
     * {@link DisplayMode#BAR}. Default value is {@code false}.
     *
     * @param scaleBarTextVisible
     *            {@code true} to show text below the scale bar
     */
    public void setScaleBarTextVisible(boolean scaleBarTextVisible) {
        this.scaleBarTextVisible = scaleBarTextVisible;
        markAsDirty();
    }

    /**
     * Display mode for the scale control.
     */
    public enum DisplayMode {
        /**
         * Display as a simple line with a text label.
         */
        LINE,
        /**
         * Display as a segmented bar with alternating colors.
         */
        BAR
    }

    /**
     * Units for the scale control.
     */
    public enum Unit {
        /**
         * Degrees
         */
        DEGREES,
        /**
         * Imperial units (miles, feet)
         */
        IMPERIAL,
        /**
         * Nautical miles
         */
        NAUTICAL,
        /**
         * Metric units (kilometers, meters)
         */
        METRIC,
        /**
         * US customary units (miles, feet)
         */
        US
    }
}

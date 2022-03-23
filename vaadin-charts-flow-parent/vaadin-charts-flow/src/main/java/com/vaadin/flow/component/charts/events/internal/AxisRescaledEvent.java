package com.vaadin.flow.component.charts.events.internal;

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

import java.io.Serializable;

/**
 * Event for information about rescaling the axis. Wraps
 * <code>Axis.setExtremes()</code>.
 *
 * @since 2.0
 */
public class AxisRescaledEvent implements Serializable {

    private static final long serialVersionUID = 20141118;

    private final int axis;

    private final int axisIndex;

    private final Number minimum;

    private final Number maximum;

    private final boolean redraw;

    private final boolean animate;

    /**
     * Constructs the event.
     *
     * @param axis
     *            Number depicting the dimension of the axis. 0 is X, 1 is Y,
     *            etc.
     * @param axisIndex
     *            Index of the axis.
     * @param minimum
     *            Minimum value on the axis.
     * @param maximum
     *            Maximum value on the axis.
     * @param redraw
     *            Whether or not to redraw the chart.
     * @param animate
     *            Whether or not to animate the rescaling.
     */
    public AxisRescaledEvent(int axis, int axisIndex, Number minimum,
            Number maximum, boolean redraw, boolean animate) {
        this.axis = axis;
        this.axisIndex = axisIndex;
        this.minimum = minimum;
        this.maximum = maximum;
        this.redraw = redraw;
        this.animate = animate;
    }

    /**
     * Constructs the event with animated transition.
     *
     * @param axis
     *            Number depicting the dimension of the axis. 0 is X, 1 is Y,
     *            etc.
     * @param axisIndex
     *            The axis that was resized.
     * @param minimum
     *            Minimum value on the axis.
     * @param maximum
     *            Maximum value on the axis.
     * @param redraw
     *            Whether or not to redraw the chart.
     */
    public AxisRescaledEvent(int axis, int axisIndex, Number minimum,
            Number maximum, boolean redraw) {
        this(axis, axisIndex, minimum, maximum, redraw, true);
    }

    /**
     * Constructs the event with animated transition, redraws the chart.
     *
     * @param axis
     *            Number depicting the dimension of the axis. 0 is X, 1 is Y,
     *            etc.
     * @param axisIndex
     *            The axis that was resized.
     * @param minimum
     *            Minimum value on the axis.
     * @param maximum
     *            Maximum value on the axis.
     */
    public AxisRescaledEvent(int axis, int axisIndex, Number minimum,
            Number maximum) {
        this(axis, axisIndex, minimum, maximum, true, true);
    }

    /**
     * Returns the new minimum value on the axis.
     *
     * @return The new minimum value on the axis.
     */
    public Number getMinimum() {
        return minimum;
    }

    /**
     * Returns the new maximum value on the axis.
     *
     * @return The new maximum value on the axis.
     */
    public Number getMaximum() {
        return maximum;
    }

    /**
     * Checks whether or not to redraw the chart.
     *
     * @return <b>true</b> when chart needs to be redrawn, <b>false</b>
     *         otherwise.
     */
    public boolean isRedrawingNeeded() {
        return redraw;
    }

    /**
     * Checks whether or not animate the resizing.
     *
     * @return <b>true</b> when redrawing should be animated, <b>false</b>
     *         otherwise.
     */
    public boolean isAnimated() {
        return animate;
    }

    /**
     * Returns the axis the event is about.
     *
     * @return The axis that was rescaled.
     */
    public int getAxisIndex() {
        return axisIndex;
    }

    /**
     * Returns a number indicating axis dimension (X, Y, ...).
     *
     * @return Axis dimension.
     */
    public int getAxis() {
        return axis;
    }

}

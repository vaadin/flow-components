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

import com.vaadin.flow.component.charts.model.Series;

/**
 * Event for information about slicing a pie point
 *
 * @since 2.0
 */
public class ItemSlicedEvent extends AbstractSeriesEvent {

    private final int index;
    private final boolean sliced;
    private final boolean redraw;
    private final boolean animation;

    /**
     * Constructs the event.
     *
     * @param index
     * @param sliced
     * @param redraw
     * @param animation
     */
    public ItemSlicedEvent(Series series, int index, boolean sliced,
            boolean redraw, boolean animation) {
        super(series);
        this.index = index;
        this.sliced = sliced;
        this.redraw = redraw;
        this.animation = animation;
    }

    /**
     * Constructs the event with animated transition
     *
     * @param index
     * @param sliced
     * @param redraw
     */
    public ItemSlicedEvent(Series series, int index, boolean sliced,
            boolean redraw) {
        this(series, index, sliced, redraw, true);
    }

    /**
     * Constructs the event with animated transition, redraws the chart
     *
     * @param index
     * @param sliced
     */
    public ItemSlicedEvent(Series series, int index, boolean sliced) {
        this(series, index, sliced, true, true);
    }

    /**
     * Returns the index of the point to be sliced
     *
     * @return
     */
    public int getIndex() {
        return index;
    }

    /**
     * When true, the point is sliced out. When false, the point is set in. When
     * null or undefined, the sliced state is toggled.
     *
     * @return
     */
    public boolean isSliced() {
        return sliced;
    }

    /**
     * Whether or not redrawing should be immediate.
     *
     * @return <b>true</b> when redrawing needs to be immediate, <b>false</b>
     *         otherwise.
     */
    public boolean isRedraw() {
        return redraw;
    }

    /**
     * Whether or not animation should be used.
     *
     * @return <b>true</b> when slicing should be animated, <b>false</b>
     *         otherwise.
     */
    public boolean isAnimation() {
        return animation;
    }

}

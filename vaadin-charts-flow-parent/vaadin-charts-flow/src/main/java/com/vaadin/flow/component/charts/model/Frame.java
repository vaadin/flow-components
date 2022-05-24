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
 * Provides the option to draw a frame around the charts by defining a bottom,
 * front and back panel.
 */
public class Frame extends AbstractConfigurationObject {

    private Back back;
    private Bottom bottom;
    private Side side;
    private Top top;

    public Frame() {
    }

    /**
     * @see #setBack(Back)
     */
    public Back getBack() {
        if (back == null) {
            back = new Back();
        }
        return back;
    }

    /**
     * Defines the back panel of the frame around 3D charts.
     */
    public void setBack(Back back) {
        this.back = back;
    }

    /**
     * @see #setBottom(Bottom)
     */
    public Bottom getBottom() {
        if (bottom == null) {
            bottom = new Bottom();
        }
        return bottom;
    }

    /**
     * The bottom of the frame around a 3D chart.
     */
    public void setBottom(Bottom bottom) {
        this.bottom = bottom;
    }

    /**
     * @see #setSide(Side)
     */
    public Side getSide() {
        if (side == null) {
            side = new Side();
        }
        return side;
    }

    /**
     * <p>
     * Note: As of v5.0.12, <code>frame.left</code> or <code>frame.right</code>
     * should be used instead.
     * </p>
     *
     * <p>
     * The side for the frame around a 3D chart.
     * </p>
     */
    public void setSide(Side side) {
        this.side = side;
    }

    /**
     * @see #setTop(Top)
     */
    public Top getTop() {
        if (top == null) {
            top = new Top();
        }
        return top;
    }

    /**
     * The top of the frame around a 3D chart.
     */
    public void setTop(Top top) {
        this.top = top;
    }
}

/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
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

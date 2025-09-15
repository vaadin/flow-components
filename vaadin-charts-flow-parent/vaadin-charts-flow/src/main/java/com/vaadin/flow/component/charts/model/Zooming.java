/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Options for zooming configuration. This enables zooming in on plots and
 * provides options for controlling zoom behavior including mouse wheel support.
 */
public class Zooming extends AbstractConfigurationObject {

    private PanKey key;
    private ZoomingMouseWheel mouseWheel;
    private Dimension pinchType;
    private ZoomingResetButton resetButton;
    private Boolean singleTouch;
    private Dimension type;

    public Zooming() {
    }

    /**
     * @see #setKey(PanKey)
     */
    public PanKey getKey() {
        return key;
    }

    /**
     * Set a key to hold when dragging to zoom the chart. This is useful to
     * avoid zooming while moving points. Should be set different than
     * {@link ChartModel#getPanKey()}.
     * 
     * @param key
     */
    public void setKey(PanKey key) {
        this.key = key;
    }

    /**
     * @see #setMouseWheel(ZoomingMouseWheel)
     */
    public ZoomingMouseWheel getMouseWheel() {
        if (mouseWheel == null) {
            mouseWheel = new ZoomingMouseWheel();
        }
        return mouseWheel;
    }

    /**
     * Configuration for mouse wheel zoom.
     * 
     * @param mouseWheel
     */
    public void setMouseWheel(ZoomingMouseWheel mouseWheel) {
        this.mouseWheel = mouseWheel;
    }

    /**
     * @see #setPinchType(Dimension)
     */
    public Dimension getPinchType() {
        return pinchType;
    }

    /**
     * Equivalent to type, but for pinch gestures. By default, the pinchType is
     * the same as the {@link #getType()} setting.
     * 
     * @param pinchType
     */
    public void setPinchType(Dimension pinchType) {
        this.pinchType = pinchType;
    }

    /**
     * @see #setResetButton(ZoomingResetButton)
     */
    public ZoomingResetButton getResetButton() {
        if (resetButton == null) {
            resetButton = new ZoomingResetButton();
        }
        return resetButton;
    }

    /**
     * The button that appears after a selection zoom, allowing the user to
     * reset zoom.
     * 
     * @param resetButton
     */
    public void setResetButton(ZoomingResetButton resetButton) {
        this.resetButton = resetButton;
    }

    /**
     * @see #setSingleTouch(Boolean)
     */
    public Boolean getSingleTouch() {
        return singleTouch;
    }

    /**
     * Enables zooming by a single touch, in combination with
     * {@link #getType()}. When enabled, two-finger pinch will still work as set
     * up by {@link #getPinchType()}. However, single-touch will interfere with
     * touch-dragging the chart to read the tooltip. And especially when
     * vertical zooming is enabled, it will make it hard to scroll vertically on
     * the page.
     * 
     * @param singleTouch
     */
    public void setSingleTouch(Boolean singleTouch) {
        this.singleTouch = singleTouch;
    }

    /**
     * @see #setType(Dimension)
     */
    public Dimension getType() {
        return type;
    }

    /**
     * Decides in what dimensions the user can zoom by dragging the mouse.
     *
     * Note: For non-cartesian series, the only supported zooming type is
     * {@link Dimension#XY}, as zooming in a single direction is not applicable
     * due to the radial nature of the coordinate system.
     * 
     * @param type
     */
    public void setType(Dimension type) {
        this.type = type;
    }
}

/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.Chart;

/**
 * Configuration for mouse wheel zoom.
 * <p>
 * Zooming with the mouse wheel is enabled by default in
 * {@link Chart#setTimeline(Boolean)} charts. In other chart types, it is
 * enabled if {@link #getEnabled()} is set. It can be disabled by setting
 * {@link #setEnabled(Boolean)} option to false.
 */
public class ZoomingMouseWheel extends AbstractConfigurationObject {

    private Boolean enabled;
    private Number sensitivity;
    private Dimension type;

    public ZoomingMouseWheel() {
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable zooming with mouse wheel.
     * 
     * @param enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setSensitivity(Number)
     */
    public Number getSensitivity() {
        return sensitivity;
    }

    /**
     * Adjust the sensitivity of the mouse wheel. Higher values mean the zoom
     * changes more with each wheel turn. Lower values require more wheel turns
     * to zoom the same amount.
     * <p>
     * Sensitivity of mouse wheel or trackpad scrolling. 1 is no sensitivity,
     * while with 2, one mouse wheel delta will zoom in 50%.
     * 
     * @param sensitivity
     */
    public void setSensitivity(Number sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * @see #setType(Dimension)
     */
    public Dimension getType() {
        return type;
    }

    /**
     * Decides in what dimensions the user can zoom scrolling the wheel. In
     * charts not of type {@link Chart#setTimeline(Boolean)}, if not specified
     * here, it will inherit the type from {@link Zooming#getType()}. In
     * {@link Chart#setTimeline(Boolean)} charts, it defaults to
     * {@link Dimension#X}.
     * 
     * Note that particularly with mouse wheel in the y direction, the zoom is
     * affected by the default {@link YAxis#getStartOnTick()} and
     * {@link YAxis#getEndOnTick()} settings. In order to respect these
     * settings, the zoom level will adjust after the user has stopped zooming.
     * To prevent this, consider setting {@link YAxis#setStartOnTick(Boolean)}
     * and {@link YAxis#setEndOnTick(Boolean)} to false.
     * 
     * @param type
     */
    public void setType(Dimension type) {
        this.type = type;
    }
}

/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.layer;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;

/**
 * Abstract base class for all map layers
 */
public abstract class Layer extends AbstractConfigurationObject {
    private double opacity = 1;
    private boolean visible = true;
    private Integer zIndex;
    private Double minZoom;
    private Double maxZoom;
    private String background;

    /**
     * @return opacity of the layer
     */
    public double getOpacity() {
        return opacity;
    }

    /**
     * Sets the opacity of the layer. The value must lie between {@code 0} and
     * {@code 1}. Default value is {@code 1}.
     *
     * @param opacity
     *            new opacity of the layer
     */
    public void setOpacity(double opacity) {
        this.opacity = opacity;
        markAsDirty();
    }

    /**
     * @return whether the layer is visible or not
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visibility of the layer. Default value is {@code true}.
     *
     * @param visible
     *            new visibility of the layer
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        markAsDirty();
    }

    /**
     * @return the z-index of the layer, or null if not defined
     */
    public Integer getzIndex() {
        return zIndex;
    }

    /**
     * Sets the z-index of the layer. This allows to control in which order
     * layers are rendered. Layers with higher z-indexes are rendered above
     * layers with lower z-indexes. This value is {@code null} by default, which
     * means the order of the layers in the map determines the rendering order.
     *
     * @param zIndex
     *            the new z-index, or null to remove the z-index
     */
    public void setzIndex(Integer zIndex) {
        this.zIndex = zIndex;
        markAsDirty();
    }

    /**
     * @return the minimum zoom level at which this layer will be visible, or
     *         null if not defined
     */
    public Double getMinZoom() {
        return minZoom;
    }

    /**
     * Sets the minimum zoom level at which this layer will be visible.
     *
     * @param minZoom
     *            the new minimum zoom level, or null to remove it
     */
    public void setMinZoom(Double minZoom) {
        this.minZoom = minZoom;
        markAsDirty();
    }

    /**
     * @return the maximum zoom level at which this layer will be visible, or
     *         null if not defined
     */
    public Double getMaxZoom() {
        return maxZoom;
    }

    /**
     * Sets the maximum zoom level at which this layer will be visible.
     *
     * @param maxZoom
     *            the new maximum zoom level, or null to remove it
     */
    public void setMaxZoom(Double maxZoom) {
        this.maxZoom = maxZoom;
        markAsDirty();
    }

    /**
     * @return the background color of the layer as CSS color string, or null if
     *         not defined
     */
    public String getBackground() {
        return background;
    }

    /**
     * Sets the background color of the layer as CSS color string. All valid CSS
     * colors are supported, for example {@code "black"} or
     * {@code "rgb(0,0,0)"}.
     *
     * @param background
     *            the new background color of the layer, or null to remove it
     */
    public void setBackground(String background) {
        this.background = background;
        markAsDirty();
    }
}

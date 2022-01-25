package com.vaadin.flow.component.map.configuration;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright (C) 2022 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.util.Objects;

/**
 * View of the map, responsible for changing properties like center and zoom
 * level of the view
 */
public class View extends AbstractConfigurationObject {

    private Coordinate center;
    private float rotation;
    private float zoom;
    private ViewExtent extent;
    private final String projection;

    public View() {
        this("EPSG:3857");
    }

    public View(String projection) {
        this.center = new Coordinate(0, 0);
        this.rotation = 0;
        this.zoom = 0;
        this.extent = new ViewExtent(0, 0, 0, 0);
        this.projection = projection;
    }

    @Override
    public String getType() {
        return Constants.OL_VIEW;
    }

    /**
     * Gets center coordinates of the view
     *
     * @return center of the view
     */
    public Coordinate getCenter() {
        return center;
    }

    /**
     * Sets the center of the view in format specified by projection set on the
     * view, which defaults to {@code "EPSG:3857}
     *
     * @param center coordinates of the center of the view
     */
    public void setCenter(Coordinate center) {
        Objects.requireNonNull(center, "Center cannot be null");

        this.center = center;
        this.notifyChange();
    }

    /**
     * Get rotation of the view
     *
     * @return current rotation in radians
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation of the view, default to {@code 0}
     *
     * @param rotation the rotation in radians format
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
        this.notifyChange();
    }

    /**
     * Gets zoom level of the view
     *
     * @return the zoom level of the view
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Sets the zoom level of the view, default to {@code 0}
     *
     * @param zoom the zoom level in decimal format
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.notifyChange();
    }

    /**
     * Gets the projection of the view, default to {@code "EPSG:3857"}
     *
     * @return the projection of the view
     */
    public String getProjection() {
        return projection;
    }

    /**
     * Gets the coordinates of the view's extent, default value is {@code 0} for all coordinates
     *
     * @return the coordinates of the view's extent
     */
    public ViewExtent getExtent() {
        return extent;
    }

    /**
     * Sets the coordinate of current view's extent in EPSG:3857 format
     *
     * @param extent the extent of the view in EPSG:3857 format
     */
    public void setExtent(ViewExtent extent) {
        this.extent = extent;
        this.notifyChange();
    }
}

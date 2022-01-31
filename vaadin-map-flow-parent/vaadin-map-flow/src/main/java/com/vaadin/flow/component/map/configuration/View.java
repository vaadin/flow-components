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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * View of the map, responsible for changing properties like center and zoom
 * level of the view
 */
public class View extends AbstractConfigurationObject {

    private Coordinate center;
    private float rotation;
    private float zoom;
    private Extent extent;
    private final String projection;

    public View() {
        this("EPSG:3857");
    }

    public View(String projection) {
        this.center = new Coordinate(0, 0);
        this.rotation = 0;
        this.zoom = 0;
        this.extent = new Extent(0, 0, 0, 0);
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
     * @param center
     *            coordinates of the center of the view
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
     * @param rotation
     *            the rotation in radians format
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
     * @param zoom
     *            the zoom level in decimal format
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
     * Gets the extent of the view's currently visible area, default value is
     * {@code 0} for all coordinates.
     * <p>
     * The extent is calculated on the client-side and will only be available
     * after the first view change event.
     * 
     * @return the coordinates of the view's extent
     */
    @JsonIgnore
    public Extent getExtent() {
        return extent;
    }

    /**
     * Updates internal state of view to the latest values received from client
     *
     * @param center
     *            the updated center coordinates
     * @param rotation
     *            the updated rotation
     * @param zoom
     *            the updated zoom level
     * @param extent
     *            the updated extent
     */
    public void updateInternalViewState(Coordinate center, float rotation,
            float zoom, Extent extent) {
        update(() -> {
            this.center = center;
            this.rotation = rotation;
            this.zoom = zoom;
            this.extent = extent;
        }, false);
    }
}

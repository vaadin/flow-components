package com.vaadin.flow.component.map.configuration;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
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
import com.vaadin.flow.component.map.configuration.source.Source;

import java.util.Objects;

/**
 * Represents a map's viewport, responsible for changing properties like center
 * and zoom level
 */
public class View extends AbstractConfigurationObject {

    private Coordinate center;
    private float rotation;
    private float zoom;
    private Extent extent;
    private final String projection;

    /**
     * Constructs a new view using {@code EPSG:3857} / Web Mercator Sphere
     * coordinate projection by default. Unless you are using a custom map
     * service that uses a different projection, this is what you want.
     */
    public View() {
        this(Projection.EPSG_3857.stringValue());
    }

    /**
     * Constructs a new view using a custom coordinate projection. A custom
     * projection is only necessary when using a map service and corresponding
     * {@link Source} that uses a projection other than {@code EPSG:3857} / Web
     * Mercator Sphere projection.
     *
     * @param projection
     *            the custom coordinate projection to use
     */
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
     * view, which defaults to {@code EPSG:3857}
     *
     * @param center
     *            coordinates of the center of the view
     */
    public void setCenter(Coordinate center) {
        Objects.requireNonNull(center, "Center cannot be null");

        this.center = center;
        markAsDirty();
    }

    /**
     * Get rotation of the view, defaults to {@code 0}
     *
     * @return current rotation in radians
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation of the view in radians
     *
     * @param rotation
     *            the rotation in radians format
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
        markAsDirty();
    }

    /**
     * Gets zoom level of the view, defaults to {@code 0}
     *
     * @return current zoom level
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Sets the zoom level of the view. The zoom level is a decimal value that
     * starts at {@code 0} as the most zoomed-out level, and then continually
     * increases to zoom further in. By default, the maximum zoom level is
     * currently restricted to {@code 28}. In practical terms, the level of
     * detail of the map data that a map service provides determines how useful
     * higher zoom levels are.
     *
     * @param zoom
     *            new zoom level
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        markAsDirty();
    }

    /**
     * Gets the projection of the view, which defaults to {@code EPSG:3857} /
     * Web Mercator Sphere projection
     *
     * @return the projection of the view
     */
    public String getProjection() {
        return projection;
    }

    /**
     * Gets the extent (or bounding box) of the view's currently visible area.
     * Can be used to check whether a specific coordinate is within the
     * viewport.
     * <p>
     * <b>NOTE:</b> The extent is calculated on the client-side and will only be
     * available after the first view change event.
     *
     * @return the coordinates of the view's extent
     */
    @JsonIgnore
    public Extent getExtent() {
        return extent;
    }

    /**
     * Updates internal state of view to the latest values received from client.
     * <p>
     * For internal use only.
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

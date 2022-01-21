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
 * A class representing a view of the map, responsible for changing center, rotation ...
 * of the view
 */
public class View extends AbstractConfigurationObject {


    private Coordinate center;
    private float rotation;
    private float zoom;
    private String projection;

    public View() {
        this.center = new Coordinate(0, 0);
        this.rotation = 0;
        this.zoom = 0;
        this.projection = "EPSG:3857";
    }

    public View(String projection) {
        this();
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
     * Sets the center of the view in format specified by projection set on the view which defaults to EPSG:3857
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
     * Sets the rotation of the view, default to zero
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
     * Sets the zoom level of the view, default to 0
     * 
     * @param zoom
     *            the zoom level in decimal format
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.notifyChange();
    }

    /**
     * Gets the projection of the view, default to EPSG:3857
     * @return
     */
    public String getProjection() {
        return projection;
    }
}

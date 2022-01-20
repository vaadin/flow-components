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
 * View is responsible for centering of the map, rotation or zoom level
 */
public class View extends AbstractConfigurationObject {

    /**
     * Coordinates of the center of the map in epsg:3857 (Web Mercator) format
     */
    private Coordinate center;
    /**
     * Rotation of map in radian
     */
    private float rotation;
    /**
     * Zoom level of the map
     */
    private float zoom;

    public View() {
        this.center = new Coordinate(0, 0);
        this.rotation = 0;
        this.zoom = 0;
    }

    /**
     * This method is used for internal synchronization of map configuration.
     */
    @Override
    public String getType() {
        return Constants.OL_VIEW;
    }

    /**
     * Gets center coordinates of the map in epsg:3857 format
     * @return center of the map in epsg:3857 format
     */
    public Coordinate getCenter() {
        return center;
    }

    /**
     * Sets the center of the map
     * @param center coordinates of the center in epsg:3857 format
     */
    public void setCenter(Coordinate center) {
        Objects.requireNonNull(center, "Center cannot be null");

        this.center = center;
        this.notifyChange();
    }

    /**
     * Get rotation of the map
     * @return current rotation in radians
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation of the map
     * @param rotation the rotation in radians format
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
        this.notifyChange();
    }

    /**
     * Gets zoom level of the map
     * @return the zoom level of the map
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Sets the zoom level of the map
     * @param zoom the zoom level in decimal format
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.notifyChange();
    }
}

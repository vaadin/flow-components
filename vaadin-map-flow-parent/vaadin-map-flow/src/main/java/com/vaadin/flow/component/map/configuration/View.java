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

public class View extends AbstractConfigurationObject {

    private Coordinate center;
    private float rotation;
    private float zoom;

    public View() {
        this.center = new Coordinate(0, 0);
        this.rotation = 0;
        this.zoom = 0;
    }

    @Override
    public String getType() {
        return Constants.OL_VIEW;
    }

    public Coordinate getCenter() {
        return center;
    }

    public void setCenter(Coordinate center) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        this.center = center;
        this.notifyChange();
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        this.notifyChange();
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.notifyChange();
    }
}

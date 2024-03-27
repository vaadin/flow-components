/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.MapBase;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Extent;
import com.vaadin.flow.component.map.configuration.View;
import elemental.json.JsonArray;

/**
 * Fired when viewport movement has ended.
 */
@DomEvent("map-view-moveend")
public class MapViewMoveEndEvent extends ComponentEvent<MapBase> {

    private final float rotation;
    private final float zoom;
    private final Coordinate center;
    private final Extent extent;

    public MapViewMoveEndEvent(MapBase source, boolean fromClient,
            @EventData("event.detail.rotation") double rotation,
            @EventData("event.detail.zoom") double zoom,
            @EventData("event.detail.center") JsonArray center,
            @EventData("event.detail.extent") JsonArray extent) {
        super(source, fromClient);
        this.rotation = (float) rotation;
        this.zoom = (float) zoom;
        this.center = MapEventUtil.getCoordinate(center);
        this.extent = MapEventUtil.getExtent(extent);
    }

    /**
     * Gets the {@link View}'s updated rotation
     *
     * @return updated rotation in radians
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Gets the {@link View}'s updated zoom level
     *
     * @return updated zoom level
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Gets the {@link View}'s updated center coordinates
     *
     * @return updated center coordinates
     */
    public Coordinate getCenter() {
        return center;
    }

    /**
     * Gets the updated extent (or bounding box) of the {@link View}'s currently
     * visible area. Can be used to check whether a specific coordinate is
     * within the viewport.
     *
     * @return updated extent
     */
    public Extent getExtent() {
        return extent;
    }
}

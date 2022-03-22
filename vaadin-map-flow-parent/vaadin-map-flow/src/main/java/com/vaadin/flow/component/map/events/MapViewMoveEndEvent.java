package com.vaadin.flow.component.map.events;

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
        this.center = new Coordinate(center.getNumber(0), center.getNumber(1));
        this.extent = new Extent(extent.getNumber(0), extent.getNumber(1),
                extent.getNumber(2), extent.getNumber(3));
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

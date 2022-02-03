package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.MapBase;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Extent;
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

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source
     *            the source component
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     */
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
     * Gets the view's updated rotation after map's "moveend" event.
     *
     * @return latest rotation in radians
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Gets the view's updated zoom level after map's "moveend" event.
     *
     * @return latest zoom level
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Gets the view's updated center coordinates after map's "moveend" event.
     *
     * @return latest center coordinates
     */
    public Coordinate getCenter() {
        return center;
    }

    /**
     * Gets the view's updated extent after map's "moveend" event.
     *
     * @return latest view
     */
    public Extent getExtent() {
        return extent;
    }
}

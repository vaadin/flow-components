package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.ViewExtent;
import elemental.json.JsonArray;

/**
 * Fired when map movement has ended.
 */
@DomEvent("map-view-moveend")
public class MapViewMoveEndEvent extends ComponentEvent<Map> {

    private final float rotation;
    private final float zoom;
    private final Coordinate center;
    private final ViewExtent viewExtent;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public MapViewMoveEndEvent(Map source, boolean fromClient, @EventData("event.detail.rotation") double rotation,
                               @EventData("event.detail.zoom") double zoom,
                               @EventData("event.detail.center") JsonArray center,
                               @EventData("event.detail.extent") JsonArray extent) {
        super(source, fromClient);
        this.rotation = (float) rotation;
        this.zoom = (float) zoom;
        this.center = new Coordinate(center.getNumber(0), center.getNumber(1));
        this.viewExtent = new ViewExtent(extent.getNumber(0), extent.getNumber(1), extent.getNumber(2), extent.getNumber(3));
    }

    /**
     * Gets the view's updated rotation coordinates after map's "moveend" event.
     *
     * @return latest rotation in radians
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Gets the view's updated zoom level coordinates after map's "moveend" event.
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
    public ViewExtent getViewExtent() {
        return viewExtent;
    }
}



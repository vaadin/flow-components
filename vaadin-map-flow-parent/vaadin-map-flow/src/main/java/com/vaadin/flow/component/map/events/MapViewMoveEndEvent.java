package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import elemental.json.JsonArray;

/**
 * Fired when map movement has ended.
 */
@DomEvent("map-view-moveend")
public class MapViewMoveEndEvent extends ComponentEvent<Map> {

    // todo(hadi): zoom, center, extent
    private final float rotation;


    private final float zoom;

    private Coordinate center;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public MapViewMoveEndEvent(Map source, boolean fromClient, @EventData("event.detail.rotation") double rotation,
                               @EventData("event.detail.zoom") double zoom,
                               @EventData("event.detail.center") JsonArray center) {
        super(source, fromClient);
        this.rotation = (float) rotation;
        this.zoom = (float) zoom;
        this.center = new Coordinate(center.getNumber(0), center.getNumber(1));
    }

    public float getRotation() {
        return rotation;
    }


    public float getZoom() {
        return zoom;
    }

    public Coordinate getCenter() {
        return center;
    }
}



package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.MapBase;
import com.vaadin.flow.component.map.configuration.Coordinate;
import elemental.json.JsonArray;

/**
 * Representing OpenLayers' @code{click} event
 */
@DomEvent("map-click")
public class MapClickEvent extends ComponentEvent<MapBase> {

    private final Coordinate coordinate;
    private final MouseEventDetails details;

    public MapClickEvent(MapBase source, boolean fromClient,
            @EventData("event.detail.coordinate") JsonArray coordinate,
            @EventData("event.detail.originalEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button) {
        super(source, fromClient);

        this.coordinate = new Coordinate(coordinate.getNumber(0),
                coordinate.getNumber(1));

        details = new MouseEventDetails();

        details.setAbsoluteX(pageX);
        details.setAbsoluteY(pageY);
        details.setButton(MouseEventDetails.MouseButton.of(button));
        details.setAltKey(altKey);
        details.setCtrlKey(ctrlKey);
        details.setMetaKey(metaKey);
        details.setShiftKey(shiftKey);
    }

    /**
     * Gets the coordinate of the click on viewport
     *
     * @return coordinate of the click, in the view's projection.
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * Gets the click's mouse event details.
     *
     * @return mouse event details
     */
    public MouseEventDetails getMouseDetails() {
        return details;
    }
}

/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.configuration.Coordinate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * Provides data for when an element is dropped onto the map from an external
 * drag source. This event provides the geographic coordinates (latitude,
 * longitude) of the drop location on the map.
 */
@DomEvent("map-drop")
public class MapDropEvent
        extends ComponentEvent<com.vaadin.flow.component.map.Map> {

    private final Coordinate coordinate;
    private final Map<String, String> data;

    public MapDropEvent(com.vaadin.flow.component.map.Map source,
            boolean fromClient,
            @EventData("event.detail.coordinate") ArrayNode coordinate,
            @EventData("event.detail.dragData") ArrayNode dragData) {
        super(source, fromClient);

        this.coordinate = MapEventUtil.getCoordinate(coordinate);

        data = new HashMap<>();
        if (dragData != null) {
            IntStream.range(0, dragData.size()).forEach(i -> {
                JsonNode jsonData = dragData.get(i);
                data.put(jsonData.get("type").asString(),
                        jsonData.get("data").asString());
            });
        }
    }

    /**
     * Gets the geographic coordinates (latitude, longitude) where the drop
     * occurred on the map.
     *
     * @return the coordinates of the drop location
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * Get data from the {@code DataTransfer} object.
     *
     * @param type
     *            data format
     * @return data for the given format, {@code Optional.empty()} if it does
     *         not exist
     */
    public Optional<String> getDataTransferData(String type) {
        return Optional.ofNullable(data.get(type));
    }

    /**
     * Gets the data transfer text
     *
     * @return the data transfer text
     */
    public String getDataTransferText() {
        var text = data.get("text");
        if (text == null) {
            text = data.get("Text");
        }
        if (text == null) {
            text = data.get("text/plain");
        }
        return text;
    }

    /**
     * Get all the transfer data
     *
     * @return data transfer
     */
    public Map<String, String> getDataTransferData() {
        return Collections.unmodifiableMap(data);
    }
}

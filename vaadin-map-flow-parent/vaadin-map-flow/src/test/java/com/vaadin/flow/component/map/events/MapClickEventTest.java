/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.map.Map;

import elemental.json.Json;
import elemental.json.JsonArray;

public class MapClickEventTest {

    @Test
    public void create() {
        Map map = new Map();
        JsonArray coordinates = Json.createArray();
        coordinates.set(0, 10);
        coordinates.set(1, 20);
        JsonArray featureIds = Json.createArray();
        JsonArray layerIds = Json.createArray();

        MapClickEvent event = new MapClickEvent(map, true, coordinates,
                featureIds, layerIds, 0, 0, false, false, false, false, 0);

        Assert.assertNotNull(event.getCoordinate());
        Assert.assertEquals(10, event.getCoordinate().getX(), 0);
        Assert.assertEquals(20, event.getCoordinate().getY(), 0);
    }

    @Test
    public void createFromInvalidCoordinates_usesFallback() {
        Map map = new Map();
        JsonArray coordinates = Json.createArray();
        coordinates.set(0, Json.createNull());
        coordinates.set(1, Json.createNull());
        JsonArray featureIds = Json.createArray();
        JsonArray layerIds = Json.createArray();

        MapClickEvent event = new MapClickEvent(map, true, coordinates,
                featureIds, layerIds, 0, 0, false, false, false, false, 0);

        Assert.assertNotNull(event.getCoordinate());
        Assert.assertEquals(0, event.getCoordinate().getX(), 0);
        Assert.assertEquals(0, event.getCoordinate().getY(), 0);
    }
}

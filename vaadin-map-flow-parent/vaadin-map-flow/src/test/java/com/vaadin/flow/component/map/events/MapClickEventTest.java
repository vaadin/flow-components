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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.internal.JacksonUtils;

public class MapClickEventTest {

    @Test
    public void create() {
        Map map = new Map();
        ArrayNode coordinates = JacksonUtils.createArrayNode();
        coordinates.add(10);
        coordinates.add(20);
        ArrayNode featureIds = JacksonUtils.createArrayNode();
        ArrayNode layerIds = JacksonUtils.createArrayNode();

        MapClickEvent event = new MapClickEvent(map, true, coordinates,
                featureIds, layerIds, 0, 0, false, false, false, false, 0);

        Assert.assertNotNull(event.getCoordinate());
        Assert.assertEquals(10, event.getCoordinate().getX(), 0);
        Assert.assertEquals(20, event.getCoordinate().getY(), 0);
    }

    @Test
    public void createFromInvalidCoordinates_usesFallback() {
        Map map = new Map();
        ArrayNode coordinates = JacksonUtils.createArrayNode();
        coordinates.add(JacksonUtils.nullNode());
        coordinates.add(JacksonUtils.nullNode());
        ArrayNode featureIds = JacksonUtils.createArrayNode();
        ArrayNode layerIds = JacksonUtils.createArrayNode();

        MapClickEvent event = new MapClickEvent(map, true, coordinates,
                featureIds, layerIds, 0, 0, false, false, false, false, 0);

        Assert.assertNotNull(event.getCoordinate());
        Assert.assertEquals(0, event.getCoordinate().getX(), 0);
        Assert.assertEquals(0, event.getCoordinate().getY(), 0);
    }
}

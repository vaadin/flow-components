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
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;

public class MapViewMoveEndEventTest {

    @Test
    public void create() {
        Map map = new Map();
        ArrayNode coordinates = JacksonUtils.createArrayNode();
        coordinates.add(10);
        coordinates.add(20);
        ArrayNode extent = JacksonUtils.createArrayNode();
        extent.add(30);
        extent.add(40);
        extent.add(50);
        extent.add(60);

        MapViewMoveEndEvent event = new MapViewMoveEndEvent(map, true, 0, 0,
                coordinates, extent);

        Assert.assertNotNull(event.getCenter());
        Assert.assertEquals(10, event.getCenter().getX(), 0);
        Assert.assertEquals(20, event.getCenter().getY(), 0);
        Assert.assertNotNull(event.getExtent());
        Assert.assertEquals(30, event.getExtent().getMinX(), 0);
        Assert.assertEquals(40, event.getExtent().getMinY(), 0);
        Assert.assertEquals(50, event.getExtent().getMaxX(), 0);
        Assert.assertEquals(60, event.getExtent().getMaxY(), 0);
    }

    @Test
    public void createFromInvalidCoordinates_usesFallback() {
        Map map = new Map();
        ArrayNode coordinates = JacksonUtils.createArrayNode();
        coordinates.add(JacksonUtils.nullNode());
        coordinates.add(JacksonUtils.nullNode());
        ArrayNode extent = JacksonUtils.createArrayNode();
        extent.add(JacksonUtils.nullNode());
        extent.add(JacksonUtils.nullNode());
        extent.add(JacksonUtils.nullNode());
        extent.add(JacksonUtils.nullNode());

        MapViewMoveEndEvent event = new MapViewMoveEndEvent(map, true, 0, 0,
                coordinates, extent);

        Assert.assertNotNull(event.getCenter());
        Assert.assertEquals(0, event.getCenter().getX(), 0);
        Assert.assertEquals(0, event.getCenter().getY(), 0);
        Assert.assertNotNull(event.getExtent());
        Assert.assertEquals(0, event.getExtent().getMinX(), 0);
        Assert.assertEquals(0, event.getExtent().getMinY(), 0);
        Assert.assertEquals(0, event.getExtent().getMaxX(), 0);
        Assert.assertEquals(0, event.getExtent().getMaxY(), 0);
    }
}

package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.map.Map;
import elemental.json.Json;
import elemental.json.JsonArray;
import org.junit.Assert;
import org.junit.Test;

public class MapViewMoveEndEventTest {

    @Test
    public void create() {
        Map map = new Map();
        JsonArray coordinates = Json.createArray();
        coordinates.set(0, 10);
        coordinates.set(1, 20);
        JsonArray extent = Json.createArray();
        extent.set(0, 30);
        extent.set(1, 40);
        extent.set(2, 50);
        extent.set(3, 60);

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
        JsonArray coordinates = Json.createArray();
        coordinates.set(0, Json.createNull());
        coordinates.set(1, Json.createNull());
        JsonArray extent = Json.createArray();
        extent.set(0, Json.createNull());
        extent.set(1, Json.createNull());
        extent.set(2, Json.createNull());
        extent.set(3, Json.createNull());

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

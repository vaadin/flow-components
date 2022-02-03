package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/map-events")
public class MapEventsIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void changeViewPort_viewStateUpdated() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement stateTextDiv = $("div").id("view-state");

        // We are simulating user changing the view port of the map
        map.evaluateOLExpression(
                "map.getView().setCenter([4849385.650796606, 5487570.011434158]);");
        map.evaluateOLExpression("map.getView().setRotation(5);");
        map.evaluateOLExpression("map.getView().setZoom(6)");

        String[] parts = stateTextDiv.getText().split(";");
        double centerX = Double.parseDouble(parts[0]);
        double centerY = Double.parseDouble(parts[1]);
        float rotation = Float.parseFloat(parts[2]);
        float zoom = Float.parseFloat(parts[3]);

        Assert.assertEquals(4849385.650796606, centerX, 0.1);
        Assert.assertEquals(5487570.011434158, centerY, 0.1);
        Assert.assertEquals(5.0, rotation, 0.01);
        Assert.assertEquals(6.0, zoom, 0.01);
    }

    @Test
    public void changeViewPort_correctEventDataReceived() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement eventDataDiv = $("div").id("event-data");

        // We are simulating user changing the view port of the map
        map.evaluateOLExpression(
                "map.getView().setCenter([4849385.650796606, 5487570.011434158]);");
        map.evaluateOLExpression("map.getView().setRotation(5);");
        map.evaluateOLExpression("map.getView().setZoom(6)");

        String[] parts = eventDataDiv.getText().split(";");
        double centerX = Double.parseDouble(parts[0]);
        double centerY = Double.parseDouble(parts[1]);
        float rotation = Float.parseFloat(parts[2]);
        float zoom = Float.parseFloat(parts[3]);

        Assert.assertEquals(4849385.650796606, centerX, 0.1);
        Assert.assertEquals(5487570.011434158, centerY, 0.1);
        Assert.assertEquals(5.0, rotation, 0.01);
        Assert.assertEquals(6.0, zoom, 0.01);
    }

    @Test
    public void mapClick_correctEventDataReceived() {
        MapElement map = $(MapElement.class).waitForFirst();
        TestBenchElement eventDataDiv = $("div").id("event-data");

        map.clickAtCoordinates(-1956787.9241005122, 1956787.9241005122);

        String[] parts = eventDataDiv.getText().split(";");

        double xCoordinate = Double.parseDouble(parts[0]);
        double yCoordinate = Double.parseDouble(parts[1]);
        double xPixel = Double.parseDouble(parts[2]);
        double yPixel = Double.parseDouble(parts[3]);

        Assert.assertEquals(100, xPixel, 0.1);
        Assert.assertEquals(100, yPixel, 0.1);
        Assert.assertEquals(-1956787.9241005122, xCoordinate, 0.00000001);
        Assert.assertEquals(1956787.9241005122, yCoordinate, 0.00000001);
    }

}

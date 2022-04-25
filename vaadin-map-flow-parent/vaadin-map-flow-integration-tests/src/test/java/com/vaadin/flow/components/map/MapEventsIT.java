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
    private MapElement map;
    private TestBenchElement viewStateDiv;
    private TestBenchElement eventDataDiv;
    private TestBenchElement addMoveEndListener;
    private TestBenchElement addClickListener;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        viewStateDiv = $("div").id("view-state");
        eventDataDiv = $("div").id("event-data");
        addMoveEndListener = $("button").id("add-move-end-listener");
        addClickListener = $("button").id("add-click-listener");
    }

    @Test
    public void changeViewPort_viewStateUpdated() {
        addMoveEndListener.click();

        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.ViewReference view = mapReference.getView();
        // Simulate user changing the view port of the map
        view.setCenter(new MapElement.Coordinate(4849385.650796606,
                5487570.011434158));
        view.setZoom(6);
        view.setRotation(5);

        String[] parts = viewStateDiv.getText().split(";");
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
        addMoveEndListener.click();

        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.ViewReference view = mapReference.getView();
        // Simulate user changing the view port of the map
        view.setCenter(new MapElement.Coordinate(4849385.650796606,
                5487570.011434158));
        view.setZoom(6);
        view.setRotation(5);

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
    public void mapClick_correctCoordinatesAndPosition() {
        addClickListener.click();

        map.clickAtCoordinates(-1956787.9241005122, 1956787.9241005122);

        // Click events are delayed, so wait until event data shows up
        waitUntilHasText(eventDataDiv);

        String[] eventParts = eventDataDiv.getText()
                .split(System.lineSeparator());
        String[] coordinateParts = eventParts[0].split(";");
        String[] pixelPositionParts = eventParts[1].split(";");

        double xCoordinate = Double.parseDouble(coordinateParts[0]);
        double yCoordinate = Double.parseDouble(coordinateParts[1]);
        double xPixel = Double.parseDouble(pixelPositionParts[0]);
        double yPixel = Double.parseDouble(pixelPositionParts[1]);

        Assert.assertEquals(100, xPixel, 0.1);
        Assert.assertEquals(100, yPixel, 0.1);
        Assert.assertEquals(-1956787.9241005122, xCoordinate, 0.00000001);
        Assert.assertEquals(1956787.9241005122, yCoordinate, 0.00000001);
    }

    @Test
    public void mapClick_containsAllFeaturesAtLocation() {
        addClickListener.click();
        // Click on location with markers
        map.clickAtCoordinates(2000000, 0);

        // Click events are delayed, so wait until event data shows up
        waitUntilHasText(eventDataDiv);

        String[] eventParts = eventDataDiv.getText()
                .split(System.lineSeparator());
        String[] featureIds = eventParts.length > 2 ? eventParts[2].split(";")
                : new String[] {};

        // Should have 3 features
        Assert.assertEquals(3, featureIds.length);
        // In the order that they are displayed
        Assert.assertEquals("overlapping-marker-feature-3", featureIds[0]);
        Assert.assertEquals("overlapping-marker-feature-2", featureIds[1]);
        Assert.assertEquals("overlapping-marker-feature-1", featureIds[2]);
    }

    private void waitUntilHasText(TestBenchElement element) {
        waitUntil(driver -> element.getText() != null
                && !element.getText().isEmpty());
    }
}

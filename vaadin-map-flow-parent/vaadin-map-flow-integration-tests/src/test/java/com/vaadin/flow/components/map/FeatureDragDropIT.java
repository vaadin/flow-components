package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-map/feature-drag-drop")
public class FeatureDragDropIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement eventFeatureIdOutput;
    private TestBenchElement eventCoordinatesOutput;
    private TestBenchElement eventStartCoordinatesOutput;
    private TestBenchElement markerCoordinatesOutput;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        eventFeatureIdOutput = $(TestBenchElement.class).id("event-feature-id");
        eventCoordinatesOutput = $(TestBenchElement.class)
                .id("event-coordinates");
        eventStartCoordinatesOutput = $(TestBenchElement.class)
                .id("event-start-coordinates");
        markerCoordinatesOutput = $(TestBenchElement.class)
                .id("marker-coordinates");
    }

    @Test
    public void dragMarker_correctEventDataReceived() {
        // Drag and drop Nairobi marker to Cape Town
        MapElement.Coordinate nairobiCoordinates = new MapElement.Coordinate(
                36.818104, -1.302283);
        MapElement.Coordinate capeTownCoordinates = new MapElement.Coordinate(
                18.417396, -33.928992);

        dragAndDrop(nairobiCoordinates, capeTownCoordinates);
        getCommandExecutor().waitForVaadin();

        // Verify correct marker was dragged
        Assert.assertEquals("nairobi-feature", eventFeatureIdOutput.getText());

        // Verify drag start coordinate
        // Using a large delta here to compensate for pixel -> coordinate
        // conversion. For the most part, we just want to ensure that the
        // underlying Openlayers drag and drop interaction was triggered.
        MapElement.Coordinate eventStartCoordinates = parseCoordinates(
                eventStartCoordinatesOutput);
        Assert.assertEquals(nairobiCoordinates.getX(),
                eventStartCoordinates.getX(), 1);
        Assert.assertEquals(nairobiCoordinates.getY(),
                eventStartCoordinates.getY(), 1);

        // Verify drag end coordinate
        // Using a large delta here to compensate for pixel -> coordinate
        // conversion. For the most part, we just want to ensure that the
        // underlying Openlayers drag and drop interaction was triggered.
        MapElement.Coordinate eventCoordinates = parseCoordinates(
                eventCoordinatesOutput);
        Assert.assertEquals(capeTownCoordinates.getX(), eventCoordinates.getX(),
                1);
        Assert.assertEquals(capeTownCoordinates.getY(), eventCoordinates.getY(),
                1);

        // Verify marker instance coordinates have been updated
        MapElement.Coordinate markerCoordinates = parseCoordinates(
                markerCoordinatesOutput);
        Assert.assertEquals(capeTownCoordinates.getX(),
                markerCoordinates.getX(), 1);
        Assert.assertEquals(capeTownCoordinates.getY(),
                markerCoordinates.getY(), 1);
    }

    @Test
    public void dragNonDraggableMarker_noDropEvent() {
        // Drag from (non-draggable) Tunis marker to Cape Town
        MapElement.Coordinate tunisCoordinates = new MapElement.Coordinate(
                10.189819, 36.810109);
        MapElement.Coordinate capeTownCoordinates = new MapElement.Coordinate(
                18.417396, -33.928992);

        dragAndDrop(tunisCoordinates, capeTownCoordinates);
        getCommandExecutor().waitForVaadin();

        // Should not have received drag drop event
        Assert.assertEquals("", eventFeatureIdOutput.getText());
    }

    private void dragAndDrop(MapElement.Coordinate from,
            MapElement.Coordinate to) {
        MapElement.PixelCoordinate dragStartCoordinates = map
                .getPixelCoordinates(from.getX(), from.getY(), true);

        MapElement.PixelCoordinate dragEndCoordinates = map
                .getPixelCoordinates(to.getX(), to.getY(), true);

        int dragDeltaX = dragEndCoordinates.getX()
                - dragStartCoordinates.getX();
        int dragDeltaY = dragEndCoordinates.getY()
                - dragStartCoordinates.getY();

        new Actions(getDriver())
                .moveToElement(map, dragStartCoordinates.getX(),
                        dragStartCoordinates.getY())
                .clickAndHold().moveByOffset(dragDeltaX, dragDeltaY).release()
                .build().perform();
    }

    private static MapElement.Coordinate parseCoordinates(
            TestBenchElement outputElement) {
        String[] parts = outputElement.getText().split(";");
        return new MapElement.Coordinate(Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]));
    }
}

package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/define-custom-projection")
public class DefineCustomProjectionIT extends AbstractComponentIT {

    private MapElement map;
    private TestBenchElement eventDataDiv;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        eventDataDiv = $(TestBenchElement.class).id("event-data");
    }

    @Test
    public void initWithEpsg3067UserProjection_setViewportUsingEpsg3067Coordinates_correctViewport() {
        // Verify correct viewport by checking that the Turku map tile is
        // visible
        // Due to reprojecting the view, the map tile for zoom level 9 is
        // loaded, rather than for zoom level then as configured in the test
        // setup
        // This is the map tile containing Turku for zoom level 9:
        // https://b.tile.openstreetmap.org/9/287/147.png
        MapElement.LayerReference layer = map.getMapReference().getLayers()
                .getLayer("background-layer");
        MapElement.XyzSourceReference source = layer.getSource().asXyzSource();
        waitUntilMapTileLoaded(source, 9, 287, 147);
    }

    @Test
    public void initWithEpsg3067UserProjection_moveViewportClientSide_receivedEventWithEpsg3067Coordinates() {
        // Simulate user changing the viewport of the map to Helsinki
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.ViewReference view = mapReference.getView();
        view.setCenter(new MapElement.Coordinate(385725.63, 6671616.89));

        // Double-check Helsinki map tile is visible
        // (https://b.tile.openstreetmap.org/9/291/148.png)
        MapElement.LayerReference layer = map.getMapReference().getLayers()
                .getLayer("background-layer");
        MapElement.XyzSourceReference source = layer.getSource().asXyzSource();
        waitUntilMapTileLoaded(source, 9, 291, 148);

        // Check coordinates received server-side
        String[] parts = eventDataDiv.getText().split(";");
        double centerX = Double.parseDouble(parts[0]);
        double centerY = Double.parseDouble(parts[1]);

        Assert.assertEquals(385725.63, centerX, 0.001);
        Assert.assertEquals(6671616.89, centerY, 0.001);
    }

    private void waitUntilMapTileLoaded(MapElement.XyzSourceReference source,
            int z, int x, int y) {
        waitUntil(driver -> source.isTileLoaded(z, x, y));
    }
}

package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/change-user-projection")
public class ChangeUserProjectionIT extends AbstractComponentIT {

    private MapElement map;
    private TestBenchElement eventDataDiv;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        eventDataDiv = $(TestBenchElement.class).id("event-data");
    }

    @Test
    public void initWithEpsg3857UserProjection_setViewportUsingEpsg3857Coordinates_correctViewport() {
        // Verify correct viewport by checking that the Turku map tile for the
        // configured zoom level is visible
        // This is the map tile containing Turku for zoom level 10:
        // https://c.tile.openstreetmap.org/10/575/294.png
        MapElement.LayerReference layer = map.getMapReference().getLayers()
                .getLayer("background-layer");
        MapElement.XyzSourceReference source = layer.getSource().asXyzSource();
        waitUntilMapTileLoaded(source, 10, 575, 294);
    }

    @Test
    public void initWithEpsg3857UserProjection_moveViewportClientSide_receivedEventWithEpsg3857Coordinates() {
        // Simulate user changing the viewport of the map to Berlin
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.ViewReference view = mapReference.getView();
        view.setCenter(
                new MapElement.Coordinate(1491592.169957, 6893740.925498));

        // Double-check Berlin map tile is visible
        // (https://a.tile.openstreetmap.org/10/550/335.png)
        MapElement.LayerReference layer = map.getMapReference().getLayers()
                .getLayer("background-layer");
        MapElement.XyzSourceReference source = layer.getSource().asXyzSource();
        waitUntilMapTileLoaded(source, 10, 550, 335);

        // Check coordinates received server-side
        String[] parts = eventDataDiv.getText().split(";");
        double centerX = Double.parseDouble(parts[0]);
        double centerY = Double.parseDouble(parts[1]);

        Assert.assertEquals(1491592.169957, centerX, 0.001);
        Assert.assertEquals(6893740.925498, centerY, 0.001);
    }

    private void waitUntilMapTileLoaded(MapElement.XyzSourceReference source,
            int z, int x, int y) {
        waitUntil(driver -> source.isTileLoaded(z, x, y));
    }
}

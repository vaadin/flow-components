/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.components.map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

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
        // Due to reprojecting the view, the map tile keys do not match with the
        // tiles actually loaded from OSM. The key below was picked manually
        // by checking the tiles in the tile cache loaded in the browser.
        MapElement.LayerReference layer = map.getMapReference().getLayers()
                .getLayer("background-layer");
        waitUntilMapTileLoaded(layer, 10, 519, 339);
    }

    @Test
    public void initWithEpsg3067UserProjection_moveViewportClientSide_receivedEventWithEpsg3067Coordinates() {
        // Simulate user changing the viewport of the map to Helsinki
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.ViewReference view = mapReference.getView();
        view.setCenter(new MapElement.Coordinate(385725.63, 6671616.89));

        // Double-check Helsinki map tile is visible
        // Due to reprojecting the view, the map tile keys do not match with the
        // tiles actually loaded from OSM. The key below was picked manually
        // by checking the tiles in the tile cache loaded in the browser.
        // Also verified that the same tile is not loaded in the test above.
        MapElement.LayerReference layer = map.getMapReference().getLayers()
                .getLayer("background-layer");
        waitUntilMapTileLoaded(layer, 10, 523, 342);

        // Check coordinates received server-side
        String[] parts = eventDataDiv.getText().split(";");
        double centerX = Double.parseDouble(parts[0]);
        double centerY = Double.parseDouble(parts[1]);

        Assert.assertEquals(385725.63, centerX, 0.001);
        Assert.assertEquals(6671616.89, centerY, 0.001);
    }

    private void waitUntilMapTileLoaded(MapElement.LayerReference layer, int z,
            int x, int y) {
        waitUntil(driver -> layer.isTileLoaded(z, x, y));
    }
}

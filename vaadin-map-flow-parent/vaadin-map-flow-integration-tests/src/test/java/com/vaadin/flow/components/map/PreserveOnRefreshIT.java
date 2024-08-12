/**
 * Copyright 2000-2024 Vaadin Ltd.
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

@TestPath("vaadin-map/preserve-on-refresh")
public class PreserveOnRefreshIT extends AbstractComponentIT {
    private TestBenchElement customizeMap;

    @Before
    public void init() {
        open();
        $(MapElement.class).waitForFirst();
        customizeMap = $("button").id("customize-map");
    }

    @Test
    public void refresh_preservesCustomConfiguration() {
        customizeMap.click();

        getDriver().navigate().refresh();

        MapElement map = $(MapElement.class).waitForFirst();
        assertMapStateIsPreserved(map);
    }

    private void assertMapStateIsPreserved(MapElement map) {
        // This doesn't need to be an exhaustive test, more of a smoke test. We
        // just want to verify that the synchronization ran at all and reflected
        // the server-side state to the client
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.ViewReference view = mapReference.getView();

        Assert.assertEquals(3, mapReference.getLayers().getLength());
        Assert.assertEquals(22.3, view.getCenter().getX(), 0.0001);
        Assert.assertEquals(60.45, view.getCenter().getY(), 0.0001);
        Assert.assertEquals(14, view.getZoom(), 0.1);
    }
}

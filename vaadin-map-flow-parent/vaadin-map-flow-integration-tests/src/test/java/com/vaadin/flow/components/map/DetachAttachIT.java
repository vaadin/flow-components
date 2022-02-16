package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/detach-attach")
public class DetachAttachIT extends AbstractComponentIT {
    private TestBenchElement detachMap;
    private TestBenchElement attachMap;
    private TestBenchElement moveMap;

    @Before
    public void init() {
        open();
        $(MapElement.class).waitForFirst();
        attachMap = $("button").id("attach-map");
        detachMap = $("button").id("detach-map");
        moveMap = $("button").id("move-map");
    }

    /**
     * Detaches the map, then in a later roundtrip reattaches the map, and
     * verifies that the map synchronizes the full configuration, regardless
     * whether configuration objects are marked as changed or not
     */
    @Test
    public void detach_attach_fullConfigurationSynchronized() {
        detachMap.click();
        attachMap.click();

        MapElement map = $(MapElement.class).first();
        assertMapIsConfigured(map);
    }

    /**
     * Moves the map to a different container element in the same roundtrip, and
     * verifies that the map synchronizes the full configuration, regardless
     * whether configuration objects are marked as changed or not
     */
    @Test
    public void move_fullConfigurationSynchronized() {
        moveMap.click();

        MapElement map = $(MapElement.class).first();
        assertMapIsConfigured(map);
    }

    private void assertMapIsConfigured(MapElement map) {
        // IT page uses the default configuration, so check if map is configured
        // with the two default layers
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(2, numLayers);
    }
}

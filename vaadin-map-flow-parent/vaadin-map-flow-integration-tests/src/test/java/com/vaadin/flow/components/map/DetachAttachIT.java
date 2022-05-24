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
    public void detach_attach_configurationSynchronized() {
        detachMap.click();
        attachMap.click();

        MapElement map = $(MapElement.class).first();
        assertMapIsSynchronized(map);
    }

    /**
     * Moves the map to a different container element in the same roundtrip, and
     * verifies that the map synchronizes the full configuration, regardless
     * whether configuration objects are marked as changed or not
     */
    @Test
    public void move_configurationSynchronized() {
        moveMap.click();

        MapElement map = $(MapElement.class).first();
        assertMapIsSynchronized(map);
    }

    private void assertMapIsSynchronized(MapElement map) {
        // This doesn't need to be an exhaustive test, more of a smoke test. We
        // just want to verify that the synchronization ran at all and reflected
        // the server-side state to the client
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.ViewReference view = mapReference.getView();

        Assert.assertEquals(2, mapReference.getLayers().getLength());
        Assert.assertEquals(2482424.644689998, view.getCenter().getX(), 0.0001);
        Assert.assertEquals(8500614.173537256, view.getCenter().getY(), 0.0001);
        Assert.assertEquals(14, view.getZoom(), 0.1);
    }
}

package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
        assertCustomizedConfiguration(map);
    }

    private void assertCustomizedConfiguration(MapElement map) {
        // We should have 3 layers in total
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(3, numLayers);
        // And a custom viewport
        List<Number> center = (List<Number>) map
                .evaluateOLExpression("map.getView().getCenter()");
        Number zoomLevel = (Number) map
                .evaluateOLExpression("map.getView().getZoom()");
        Assert.assertEquals(center.get(0).doubleValue(), 2482424.644689998,
                0.0001);
        Assert.assertEquals(center.get(1).doubleValue(), 8500614.173537256,
                0.0001);
        Assert.assertEquals(zoomLevel.doubleValue(), 14, 0.1);
    }
}

package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@TestPath("vaadin-map/view")
public class ViewIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void changeViewPort() {
        // Testing setCenter
        MapElement map = $(MapElement.class).first();
        TestBenchElement setCenterButton = $("button").id("set-center-button");
        setCenterButton.click();

        List<Number> center = (List<Number>) map
                .evaluateOLExpression("map.getView().getCenter()");
        Assert.assertEquals(center.get(0).doubleValue(), 2482424.644689998,
                0.0001);
        Assert.assertEquals(center.get(1).doubleValue(), 8500614.173537256,
                0.0001);

        // Testing setZoom
        TestBenchElement setZoomButton = $("button").id("set-zoom-button");
        setZoomButton.click();
        Number zoomLevel = (Number) map
                .evaluateOLExpression("map.getView().getZoom()");
        Assert.assertEquals(zoomLevel.doubleValue(), 14, 0.1);

        // Testing setRotation
        TestBenchElement setRotationButton = $("button")
                .id("set-rotation-button");
        setRotationButton.click();

        Number rotation = (Number) map
                .evaluateOLExpression("map.getView().getRotation()");
        Assert.assertEquals(rotation.doubleValue(), 0.785398, 0.1);
    }
}

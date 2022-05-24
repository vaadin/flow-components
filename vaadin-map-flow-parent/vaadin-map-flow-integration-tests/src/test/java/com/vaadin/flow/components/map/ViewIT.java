package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.ViewReference view = mapReference.getView();

        MapElement.Coordinate center = view.getCenter();
        Assert.assertEquals(2482424.644689998, center.getX(), 0.0001);
        Assert.assertEquals(8500614.173537256, center.getY(), 0.0001);

        // Testing setZoom
        TestBenchElement setZoomButton = $("button").id("set-zoom-button");
        setZoomButton.click();
        Assert.assertEquals(14, view.getZoom(), 0.0001);

        // Testing setRotation
        TestBenchElement setRotationButton = $("button")
                .id("set-rotation-button");
        setRotationButton.click();

        Assert.assertEquals(0.785398, view.getRotation(), 0.0001);
    }
}

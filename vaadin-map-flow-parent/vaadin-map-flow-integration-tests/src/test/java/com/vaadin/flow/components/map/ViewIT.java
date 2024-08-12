/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
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
        Assert.assertEquals(22.3, center.getX(), 0.0001);
        Assert.assertEquals(60.45, center.getY(), 0.0001);

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

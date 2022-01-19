package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

@TestPath("vaadin-map/view-methods")
public class MapSetViewIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void setCenter_Should_Center_Map_View() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement setCenterButton = $("button").id("set-center-button");
        setCenterButton.click();

        ArrayList<Double> center = (ArrayList<Double>)map.evaluateOLExpression("map.getView().getCenter()");
        Assert.assertEquals(center.get(0), 2482424.644689998, 0.0001);
        Assert.assertEquals(center.get(1), 8500614.173537256, 0.0001);
    }

    @Test
    public void setZoom_Should_Set_Zoom_Level() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement setZoomButton = $("button").id("set-zoom-button");
        setZoomButton.click();

        long zoomLevel = (long) map.evaluateOLExpression("map.getView().getZoom()");
        Assert.assertEquals(zoomLevel, 14);
    }

    @Test
    public void setRotation_Should_Set_Rotation() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement setZoomButton = $("button").id("set-rotation-button");
        setZoomButton.click();

        double rotation = (double) map.evaluateOLExpression("map.getView().getRotation()");
        Assert.assertEquals(rotation, 0.785398, 0.1);
    }
}

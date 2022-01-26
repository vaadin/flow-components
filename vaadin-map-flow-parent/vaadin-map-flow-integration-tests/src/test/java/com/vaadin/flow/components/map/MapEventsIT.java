package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/map-events")
public class MapEventsIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void changeViewPort_viewStateUpdated() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement stateTextDiv = $("div").id("view-state");

        // We are simulating user changing the view port of the map
        map.evaluateOLExpression(
                "map.getView().setCenter([4849385.650796606, 5487570.011434158]);");
        map.evaluateOLExpression("map.getView().setRotation(5);");
        map.evaluateOLExpression("map.getView().setZoom(6)");

        Assert.assertEquals("4849385.650796606;5487570.011434158;5.0;6.0",
                stateTextDiv.getText());
    }

    @Test
    public void changeViewPort_correctEventDataReceived() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement eventDataDiv = $("div").id("event-data");

        // We are simulating user changing the view port of the map
        map.evaluateOLExpression(
                "map.getView().setCenter([4849385.650796606, 5487570.011434158]);");
        map.evaluateOLExpression("map.getView().setRotation(5);");
        map.evaluateOLExpression("map.getView().setZoom(6)");

        Assert.assertEquals("4849385.650796606;5487570.011434158;5.0;6.0",
                eventDataDiv.getText());
    }

}

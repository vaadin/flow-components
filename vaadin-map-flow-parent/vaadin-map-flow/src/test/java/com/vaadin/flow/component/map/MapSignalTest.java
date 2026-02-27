/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.events.MapViewMoveEndEvent;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

import tools.jackson.databind.node.ArrayNode;

public class MapSignalTest extends AbstractSignalsUnitTest {

    private Map map;

    @Before
    public void setup() {
        map = new Map();
    }

    @After
    public void tearDown() {
        if (map != null && map.isAttached()) {
            map.removeFromParent();
        }
    }

    // ===== BIND ZOOM TESTS =====

    @Test
    public void bindZoom_signalBound_zoomSynchronizedWhenAttached() {
        var zoomSignal = new ValueSignal<>(5.0);
        map.bindZoom(zoomSignal, zoomSignal::set);
        UI.getCurrent().add(map);

        Assert.assertEquals(5.0, map.getView().getZoom(), 0.001);

        zoomSignal.set(10.0);
        Assert.assertEquals(10.0, map.getView().getZoom(), 0.001);

        zoomSignal.set(3.5);
        Assert.assertEquals(3.5, map.getView().getZoom(), 0.001);
    }

    @Test
    public void bindZoom_signalBound_noEffectWhenDetached() {
        var zoomSignal = new ValueSignal<>(5.0);
        map.bindZoom(zoomSignal, zoomSignal::set);
        // Not attached to UI

        double initial = map.getView().getZoom();
        zoomSignal.set(10.0);
        Assert.assertEquals(initial, map.getView().getZoom(), 0.001);
    }

    @Test
    public void bindZoom_signalBound_detachAndReattach() {
        var zoomSignal = new ValueSignal<>(5.0);
        map.bindZoom(zoomSignal, zoomSignal::set);
        UI.getCurrent().add(map);
        Assert.assertEquals(5.0, map.getView().getZoom(), 0.001);

        // Detach
        map.removeFromParent();
        zoomSignal.set(10.0);
        Assert.assertEquals(5.0, map.getView().getZoom(), 0.001);

        // Reattach
        UI.getCurrent().add(map);
        Assert.assertEquals(10.0, map.getView().getZoom(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindZoom_setZoomWhileBound_throwsException() {
        var zoomSignal = new ValueSignal<>(5.0);
        map.bindZoom(zoomSignal, zoomSignal::set);
        UI.getCurrent().add(map);

        map.setZoom(10.0);
    }

    @Test(expected = BindingActiveException.class)
    public void bindZoom_bindAgainWhileBound_throwsException() {
        var zoomSignal = new ValueSignal<>(5.0);
        map.bindZoom(zoomSignal, zoomSignal::set);
        UI.getCurrent().add(map);

        var other = new ValueSignal<>(10.0);
        map.bindZoom(other, other::set);
    }

    // ===== BIND CENTER TESTS =====

    @Test
    public void bindCenter_signalBound_centerSynchronizedWhenAttached() {
        var centerSignal = new ValueSignal<>(new Coordinate(10.0, 20.0));
        map.bindCenter(centerSignal, centerSignal::set);
        UI.getCurrent().add(map);

        Assert.assertEquals(10.0, map.getView().getCenter().getX(), 0.001);
        Assert.assertEquals(20.0, map.getView().getCenter().getY(), 0.001);

        centerSignal.set(new Coordinate(30.0, 40.0));
        Assert.assertEquals(30.0, map.getView().getCenter().getX(), 0.001);
        Assert.assertEquals(40.0, map.getView().getCenter().getY(), 0.001);
    }

    @Test
    public void bindCenter_signalBound_noEffectWhenDetached() {
        var centerSignal = new ValueSignal<>(new Coordinate(10.0, 20.0));
        map.bindCenter(centerSignal, centerSignal::set);
        // Not attached to UI

        Coordinate initial = map.getView().getCenter();
        centerSignal.set(new Coordinate(30.0, 40.0));
        Assert.assertEquals(initial.getX(), map.getView().getCenter().getX(),
                0.001);
        Assert.assertEquals(initial.getY(), map.getView().getCenter().getY(),
                0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindCenter_setCenterWhileBound_throwsException() {
        var centerSignal = new ValueSignal<>(new Coordinate(10.0, 20.0));
        map.bindCenter(centerSignal, centerSignal::set);
        UI.getCurrent().add(map);

        map.setCenter(new Coordinate(30.0, 40.0));
    }

    @Test(expected = BindingActiveException.class)
    public void bindCenter_bindAgainWhileBound_throwsException() {
        var centerSignal = new ValueSignal<>(new Coordinate(10.0, 20.0));
        map.bindCenter(centerSignal, centerSignal::set);
        UI.getCurrent().add(map);

        var otherCenter = new ValueSignal<>(new Coordinate(30.0, 40.0));
        map.bindCenter(otherCenter, otherCenter::set);
    }

    // ===== TWO-WAY BINDING TESTS =====

    @Test
    public void bindZoom_userZoom_signalUpdated() {
        var zoomSignal = new ValueSignal<>(5.0);
        map.bindZoom(zoomSignal, zoomSignal::set);
        UI.getCurrent().add(map);

        fireMoveEndEvent(map, true, 10.0, 0, 0);

        Assert.assertEquals(10.0, zoomSignal.get(), 0.001);
    }

    @Test
    public void bindCenter_userPan_signalUpdated() {
        var centerSignal = new ValueSignal<>(new Coordinate(10.0, 20.0));
        map.bindCenter(centerSignal, centerSignal::set);
        UI.getCurrent().add(map);

        fireMoveEndEvent(map, true, 0, 30.0, 40.0);

        Assert.assertEquals(30.0, centerSignal.get().getX(), 0.001);
        Assert.assertEquals(40.0, centerSignal.get().getY(), 0.001);
    }

    @Test
    public void bindZoom_serverSideChange_signalNotUpdated() {
        var zoomSignal = new ValueSignal<>(5.0);
        map.bindZoom(zoomSignal, zoomSignal::set);
        UI.getCurrent().add(map);

        fireMoveEndEvent(map, false, 10.0, 0, 0);

        Assert.assertEquals(5.0, zoomSignal.get(), 0.001);
    }

    @Test
    public void bindCenter_serverSideChange_signalNotUpdated() {
        var centerSignal = new ValueSignal<>(new Coordinate(10.0, 20.0));
        map.bindCenter(centerSignal, centerSignal::set);
        UI.getCurrent().add(map);

        fireMoveEndEvent(map, false, 0, 30.0, 40.0);

        Assert.assertEquals(10.0, centerSignal.get().getX(), 0.001);
        Assert.assertEquals(20.0, centerSignal.get().getY(), 0.001);
    }

    @Test
    public void noSignalBound_moveEndEvent_noError() {
        UI.getCurrent().add(map);
        fireMoveEndEvent(map, true, 5.0, 10.0, 20.0);
        // No exception expected
    }

    private void fireMoveEndEvent(Map map, boolean fromClient, double zoom,
            double centerX, double centerY) {
        ArrayNode coordinates = JacksonUtils.createArrayNode();
        coordinates.add(centerX);
        coordinates.add(centerY);
        ArrayNode extent = JacksonUtils.createArrayNode();
        extent.add(0);
        extent.add(0);
        extent.add(0);
        extent.add(0);

        ComponentUtil.fireEvent(map, new MapViewMoveEndEvent(map, fromClient, 0,
                zoom, coordinates, extent));
    }
}

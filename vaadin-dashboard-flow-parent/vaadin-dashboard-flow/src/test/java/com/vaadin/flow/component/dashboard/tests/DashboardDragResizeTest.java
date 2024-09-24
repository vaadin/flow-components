/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

import elemental.json.Json;
import elemental.json.JsonObject;

public class DashboardDragResizeTest extends DashboardTestBase {
    private Dashboard dashboard;

    @Before
    @Override
    public void setup() {
        super.setup();
        dashboard = new Dashboard();
        dashboard.add(new DashboardWidget());
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();
        section.add(new DashboardWidget());
        getUi().add(dashboard);
        fakeClientCommunication();
    }

    @Test
    public void resizeWidgetHorizontally_sizeIsUpdated() {
        assertWidgetResized(0, 2, 1);
    }

    @Test
    public void resizeWidgetVertically_sizeIsUpdated() {
        assertWidgetResized(0, 1, 2);
    }

    @Test
    public void resizeWidgetBothHorizontallyAndVertically_sizeIsUpdated() {
        assertWidgetResized(0, 2, 2);
    }

    @Test
    public void resizeWidgetInSectionHorizontally_sizeIsUpdated() {
        assertWidgetResized(1, 2, 1);
    }

    @Test
    public void resizeWidgetInSectionVertically_sizeIsUpdated() {
        assertWidgetResized(1, 1, 2);
    }

    @Test
    public void resizeWidgetInSectionBothHorizontallyAndVertically_sizeIsUpdated() {
        assertWidgetResized(1, 2, 2);
    }

    @Test
    public void setDashboardNotEditable_resizeWidget_sizeIsNotUpdated() {
        dashboard.setEditable(false);
        DashboardWidget widgetToResize = dashboard.getWidgets().get(0);
        fireItemResizedEvent(widgetToResize, 2, 2);
        Assert.assertEquals(1, widgetToResize.getColspan());
        Assert.assertEquals(1, widgetToResize.getRowspan());
    }

    @Test
    public void resizeWidget_eventCorrectlyFired() {
        DashboardWidget resizedWidget = (DashboardWidget) dashboard
                .getChildren().toList().get(0);
        assertEventCorrectlyFired(resizedWidget, 1, resizedWidget,
                dashboard.getChildren().toList());
    }

    @Test
    public void resizeWidgetInSection_eventCorrectlyFired() {
        DashboardSection section = (DashboardSection) dashboard.getChildren()
                .toList().get(1);
        DashboardWidget resizedWidget = section.getWidgets().get(0);
        assertEventCorrectlyFired(resizedWidget, 1, resizedWidget,
                dashboard.getChildren().toList());
    }

    @Test
    public void setDashboardNotEditable_resizeWidget_eventNotFired() {
        dashboard.setEditable(false);
        DashboardWidget resizedWidget = (DashboardWidget) dashboard
                .getChildren().toList().get(0);
        assertEventCorrectlyFired(resizedWidget, 0, null, null);
    }

    private void assertEventCorrectlyFired(DashboardWidget widgetToResize,
            int expectedListenerInvokedCount, Component expectedResizedWidget,
            List<Component> expectedItems) {
        AtomicInteger listenerInvokedCount = new AtomicInteger(0);
        AtomicReference<Component> eventResizedWidget = new AtomicReference<>();
        AtomicReference<List<Component>> eventItems = new AtomicReference<>();
        dashboard.addItemResizedListener(e -> {
            listenerInvokedCount.incrementAndGet();
            eventResizedWidget.set(e.getItem());
            eventItems.set(e.getItems());
            e.unregisterListener();
        });
        fireItemResizedEvent(widgetToResize, 2, 2);
        Assert.assertEquals(expectedListenerInvokedCount,
                listenerInvokedCount.get());
        if (expectedListenerInvokedCount > 0) {
            Assert.assertEquals(expectedResizedWidget,
                    eventResizedWidget.get());
            Assert.assertEquals(expectedItems, eventItems.get());
        }
    }

    private void assertWidgetResized(int widgetIndexToResize, int targetColspan,
            int targetRowspan) {
        DashboardWidget widgetToResize = dashboard.getWidgets()
                .get(widgetIndexToResize);
        // Assert widget is enlarged
        fireItemResizedEvent(widgetToResize, targetColspan, targetRowspan);
        Assert.assertEquals(targetColspan, widgetToResize.getColspan());
        Assert.assertEquals(targetRowspan, widgetToResize.getRowspan());
        // Assert widget is shrunk
        fireItemResizedEvent(widgetToResize, 1, 1);
        Assert.assertEquals(1, widgetToResize.getColspan());
        Assert.assertEquals(1, widgetToResize.getRowspan());
    }

    private void fireItemResizedEvent(DashboardWidget widget, int targetColspan,
            int targetRowspan) {
        JsonObject eventData = Json.createObject();
        eventData.put("event.detail.item.nodeid",
                widget.getElement().getNode().getId());
        eventData.put("event.detail.item.rowspan", targetRowspan);
        eventData.put("event.detail.item.colspan", targetColspan);
        DomEvent itemResizedDomEvent = new DomEvent(dashboard.getElement(),
                "dashboard-item-resized", eventData);
        dashboard.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(itemResizedDomEvent);
    }
}

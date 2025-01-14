/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;

public class DashboardItemResizeTest extends DashboardTestBase {
    private Dashboard dashboard;

    @Before
    @Override
    public void setup() {
        super.setup();
        dashboard = getNewDashboard();
        dashboard.add(getNewWidget());
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();
        section.add(getNewWidget());
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
        DashboardTestHelper.fireItemResizedEvent(dashboard, widgetToResize, 2,
                2);
        Assert.assertEquals(1, widgetToResize.getColspan());
        Assert.assertEquals(1, widgetToResize.getRowspan());
    }

    @Test
    public void resizeWidget_noClientUpdate() {
        getUi().getInternals().dumpPendingJavaScriptInvocations();

        assertWidgetResized(0, 2, 1);

        fakeClientCommunication();

        Assert.assertTrue(getUi().getInternals()
                .dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    public void resizeWidget_eventCorrectlyFired() {
        DashboardWidget resizedWidget = (DashboardWidget) dashboard
                .getChildren().toList().get(0);
        assertItemResizedEventCorrectlyFired(resizedWidget, 1, resizedWidget,
                dashboard.getChildren().toList());
    }

    @Test
    public void resizeWidgetInSection_eventCorrectlyFired() {
        DashboardSection section = (DashboardSection) dashboard.getChildren()
                .toList().get(1);
        DashboardWidget resizedWidget = section.getWidgets().get(0);
        assertItemResizedEventCorrectlyFired(resizedWidget, 1, resizedWidget,
                dashboard.getChildren().toList());
    }

    @Test
    public void setDashboardNotEditable_resizeWidget_eventNotFired() {
        dashboard.setEditable(false);
        DashboardWidget resizedWidget = (DashboardWidget) dashboard
                .getChildren().toList().get(0);
        assertItemResizedEventCorrectlyFired(resizedWidget, 0, null, null);
    }

    @Test
    public void changeWidgetResizeMode_eventCorrectlyFired() {
        Component resizedItem = dashboard.getChildren().toList().get(0);
        assertItemResizeModeChangedEventCorrectlyFired(resizedItem, true);
        assertItemResizeModeChangedEventCorrectlyFired(resizedItem, false);
    }

    @Test
    public void changeWidgetInSectionResizeMode_eventCorrectlyFired() {
        DashboardSection section = (DashboardSection) dashboard.getChildren()
                .toList().get(1);
        Component resizedItem = section.getWidgets().get(0);
        assertItemResizeModeChangedEventCorrectlyFired(resizedItem, true);
        assertItemResizeModeChangedEventCorrectlyFired(resizedItem, false);
    }

    private void assertItemResizedEventCorrectlyFired(
            DashboardWidget widgetToResize, int expectedListenerInvokedCount,
            Component expectedResizedWidget, List<Component> expectedItems) {
        AtomicInteger listenerInvokedCount = new AtomicInteger(0);
        AtomicReference<Component> eventResizedWidget = new AtomicReference<>();
        AtomicReference<List<Component>> eventItems = new AtomicReference<>();
        dashboard.addItemResizedListener(e -> {
            listenerInvokedCount.incrementAndGet();
            eventResizedWidget.set(e.getItem());
            eventItems.set(e.getItems());
            e.unregisterListener();
        });
        DashboardTestHelper.fireItemResizedEvent(dashboard, widgetToResize, 2,
                2);
        Assert.assertEquals(expectedListenerInvokedCount,
                listenerInvokedCount.get());
        if (expectedListenerInvokedCount > 0) {
            Assert.assertEquals(expectedResizedWidget,
                    eventResizedWidget.get());
            Assert.assertEquals(expectedItems, eventItems.get());
        }
    }

    private void assertItemResizeModeChangedEventCorrectlyFired(Component item,
            boolean resizeMode) {
        AtomicInteger listenerInvokedCount = new AtomicInteger(0);
        AtomicReference<Component> eventItem = new AtomicReference<>();
        AtomicReference<Boolean> eventIsResizeMode = new AtomicReference<>();
        dashboard.addItemResizeModeChangedListener(e -> {
            listenerInvokedCount.incrementAndGet();
            eventItem.set(e.getItem());
            eventIsResizeMode.set(e.isResizeMode());
            e.unregisterListener();
        });
        DashboardTestHelper.fireItemResizeModeChangedEvent(dashboard,
                item.getElement().getNode().getId(), resizeMode);
        Assert.assertEquals(1, listenerInvokedCount.get());
        Assert.assertEquals(item, eventItem.get());
        Assert.assertEquals(resizeMode, eventIsResizeMode.get());
    }

    private void assertWidgetResized(int widgetIndexToResize, int targetColspan,
            int targetRowspan) {
        DashboardWidget widgetToResize = dashboard.getWidgets()
                .get(widgetIndexToResize);
        // Assert widget is enlarged
        DashboardTestHelper.fireItemResizedEvent(dashboard, widgetToResize,
                targetColspan, targetRowspan);
        Assert.assertEquals(targetColspan, widgetToResize.getColspan());
        Assert.assertEquals(targetRowspan, widgetToResize.getRowspan());
        // Assert widget is shrunk
        DashboardTestHelper.fireItemResizedEvent(dashboard, widgetToResize, 1,
                1);
        Assert.assertEquals(1, widgetToResize.getColspan());
        Assert.assertEquals(1, widgetToResize.getRowspan());
    }
}

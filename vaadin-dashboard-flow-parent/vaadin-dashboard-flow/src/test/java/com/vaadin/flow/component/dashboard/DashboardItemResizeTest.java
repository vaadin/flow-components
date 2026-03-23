/**
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;

class DashboardItemResizeTest extends DashboardTestBase {
    private Dashboard dashboard;

    @BeforeEach
    @Override
    void setup() {
        super.setup();
        dashboard = getNewDashboard();
        dashboard.add(getNewWidget());
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();
        section.add(getNewWidget());
        ui.add(dashboard);
        ui.fakeClientCommunication();
    }

    @Test
    void resizeWidgetHorizontally_sizeIsUpdated() {
        assertWidgetResized(0, 2, 1);
    }

    @Test
    void resizeWidgetVertically_sizeIsUpdated() {
        assertWidgetResized(0, 1, 2);
    }

    @Test
    void resizeWidgetBothHorizontallyAndVertically_sizeIsUpdated() {
        assertWidgetResized(0, 2, 2);
    }

    @Test
    void resizeWidgetInSectionHorizontally_sizeIsUpdated() {
        assertWidgetResized(1, 2, 1);
    }

    @Test
    void resizeWidgetInSectionVertically_sizeIsUpdated() {
        assertWidgetResized(1, 1, 2);
    }

    @Test
    void resizeWidgetInSectionBothHorizontallyAndVertically_sizeIsUpdated() {
        assertWidgetResized(1, 2, 2);
    }

    @Test
    void setDashboardNotEditable_resizeWidget_sizeIsNotUpdated() {
        dashboard.setEditable(false);
        DashboardWidget widgetToResize = dashboard.getWidgets().get(0);
        DashboardTestHelper.fireItemResizedEvent(dashboard, widgetToResize, 2,
                2);
        Assertions.assertEquals(1, widgetToResize.getColspan());
        Assertions.assertEquals(1, widgetToResize.getRowspan());
    }

    @Test
    void resizeWidget_noClientUpdate() {
        ui.dumpPendingJavaScriptInvocations();

        assertWidgetResized(0, 2, 1);

        ui.fakeClientCommunication();

        Assertions.assertTrue(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    void resizeWidget_eventCorrectlyFired() {
        DashboardWidget resizedWidget = (DashboardWidget) dashboard
                .getChildren().toList().get(0);
        assertItemResizedEventCorrectlyFired(resizedWidget, 1, resizedWidget,
                dashboard.getChildren().toList());
    }

    @Test
    void resizeWidgetInSection_eventCorrectlyFired() {
        DashboardSection section = (DashboardSection) dashboard.getChildren()
                .toList().get(1);
        DashboardWidget resizedWidget = section.getWidgets().get(0);
        assertItemResizedEventCorrectlyFired(resizedWidget, 1, resizedWidget,
                dashboard.getChildren().toList());
    }

    @Test
    void setDashboardNotEditable_resizeWidget_eventNotFired() {
        dashboard.setEditable(false);
        DashboardWidget resizedWidget = (DashboardWidget) dashboard
                .getChildren().toList().get(0);
        assertItemResizedEventCorrectlyFired(resizedWidget, 0, null, null);
    }

    @Test
    void changeWidgetResizeMode_eventCorrectlyFired() {
        Component resizedItem = dashboard.getChildren().toList().get(0);
        assertItemResizeModeChangedEventCorrectlyFired(resizedItem, true);
        assertItemResizeModeChangedEventCorrectlyFired(resizedItem, false);
    }

    @Test
    void changeWidgetInSectionResizeMode_eventCorrectlyFired() {
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
        Assertions.assertEquals(expectedListenerInvokedCount,
                listenerInvokedCount.get());
        if (expectedListenerInvokedCount > 0) {
            Assertions.assertEquals(expectedResizedWidget,
                    eventResizedWidget.get());
            Assertions.assertEquals(expectedItems, eventItems.get());
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
        Assertions.assertEquals(1, listenerInvokedCount.get());
        Assertions.assertEquals(item, eventItem.get());
        Assertions.assertEquals(resizeMode, eventIsResizeMode.get());
    }

    private void assertWidgetResized(int widgetIndexToResize, int targetColspan,
            int targetRowspan) {
        DashboardWidget widgetToResize = dashboard.getWidgets()
                .get(widgetIndexToResize);
        // Assert widget is enlarged
        DashboardTestHelper.fireItemResizedEvent(dashboard, widgetToResize,
                targetColspan, targetRowspan);
        Assertions.assertEquals(targetColspan, widgetToResize.getColspan());
        Assertions.assertEquals(targetRowspan, widgetToResize.getRowspan());
        // Assert widget is shrunk
        DashboardTestHelper.fireItemResizedEvent(dashboard, widgetToResize, 1,
                1);
        Assertions.assertEquals(1, widgetToResize.getColspan());
        Assertions.assertEquals(1, widgetToResize.getRowspan());
    }
}

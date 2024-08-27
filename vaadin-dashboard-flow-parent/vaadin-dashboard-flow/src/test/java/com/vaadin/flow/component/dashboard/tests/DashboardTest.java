/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.JsonArray;

public class DashboardTest {

    private final UI ui = new UI();
    private Dashboard dashboard;

    @Before
    public void setup() {
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        dashboard = new Dashboard();
        ui.add(dashboard);
        fakeClientCommunication();
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addWidget_widgetIsAdded() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        assertWidgets(dashboard, widget1, widget2);
    }

    @Test
    public void addNullWidget_exceptionIsThrown() {
        Assert.assertThrows(NullPointerException.class,
                () -> dashboard.add((DashboardWidget) null));
    }

    @Test
    public void addNullWidgetInArray_noWidgetIsAdded() {
        DashboardWidget widget = new DashboardWidget();
        try {
            dashboard.add(widget, null);
        } catch (NullPointerException e) {
            // Do nothing
        }
        fakeClientCommunication();
        assertWidgets(dashboard);
    }

    @Test
    public void addWidgetAtIndex_widgetIsCorrectlyAdded() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        DashboardWidget widget3 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        dashboard.addWidgetAtIndex(1, widget3);
        fakeClientCommunication();
        assertWidgets(dashboard, widget1, widget3, widget2);
    }

    @Test
    public void addNullWidgetAtIndex_exceptionIsThrown() {
        Assert.assertThrows(NullPointerException.class,
                () -> dashboard.addWidgetAtIndex(0, null));
    }

    @Test
    public void removeWidget_widgetIsRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        dashboard.remove(widget1);
        fakeClientCommunication();
        assertWidgets(dashboard, widget2);
    }

    @Test
    public void removeNullWidget_exceptionIsThrown() {
        Assert.assertThrows(NullPointerException.class,
                () -> dashboard.remove((DashboardWidget) null));
    }

    @Test
    public void removeAllWidgets_widgetsAreRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        dashboard.removeAll();
        fakeClientCommunication();
        assertWidgets(dashboard);
    }

    @Test
    public void removeWidgetFromParent_widgetIsRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        dashboard.add(widget1);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertWidgets(dashboard);
    }

    @Test
    public void addWidget_virtualNodeIdsInSync() {
        DashboardWidget widget1 = new DashboardWidget();
        dashboard.add(widget1);
        fakeClientCommunication();
        assertWidgets(dashboard, widget1);
    }

    @Test
    public void removeWidget_virtualNodeIdsInSync() {
        DashboardWidget widget1 = new DashboardWidget();
        dashboard.add(widget1);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertWidgets(dashboard);
    }

    @Test
    public void selfRemoveChild_virtualNodeIdsInSync() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertWidgets(dashboard, widget2);
    }

    @Test
    public void addSeparately_selfRemoveChild_doesNotThrow() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1);
        dashboard.add(widget2);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertWidgets(dashboard, widget2);
    }

    @Test
    public void addWidgetFromLayoutToDashboard_widgetIsMoved() {
        Div parent = new Div();
        ui.add(parent);
        DashboardWidget widget = new DashboardWidget();
        parent.add(widget);
        fakeClientCommunication();
        dashboard.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(parent.getChildren().noneMatch(widget::equals));
        assertWidgets(dashboard, widget);
    }

    @Test
    public void addWidgetFromDashboardToLayout_widgetIsMoved() {
        DashboardWidget widget = new DashboardWidget();
        dashboard.add(widget);
        fakeClientCommunication();
        Div parent = new Div();
        ui.add(parent);
        parent.add(widget);
        fakeClientCommunication();
        assertWidgets(dashboard);
        Assert.assertTrue(parent.getChildren().anyMatch(widget::equals));
    }

    @Test
    public void addWidgetToAnotherDashboard_widgetIsMoved() {
        DashboardWidget widget = new DashboardWidget();
        dashboard.add(widget);
        fakeClientCommunication();
        Dashboard newDashboard = new Dashboard();
        ui.add(newDashboard);
        newDashboard.add(widget);
        fakeClientCommunication();
        assertWidgets(dashboard);
        assertWidgets(newDashboard, widget);
    }

    @Test
    public void setMaximumColumnCount_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-col-max-count";
        int valueToSet = 5;

        Assert.assertNull(dashboard.getStyle().get(propertyName));

        dashboard.setMaximumColumnCount(valueToSet);
        Assert.assertEquals(String.valueOf(valueToSet),
                dashboard.getStyle().get(propertyName));

        dashboard.setMaximumColumnCount(null);
        Assert.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    public void setMaximumColumnCountNull_propertyIsRemoved() {
        dashboard.setMaximumColumnCount(5);

        dashboard.setMaximumColumnCount(null);
        Assert.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-col-max-count"));
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private static void assertWidgets(Dashboard dashboard,
            DashboardWidget... expectedWidgets) {
        assertVirtualChildren(dashboard, expectedWidgets);
        Assert.assertEquals(Arrays.asList(expectedWidgets),
                dashboard.getWidgets());
    }

    private static void assertVirtualChildren(Dashboard dashboard,
            Component... components) {
        // Get a List of the node ids
        List<Integer> expectedChildNodeIds = Arrays.stream(components)
                .map(component -> component.getElement().getNode().getId())
                .toList();
        // Get the node ids from the items property of the dashboard
        List<Integer> actualChildNodeIds = getChildNodeIds(dashboard);
        Assert.assertEquals(expectedChildNodeIds, actualChildNodeIds);
    }

    private static List<Integer> getChildNodeIds(Dashboard dashboard) {
        JsonArray jsonArrayOfIds = (JsonArray) dashboard.getElement()
                .getPropertyRaw("items");
        return JsonUtils.objectStream(jsonArrayOfIds)
                .mapToInt(obj -> (int) obj.getNumber("nodeid")).boxed()
                .toList();
    }
}

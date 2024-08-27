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
import java.util.Collections;
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

        Assert.assertEquals(List.of(widget1, widget2), dashboard.getWidgets());
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
        Assert.assertEquals(Collections.emptyList(), dashboard.getWidgets());
    }

    @Test
    public void addWidgetAtIndex_widgetIsCorrectlyAdded() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        DashboardWidget widget3 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        dashboard.addWidgetAtIndex(1, widget3);

        Assert.assertEquals(List.of(widget1, widget3, widget2),
                dashboard.getWidgets());
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
        dashboard.remove(widget1);

        Assert.assertEquals(List.of(widget2), dashboard.getWidgets());
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
        dashboard.removeAll();

        Assert.assertEquals(Collections.emptyList(), dashboard.getWidgets());
    }

    @Test
    public void removeWidgetFromParent_widgetIsRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        dashboard.add(widget1);
        fakeClientCommunication();
        widget1.removeFromParent();
        Assert.assertEquals(Collections.emptyList(), dashboard.getWidgets());
    }

    @Test
    public void addWidget_virtualNodeIdsInSync() {
        DashboardWidget widget1 = new DashboardWidget();
        dashboard.add(widget1);
        fakeClientCommunication();
        assertVirtualChildren(dashboard, widget1);
    }

    @Test
    public void removeWidget_virtualNodeIdsInSync() {
        DashboardWidget widget1 = new DashboardWidget();
        dashboard.add(widget1);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertVirtualChildren(dashboard);
    }

    @Test
    public void selfRemoveChild_virtualNodeIdsInSync() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertVirtualChildren(dashboard, widget2);
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
        assertVirtualChildren(dashboard, widget2);
    }

    @Test
    public void addWidgetToLayout_widgetIsAdded() {
        Div layout = new Div();
        ui.add(layout);
        DashboardWidget widget = new DashboardWidget();
        layout.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(widget.getParent().isPresent());
        Assert.assertEquals(layout, widget.getParent().get());
    }

    @Test
    public void removeWidgetFromLayout_widgetIsRemoved() {
        Div layout = new Div();
        ui.add(layout);
        DashboardWidget widget = new DashboardWidget();
        layout.add(widget);
        fakeClientCommunication();
        layout.remove(widget);
        fakeClientCommunication();
        Assert.assertTrue(widget.getParent().isEmpty());
    }

    @Test
    public void addWidgetToLayout_removeFromParent_widgetIsRemoved() {
        Div layout = new Div();
        ui.add(layout);
        DashboardWidget widget = new DashboardWidget();
        layout.add(widget);
        fakeClientCommunication();
        widget.removeFromParent();
        fakeClientCommunication();
        Assert.assertTrue(widget.getParent().isEmpty());
    }

    @Test
    public void addWidgetFromLayoutToDashboard_widgetIsAdded() {
        Div parent = new Div();
        ui.add(parent);
        DashboardWidget widget = new DashboardWidget();
        parent.add(widget);
        fakeClientCommunication();
        dashboard.add(widget);
        fakeClientCommunication();
        assertWidgets(dashboard, widget);
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
    public void addWidgetFromLayoutToOtherLayout_widgetIsAdded() {
        Div parent = new Div();
        ui.add(parent);
        DashboardWidget widget = new DashboardWidget();
        parent.add(widget);
        fakeClientCommunication();
        Div newParent = new Div();
        ui.add(newParent);
        newParent.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(widget.getParent().isPresent());
        Assert.assertEquals(newParent, widget.getParent().get());
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

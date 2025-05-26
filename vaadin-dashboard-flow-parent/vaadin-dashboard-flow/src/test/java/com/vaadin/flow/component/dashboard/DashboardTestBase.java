/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class DashboardTestBase {

    private final UI ui = new UI();

    @Before
    public void setup() {
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);
        fakeClientCommunication();
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    protected UI getUi() {
        return ui;
    }

    protected void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    protected static JsonArray getItemsArray(
            List<Component> rootLevelComponents) {
        JsonArray itemsArray = Json.createArray();
        rootLevelComponents.forEach(child -> {
            JsonObject rootLevelItem = Json.createObject();
            rootLevelItem.put("id", child.getElement().getNode().getId());
            if (child instanceof DashboardSection section) {
                JsonArray sectionItemsArray = Json.createArray();
                section.getWidgets().forEach(widget -> {
                    JsonObject sectionItem = Json.createObject();
                    sectionItem.put("id",
                            widget.getElement().getNode().getId());
                    sectionItemsArray.set(sectionItemsArray.length(),
                            sectionItem);
                });
                rootLevelItem.put("items", sectionItemsArray);
            }
            itemsArray.set(itemsArray.length(), rootLevelItem);
        });
        return itemsArray;
    }

    protected static void assertChildComponents(Dashboard dashboard,
            Component... expectedChildren) {
        List<DashboardWidget> expectedWidgets = getExpectedWidgets(
                expectedChildren);
        Assert.assertEquals(expectedWidgets, dashboard.getWidgets());
        Assert.assertEquals(Arrays.asList(expectedChildren),
                dashboard.getChildren().toList());
    }

    protected static List<DashboardWidget> getExpectedWidgets(
            Component... expectedChildren) {
        List<DashboardWidget> expectedWidgets = new ArrayList<>();
        for (Component child : expectedChildren) {
            if (child instanceof DashboardSection section) {
                expectedWidgets.addAll(section.getWidgets());
            } else if (child instanceof DashboardWidget widget) {
                expectedWidgets.add(widget);
            } else {
                throw new IllegalArgumentException(
                        "A dashboard can only contain widgets or sections.");
            }
        }
        return expectedWidgets;
    }

    protected static void assertSectionWidgets(DashboardSection section,
            DashboardWidget... expectedWidgets) {
        Assert.assertEquals(Arrays.asList(expectedWidgets),
                section.getWidgets());
    }

    protected DashboardWidget getNewWidget() {
        return new DashboardWidget();
    }

    protected Dashboard getNewDashboard() {
        return new Dashboard();
    }
}

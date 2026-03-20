/**
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class DashboardTestBase {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @BeforeEach
    void setup() {
        ui.fakeClientCommunication();
    }

    protected static ArrayNode getItemsArray(
            List<Component> rootLevelComponents) {
        ArrayNode itemsArray = JacksonUtils.createArrayNode();
        rootLevelComponents.forEach(child -> {
            ObjectNode rootLevelItem = JacksonUtils.createObjectNode();
            rootLevelItem.put("id", child.getElement().getNode().getId());
            if (child instanceof DashboardSection section) {
                ArrayNode sectionItemsArray = JacksonUtils.createArrayNode();
                section.getWidgets().forEach(widget -> {
                    ObjectNode sectionItem = JacksonUtils.createObjectNode();
                    sectionItem.put("id",
                            widget.getElement().getNode().getId());
                    sectionItemsArray.add(sectionItem);
                });
                rootLevelItem.set("items", sectionItemsArray);
            }
            itemsArray.add(rootLevelItem);
        });
        return itemsArray;
    }

    protected static void assertChildComponents(Dashboard dashboard,
            Component... expectedChildren) {
        List<DashboardWidget> expectedWidgets = getExpectedWidgets(
                expectedChildren);
        Assertions.assertEquals(expectedWidgets, dashboard.getWidgets());
        Assertions.assertEquals(Arrays.asList(expectedChildren),
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
        Assertions.assertEquals(Arrays.asList(expectedWidgets),
                section.getWidgets());
    }

    protected DashboardWidget getNewWidget() {
        return new DashboardWidget();
    }

    protected Dashboard getNewDashboard() {
        return new Dashboard();
    }
}

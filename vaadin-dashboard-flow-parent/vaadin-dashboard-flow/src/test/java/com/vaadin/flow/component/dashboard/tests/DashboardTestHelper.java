/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class DashboardTestHelper {

    static void fireItemResizeModeChangedEvent(Dashboard dashboard,
            int itemNodeId, boolean resizeMode) {
        JsonObject eventData = Json.createObject();
        eventData.put("event.detail.item.nodeid", itemNodeId);
        eventData.put("event.detail.value", resizeMode);
        fireDomEvent(dashboard, "dashboard-item-resize-mode-changed",
                eventData);
    }

    static void fireItemResizedEvent(Dashboard dashboard,
            DashboardWidget widget, int targetColspan, int targetRowspan) {
        JsonObject eventData = Json.createObject();
        eventData.put("event.detail.item.nodeid",
                widget.getElement().getNode().getId());
        eventData.put("event.detail.item.rowspan", targetRowspan);
        eventData.put("event.detail.item.colspan", targetColspan);
        fireDomEvent(dashboard, "dashboard-item-resized", eventData);
    }

    static void fireItemMovedEvent(Dashboard dashboard, int itemNodeId,
            JsonArray itemsArray, Integer sectionNodeId) {
        JsonObject eventData = Json.createObject();
        eventData.put("event.detail.item", itemNodeId);
        eventData.put("event.detail.items", itemsArray);
        if (sectionNodeId != null) {
            eventData.put("event.detail.section", sectionNodeId);
        }
        fireDomEvent(dashboard, "dashboard-item-moved-flow", eventData);
    }

    static void fireItemMoveModeChangedEvent(Dashboard dashboard,
            int itemNodeId, boolean moveMode) {
        JsonObject eventData = Json.createObject();
        eventData.put("event.detail.item.nodeid", itemNodeId);
        eventData.put("event.detail.value", moveMode);
        fireDomEvent(dashboard, "dashboard-item-move-mode-changed", eventData);
    }

    static void fireItemRemovedEvent(Dashboard dashboard, int nodeId) {
        JsonObject eventData = Json.createObject();
        eventData.put("event.detail.item.nodeid", nodeId);
        fireDomEvent(dashboard, "dashboard-item-removed", eventData);
    }

    static void fireItemSelectedChangedEvent(Dashboard dashboard,
            int itemNodeId, boolean selected) {
        JsonObject eventData = Json.createObject();
        eventData.put("event.detail.item.nodeid", itemNodeId);
        eventData.put("event.detail.value", selected);
        fireDomEvent(dashboard, "dashboard-item-selected-changed", eventData);
    }

    private static void fireDomEvent(Dashboard dashboard, String eventType,
            JsonObject eventData) {
        DomEvent domEvent = new DomEvent(dashboard.getElement(), eventType,
                eventData);
        dashboard.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(domEvent);
    }
}

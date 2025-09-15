/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

public class DashboardTestHelper {

    static void fireItemResizeModeChangedEvent(Dashboard dashboard,
            int itemNodeId, boolean resizeMode) {
        ObjectNode eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.item.id", itemNodeId);
        eventData.put("event.detail.value", resizeMode);
        fireDomEvent(dashboard, "dashboard-item-resize-mode-changed",
                eventData);
    }

    static void fireItemResizedEvent(Dashboard dashboard,
            DashboardWidget widget, int targetColspan, int targetRowspan) {
        ObjectNode eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.item.id",
                widget.getElement().getNode().getId());
        eventData.put("event.detail.item.rowspan", targetRowspan);
        eventData.put("event.detail.item.colspan", targetColspan);
        fireDomEvent(dashboard, "dashboard-item-resized", eventData);
    }

    static void fireItemMovedEvent(Dashboard dashboard, int itemNodeId,
            ArrayNode itemsArray, Integer sectionNodeId) {
        ObjectNode eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.item", itemNodeId);
        eventData.set("event.detail.items", itemsArray);
        if (sectionNodeId != null) {
            eventData.put("event.detail.section", sectionNodeId);
        }
        fireDomEvent(dashboard, "dashboard-item-moved-flow", eventData);
    }

    static void fireItemMoveModeChangedEvent(Dashboard dashboard,
            int itemNodeId, boolean moveMode) {
        ObjectNode eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.item.id", itemNodeId);
        eventData.put("event.detail.value", moveMode);
        fireDomEvent(dashboard, "dashboard-item-move-mode-changed", eventData);
    }

    static void fireItemRemovedEvent(Dashboard dashboard, int nodeId) {
        ObjectNode eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.item.id", nodeId);
        fireDomEvent(dashboard, "dashboard-item-removed", eventData);
    }

    static void fireItemSelectedChangedEvent(Dashboard dashboard,
            int itemNodeId, boolean selected) {
        ObjectNode eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.item.id", itemNodeId);
        eventData.put("event.detail.value", selected);
        fireDomEvent(dashboard, "dashboard-item-selected-changed", eventData);
    }

    private static void fireDomEvent(Dashboard dashboard, String eventType,
            JsonNode eventData) {
        DomEvent domEvent = new DomEvent(dashboard.getElement(), eventType,
                eventData);
        dashboard.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(domEvent);
    }
}

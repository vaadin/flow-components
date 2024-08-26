/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementDetachEvent;
import com.vaadin.flow.dom.ElementDetachListener;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * @author Vaadin Ltd
 */
@Tag("vaadin-dashboard")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha8")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/dashboard/src/vaadin-dashboard.js")
@JsModule("./flow-component-renderer.js")
// @NpmPackage(value = "@vaadin/dashboard", version = "24.6.0-alpha0")
public class Dashboard extends Component {

    private final List<DashboardWidget> widgets = new ArrayList<>();

    private boolean pendingUpdate = false;

    /**
     * Creates an empty dashboard.
     */
    public Dashboard() {
    }

    /**
     * Returns the widgets in the dashboard.
     *
     * @return The widgets in the dashboard
     */
    public List<DashboardWidget> getWidgets() {
        return Collections.unmodifiableList(widgets);
    }

    /**
     * Adds the given widgets to the dashboard.
     *
     * @param widgets
     *            the widgets to add, not {@code null}
     */
    public void add(DashboardWidget... widgets) {
        Objects.requireNonNull(widgets, "Widgets to add cannot be null.");
        List<DashboardWidget> toAdd = new ArrayList<>(widgets.length);
        for (DashboardWidget widget : widgets) {
            Objects.requireNonNull(widget, "Widget to add cannot be null.");
            toAdd.add(widget);
        }
        toAdd.forEach(this::doAddWidget);
        updateClient();
    }

    /**
     * Adds the given widget as child of this dashboard at the specific index.
     * <p>
     * In case the specified widget has already been added to another parent, it
     * will be removed from there and added to this one.
     *
     * @param index
     *            the index, where the widget will be added. The index must be
     *            non-negative and may not exceed the children count
     * @param widget
     *            the widget to add, not {@code null}
     */
    public void addWidgetAtIndex(int index, DashboardWidget widget) {
        Objects.requireNonNull(widget, "Widget to add cannot be null.");
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a widget with a negative index.");
        }
        // The case when the index is bigger than the children count is handled
        // inside the method below
        doAddWidget(index, widget);
        updateClient();
    }

    /**
     * Removes the given widgets from this dashboard.
     *
     * @param widgets
     *            the widgets to remove, not {@code null}
     * @throws IllegalArgumentException
     *             if there is a widget whose non {@code null} parent is not
     *             this dashboard
     */
    public void remove(DashboardWidget... widgets) {
        Objects.requireNonNull(widgets, "Widgets to remove cannot be null.");
        List<DashboardWidget> toRemove = new ArrayList<>(widgets.length);
        for (DashboardWidget widget : widgets) {
            Objects.requireNonNull(widget, "Widget to remove cannot be null.");
            Element parent = widget.getElement().getParent();
            if (parent == null) {
                LoggerFactory.getLogger(getClass()).debug(
                        "Removal of a widget with no parent does nothing.");
                continue;
            }
            if (getElement().equals(parent)) {
                toRemove.add(widget);
            } else {
                throw new IllegalArgumentException("The given widget (" + widget
                        + ") is not a child of this dashboard");
            }
        }
        toRemove.forEach(this::doRemoveWidget);
        updateClient();
    }

    /**
     * Removes all widgets from this dashboard.
     */
    public void removeAll() {
        doRemoveAllWidgets();
        updateClient();
    }

    private final Map<Element, Registration> childDetachListenerMap = new HashMap<>();

    // Must not use lambda here as that would break serialization. See
    // https://github.com/vaadin/flow-components/issues/5597
    private final ElementDetachListener childDetachListener = new ElementDetachListener() {
        @Override
        public void onDetach(ElementDetachEvent e) {
            var detachedElement = e.getSource();
            getWidgets().stream()
                    .filter(widget -> Objects.equals(detachedElement,
                            widget.getElement()))
                    .findAny().ifPresent(detachedWidget -> {
                        // The child was removed from the dashboard

                        // Remove the registration for the child detach listener
                        childDetachListenerMap.get(detachedWidget.getElement())
                                .remove();
                        childDetachListenerMap
                                .remove(detachedWidget.getElement());

                        doRemoveWidget(detachedWidget);
                        updateClient();
                    });
        }
    };

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachRenderer();
        doUpdateClient();
    }

    private void updateClient() {
        if (pendingUpdate) {
            return;
        }
        pendingUpdate = true;
        getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(this, ctx -> {
                    doUpdateClient();
                    pendingUpdate = false;
                }));
    }

    private void doUpdateClient() {
        getElement().getChildren().forEach(child -> {
            if (!childDetachListenerMap.containsKey(child)) {
                childDetachListenerMap.put(child,
                        child.addDetachListener(childDetachListener));
            }
        });
        getElement().setPropertyJson("items", createItemsJsonArray());
    }

    private void attachRenderer() {
        getElement().executeJs(
                "Vaadin.FlowComponentHost.patchVirtualContainer(this);");
        String appId = UI.getCurrent().getInternals().getAppId();
        getElement().executeJs(
                "this.renderer = (root, _, model) => Vaadin.FlowComponentHost.setChildNodes($0, [model.item.nodeid], root);",
                appId);
    }

    private JsonArray createItemsJsonArray() {
        JsonArray jsonItems = Json.createArray();
        for (DashboardWidget widget : widgets) {
            JsonObject jsonItem = Json.createObject();
            jsonItem.put("nodeid", getWidgetNodeId(widget));
            jsonItems.set(jsonItems.length(), jsonItem);
        }
        return jsonItems;
    }

    private int getWidgetNodeId(DashboardWidget widget) {
        return widget.getElement().getNode().getId();
    }

    private void doRemoveAllWidgets() {
        List<Element> elementsToRemove = widgets.stream()
                .map(Component::getElement).toList();
        elementsToRemove.forEach(getElement()::removeVirtualChild);
        widgets.clear();
    }

    private void doRemoveWidget(DashboardWidget widget) {
        getElement().removeVirtualChild(widget.getElement());
        widgets.remove(widget);
    }

    private void doAddWidget(int index, DashboardWidget widget) {
        if (widget.getParent().isPresent()) {
            widget.removeFromParent();
        }
        getElement().appendVirtualChild(widget.getElement());
        widgets.add(index, widget);
    }

    private void doAddWidget(DashboardWidget widget) {
        if (widget.getParent().isPresent()) {
            widget.removeFromParent();
        }
        getElement().appendVirtualChild(widget.getElement());
        widgets.add(widget);
    }
}

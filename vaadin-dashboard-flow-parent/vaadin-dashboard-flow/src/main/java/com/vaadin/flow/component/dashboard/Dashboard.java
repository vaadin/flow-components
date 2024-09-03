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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;

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
public class Dashboard extends Component implements HasWidgets {

    private final List<Component> childrenComponents = new ArrayList<>();

    private final DashboardChildDetachHandler childDetachHandler;

    private boolean pendingUpdate = false;

    /**
     * Creates an empty dashboard.
     */
    public Dashboard() {
        childDetachHandler = getChildDetachHandler();
    }

    /**
     * Adds an empty section to this dashboard.
     */
    public DashboardSection addSection() {
        return addSection((String) null);
    }

    /**
     * Adds an empty section to this dashboard.
     *
     * @param title
     *            the title of the section
     */
    public DashboardSection addSection(String title) {
        DashboardSection dashboardSection = new DashboardSection(title);
        addSection(dashboardSection);
        return dashboardSection;
    }

    /**
     * Adds the given section to this dashboard.
     *
     * @param section
     *            the widgets to add, not {@code null}
     */
    public void addSection(DashboardSection section) {
        doAddSection(section);
        updateClient();
    }

    @Override
    public List<DashboardWidget> getWidgets() {
        List<DashboardWidget> widgets = new ArrayList<>();
        childrenComponents.forEach(component -> {
            if (component instanceof DashboardSection section) {
                widgets.addAll(section.getWidgets());
            } else {
                widgets.add((DashboardWidget) component);
            }
        });
        return Collections.unmodifiableList(widgets);
    }

    @Override
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

    @Override
    public void addWidgetAtIndex(int index, DashboardWidget widget) {
        Objects.requireNonNull(widget, "Widget to add cannot be null.");
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a widget with a negative index.");
        }
        if (index > childrenComponents.size()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot add a widget with index %d when there are %d children components",
                    index, childrenComponents.size()));
        }
        doAddWidgetAtIndex(index, widget);
        updateClient();
    }

    @Override
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
        if (!toRemove.isEmpty()) {
            toRemove.forEach(this::doRemoveWidget);
            updateClient();
        }
    }

    /**
     * Removes the given section from this component.
     *
     * @param section
     *            the section to remove, not {@code null}
     * @throws IllegalArgumentException
     *             if the non {@code null} parent of the section is not this
     *             component
     */
    public void remove(DashboardSection section) {
        Objects.requireNonNull(section, "Section to remove cannot be null.");
        doRemoveSection(section);
        updateClient();
    }

    /**
     * Removes all widgets and sections from this component.
     */
    @Override
    public void removeAll() {
        if (getChildren().findAny().isEmpty()) {
            return;
        }
        doRemoveAll();
        updateClient();
    }

    /**
     * Returns the maximum column count of the dashboard.
     *
     * @return the maximum column count of the dashboard
     */
    public Integer getMaximumColumnCount() {
        String maxColCount = getStyle().get("--vaadin-dashboard-col-max-count");
        return maxColCount == null ? null : Integer.valueOf(maxColCount);
    }

    /**
     * Sets the maximum column count of the dashboard.
     *
     * @param maxColCount
     *            the new maximum column count. Pass in {@code null} to set the
     *            maximum column count back to the default value.
     */
    public void setMaximumColumnCount(Integer maxColCount) {
        getStyle().set("--vaadin-dashboard-col-max-count",
                maxColCount == null ? null : String.valueOf(maxColCount));
    }

    /**
     * Returns the minimum column width of the dashboard.
     *
     * @return the minimum column width of the dashboard
     */
    public String getMinimumColumnWidth() {
        return getStyle().get("--vaadin-dashboard-col-min-width");
    }

    /**
     * Sets the minimum column width of the dashboard.
     *
     * @param minColWidth
     *            the new minimum column width. Pass in {@code null} to set the
     *            minimum column width back to the default value.
     */
    public void setMinimumColumnWidth(String minColWidth) {
        getStyle().set("--vaadin-dashboard-col-min-width", minColWidth);
    }

    /**
     * Returns the maximum column width of the dashboard.
     *
     * @return the maximum column width of the dashboard
     */
    public String getMaximumColumnWidth() {
        return getStyle().get("--vaadin-dashboard-col-max-width");
    }

    /**
     * Sets the maximum column width of the dashboard.
     *
     * @param maxColWidth
     *            the new maximum column width. Pass in {@code null} to set the
     *            maximum column width back to the default value.
     */
    public void setMaximumColumnWidth(String maxColWidth) {
        getStyle().set("--vaadin-dashboard-col-max-width", maxColWidth);
    }

    /**
     * Returns the gap between the cells of the dashboard.
     *
     * @return the gap between the cells of the dashboard
     */
    public String getGap() {
        return getStyle().get("--vaadin-dashboard-gap");
    }

    /**
     * Sets the gap between the cells of the dashboard.
     *
     * @param gap
     *            the new gap. Pass in {@code null} to set the gap back to the
     *            default value.
     */
    public void setGap(String gap) {
        getStyle().set("--vaadin-dashboard-gap", gap);
    }

    @Override
    public Stream<Component> getChildren() {
        return childrenComponents.stream();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachRenderer();
        doUpdateClient();
    }

    void updateClient() {
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
        childDetachHandler.refreshListeners();
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
        for (Component component : childrenComponents) {
            JsonObject jsonItem;
            if (component instanceof DashboardSection section) {
                jsonItem = getSectionJsonItem(section);
            } else {
                jsonItem = getWidgetJsonItem((DashboardWidget) component);
            }
            jsonItems.set(jsonItems.length(), jsonItem);
        }
        return jsonItems;
    }

    private JsonObject getSectionJsonItem(DashboardSection section) {
        JsonObject sectionJsonItem = Json.createObject();
        if (section.getTitle() != null) {
            sectionJsonItem.put("title", section.getTitle());
        }
        JsonArray sectionItems = Json.createArray();
        section.getWidgets().forEach(widget -> {
            JsonObject sectionItem = getWidgetJsonItem(widget);
            sectionItems.set(sectionItems.length(), sectionItem);
        });
        sectionJsonItem.put("items", sectionItems);
        return sectionJsonItem;
    }

    private JsonObject getWidgetJsonItem(DashboardWidget widget) {
        JsonObject widgetJsonItem = Json.createObject();
        widgetJsonItem.put("nodeid", getComponentNodeId(widget));
        widgetJsonItem.put("colspan", widget.getColspan());
        return widgetJsonItem;
    }

    private int getComponentNodeId(Component component) {
        return component.getElement().getNode().getId();
    }

    private void doRemoveAll() {
        new ArrayList<>(childrenComponents).forEach(child -> {
            if (child instanceof DashboardSection section) {
                doRemoveSection(section);
            } else {
                doRemoveWidget((DashboardWidget) child);
            }
        });
    }

    private void doRemoveWidget(DashboardWidget widget) {
        getElement().removeChild(widget.getElement());
        childrenComponents.remove(widget);
    }

    private void doAddWidgetAtIndex(int index, DashboardWidget widget) {
        getElement().appendChild(widget.getElement());
        childrenComponents.add(index, widget);
    }

    private void doAddWidget(DashboardWidget widget) {
        getElement().appendChild(widget.getElement());
        childrenComponents.add(widget);
    }

    private void doAddSection(DashboardSection section) {
        getElement().appendVirtualChild(section.getElement());
        childrenComponents.add(section);
    }

    private void doRemoveSection(DashboardSection section) {
        getElement().removeVirtualChild(section.getElement());
        childrenComponents.remove(section);
    }

    private DashboardChildDetachHandler getChildDetachHandler() {
        return new DashboardChildDetachHandler() {
            @Override
            void removeChild(Component child) {
                childrenComponents.remove(child);
                updateClient();
            }

            @Override
            Collection<Component> getDirectChildren() {
                return Dashboard.this.getChildren().toList();
            }
        };
    }
}

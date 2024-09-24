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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

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
public class Dashboard extends Component implements HasWidgets, HasSize {

    private final List<Component> childrenComponents = new ArrayList<>();

    private final DashboardChildDetachHandler childDetachHandler;

    private boolean pendingUpdate = false;

    /**
     * Creates an empty dashboard.
     */
    public Dashboard() {
        childDetachHandler = getChildDetachHandler();
        initItemMovedClientEventListener();
        initItemResizedClientEventListener();
        initItemRemovedClientEventListener();
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
     * Returns the minimum row height of the dashboard.
     *
     * @return the minimum row height of the dashboard
     */
    public String getMinimumRowHeight() {
        return getStyle().get("--vaadin-dashboard-row-min-height");
    }

    /**
     * Sets the minimum row height of the dashboard.
     *
     * @param minRowHeight
     *            the new minimum row height. Pass in {@code null} to set the
     *            minimum row height back to the default value.
     */
    public void setMinimumRowHeight(String minRowHeight) {
        getStyle().set("--vaadin-dashboard-row-min-height", minRowHeight);
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

    /**
     * Sets the option to make the dashboard editable.
     *
     * @param editable
     *            whether to set the dashboard editable
     */
    public void setEditable(boolean editable) {
        getElement().setProperty("editable", editable);
    }

    /**
     * Returns whether the dashboard is editable.
     *
     * @return whether to set the dashboard editable
     */
    public boolean isEditable() {
        return getElement().getProperty("editable", false);
    }

    /**
     * Adds an item moved listener to this dashboard.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addItemMovedListener(
            ComponentEventListener<DashboardItemMovedEvent> listener) {
        return addListener(DashboardItemMovedEvent.class, listener);
    }

    /**
     * Adds an item resized listener to this dashboard.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addItemResizedListener(
            ComponentEventListener<DashboardItemResizedEvent> listener) {
        return addListener(DashboardItemResizedEvent.class, listener);
    }

    /**
     * Adds an item removed listener to this dashboard.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addItemRemovedListener(
            ComponentEventListener<DashboardItemRemovedEvent> listener) {
        return addListener(DashboardItemRemovedEvent.class, listener);
    }

    @Override
    public Stream<Component> getChildren() {
        return childrenComponents.stream();
    }

    /**
     * @throws UnsupportedOperationException
     *             Dashboard does not support setting visibility
     */
    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException(
                "Dashboard does not support setting visibility");
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getElement().executeJs(
                "Vaadin.FlowComponentHost.patchVirtualContainer(this);");
        customizeItemMovedEvent();
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
        updateClientItems();
    }

    private void updateClientItems() {
        final AtomicInteger itemIndex = new AtomicInteger();
        List<String> itemRepresentations = new ArrayList<>();
        List<Component> flatOrderedComponents = new ArrayList<>();
        for (Component component : childrenComponents) {
            flatOrderedComponents.add(component);
            String itemRepresentation;
            if (component instanceof DashboardSection section) {
                flatOrderedComponents.addAll(section.getWidgets());
                List<DashboardWidget> sectionWidgets = section.getWidgets();
                int sectionIndex = itemIndex.getAndIncrement();
                String sectionWidgetsRepresentation = sectionWidgets.stream()
                        .map(widget -> getWidgetRepresentation(widget,
                                itemIndex.getAndIncrement()))
                        .collect(Collectors.joining(","));
                itemRepresentation = "{ component: $%d, items: [ %s ], nodeid: %d }"
                        .formatted(sectionIndex, sectionWidgetsRepresentation,
                                section.getElement().getNode().getId());
            } else {
                itemRepresentation = getWidgetRepresentation(
                        (DashboardWidget) component,
                        itemIndex.getAndIncrement());
            }
            itemRepresentations.add(itemRepresentation);
        }
        String updateItemsSnippet = "this.items = [ %s ];"
                .formatted(String.join(",", itemRepresentations));
        getElement().executeJs(updateItemsSnippet,
                flatOrderedComponents.toArray(Component[]::new));
    }

    private static String getWidgetRepresentation(DashboardWidget widget,
            int itemIndex) {
        return "{ component: $%d, colspan: %d, rowspan: %d, nodeid: %d  }"
                .formatted(itemIndex, widget.getColspan(), widget.getRowspan(),
                        widget.getElement().getNode().getId());
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
        getElement().appendChild(section.getElement());
        childrenComponents.add(section);
    }

    private void doRemoveSection(DashboardSection section) {
        getElement().removeChild(section.getElement());
        childrenComponents.remove(section);
    }

    private DashboardChildDetachHandler getChildDetachHandler() {
        return new DashboardChildDetachHandler(this) {
            @Override
            void removeChild(Component child) {
                childrenComponents.remove(child);
                updateClient();
            }
        };
    }

    private void initItemMovedClientEventListener() {
        String itemKey = "event.detail.item";
        String itemsKey = "event.detail.items";
        String sectionKey = "event.detail.section";
        getElement().addEventListener("dashboard-item-moved-flow", e -> {
            if (!isEditable()) {
                return;
            }
            handleItemMovedClientEvent(e, itemKey, itemsKey, sectionKey);
            updateClient();
        }).addEventData(itemKey).addEventData(itemsKey)
                .addEventData(sectionKey);
    }

    private void handleItemMovedClientEvent(DomEvent e, String itemKey,
            String itemsKey, String sectionKey) {
        int itemNodeId = (int) e.getEventData().getNumber(itemKey);
        JsonArray itemsNodeIds = e.getEventData().getArray(itemsKey);
        Integer sectionNodeId = e.getEventData().hasKey(sectionKey)
                ? (int) e.getEventData().getNumber(sectionKey)
                : null;
        DashboardSection section = null;
        List<Component> reorderedItems;
        if (sectionNodeId == null) {
            reorderedItems = getReorderedItemsList(itemsNodeIds, this);
            childrenComponents.clear();
            childrenComponents.addAll(reorderedItems);
        } else {
            section = getChildren()
                    .filter(child -> sectionNodeId
                            .equals(child.getElement().getNode().getId()))
                    .map(DashboardSection.class::cast).findAny().orElseThrow();
            reorderedItems = getReorderedItemsList(
                    getSectionItems(itemsNodeIds, sectionNodeId), section);
            section.removeAll();
            reorderedItems.stream().map(DashboardWidget.class::cast)
                    .forEach(section::add);
        }
        Component movedItem = reorderedItems.stream().filter(
                item -> itemNodeId == item.getElement().getNode().getId())
                .findAny().orElseThrow();
        fireEvent(new DashboardItemMovedEvent(this, true, movedItem,
                getChildren().toList(), section));
    }

    private void initItemResizedClientEventListener() {
        String nodeIdKey = "event.detail.item.nodeid";
        String colspanKey = "event.detail.item.colspan";
        String rowspanKey = "event.detail.item.rowspan";
        getElement().addEventListener("dashboard-item-resized", e -> {
            if (!isEditable()) {
                return;
            }
            handleItemResizedClientEvent(e, nodeIdKey, colspanKey, rowspanKey);
            updateClient();
        }).addEventData(nodeIdKey).addEventData(colspanKey)
                .addEventData(rowspanKey);
    }

    private void handleItemResizedClientEvent(DomEvent e, String nodeIdKey,
            String colspanKey, String rowspanKey) {
        int nodeId = (int) e.getEventData().getNumber(nodeIdKey);
        int colspan = (int) e.getEventData().getNumber(colspanKey);
        int rowspan = (int) e.getEventData().getNumber(rowspanKey);
        DashboardWidget resizedWidget = getWidgets().stream()
                .filter(child -> nodeId == child.getElement().getNode().getId())
                .findAny().orElseThrow();
        resizedWidget.setRowspan(rowspan);
        resizedWidget.setColspan(colspan);
        fireEvent(new DashboardItemResizedEvent(this, true, resizedWidget,
                getChildren().toList()));
    }

    private void initItemRemovedClientEventListener() {
        String nodeIdKey = "event.detail.item.nodeid";
        DomListenerRegistration registration = getElement()
                .addEventListener("dashboard-item-removed", e -> {
                    if (!isEditable()) {
                        return;
                    }
                    handleItemRemovedClientEvent(e, nodeIdKey);
                    updateClient();
                });
        registration.addEventData(nodeIdKey);
    }

    private void handleItemRemovedClientEvent(DomEvent e, String nodeIdKey) {
        int nodeId = (int) e.getEventData().getNumber(nodeIdKey);
        Component removedItem = getRemovedItem(nodeId);
        removedItem.removeFromParent();
        fireEvent(new DashboardItemRemovedEvent(this, true, removedItem,
                getChildren().toList()));
    }

    private Component getRemovedItem(int nodeId) {
        return getChildren().map(item -> {
            if (nodeId == item.getElement().getNode().getId()) {
                return item;
            }
            if (item instanceof DashboardSection section) {
                return section.getWidgets().stream()
                        .filter(sectionItem -> nodeId == sectionItem
                                .getElement().getNode().getId())
                        .findAny().orElse(null);
            }
            return null;
        }).filter(Objects::nonNull).findAny().orElseThrow();
    }

    private void customizeItemMovedEvent() {
        getElement().executeJs(
                """
                        this.addEventListener('dashboard-item-moved', (e) => {
                          function mapItems(items) {
                            return items.map(({nodeid, items}) => ({
                              nodeid,
                              ...(items && { items: mapItems(items) })
                            }));
                          }
                          const flowItemMovedEvent = new CustomEvent('dashboard-item-moved-flow', {
                            detail: {
                              item: e.detail.item.nodeid,
                              items: mapItems(e.detail.items),
                              section: e.detail.section?.nodeid
                            }
                          });
                          this.dispatchEvent(flowItemMovedEvent);
                        });""");
    }

    private static List<Component> getReorderedItemsList(
            JsonArray reorderedItemsFromClient,
            Component reorderedItemsParent) {
        Objects.requireNonNull(reorderedItemsFromClient);
        Map<Integer, Component> nodeIdToItems = reorderedItemsParent
                .getChildren()
                .collect(Collectors.toMap(
                        item -> item.getElement().getNode().getId(),
                        Function.identity()));
        List<Component> items = new ArrayList<>();
        for (int index = 0; index < reorderedItemsFromClient
                .length(); index++) {
            int nodeIdFromClient = (int) ((JsonObject) reorderedItemsFromClient
                    .get(index)).getNumber("nodeid");
            items.add(nodeIdToItems.get(nodeIdFromClient));
        }
        return items;
    }

    private static JsonArray getSectionItems(JsonArray items,
            int sectionNodeId) {
        for (int rootLevelIdx = 0; rootLevelIdx < items
                .length(); rootLevelIdx++) {
            JsonObject item = items.get(rootLevelIdx);
            int itemNodeId = (int) item.getNumber("nodeid");
            if (sectionNodeId == itemNodeId) {
                JsonObject sectionObj = items.get(rootLevelIdx);
                return sectionObj.getArray("items");
            }
        }
        return null;
    }
}

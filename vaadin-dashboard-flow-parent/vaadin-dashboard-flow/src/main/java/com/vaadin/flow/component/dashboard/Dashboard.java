/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * Dashboard is a responsive layout component that allows users to organize
 * widgets either directly within the dashboard or optionally group them into
 * sections. The component supports customizable layout options like maximum
 * column count.
 * <p>
 * Internationalization (i18n) is supported through {@link DashboardI18n},
 * allowing customization of accessible names for the dashboard controls. The
 * i18n object can be set using {@link #setI18n(DashboardI18n)}.
 *
 * @see DashboardWidget
 * @see DashboardSection
 * @see DashboardI18n
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-dashboard")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/dashboard/src/vaadin-dashboard.js")
@JsModule("./flow-component-renderer.js")
@NpmPackage(value = "@vaadin/dashboard", version = "24.8.0-alpha18")
public class Dashboard extends Component implements HasWidgets, HasSize {

    private static final ThreadLocal<Boolean> suppressClientUpdates = ThreadLocal
            .withInitial(() -> false);

    private final List<Component> childrenComponents = new ArrayList<>();

    private final DashboardChildDetachHandler childDetachHandler;

    private DashboardI18n i18n;

    private boolean pendingUpdate = false;

    private boolean featureFlagEnabled;

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

    /**
     * Returns a flattened list of all the widgets in this dashboard. This
     * includes the nested widgets in sections.
     *
     * @return The flattened list of all widgets in this dashboard
     */
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
    public void add(Collection<DashboardWidget> widgets) {
        Objects.requireNonNull(widgets, "Widgets to add cannot be null.");
        widgets.forEach(widget -> Objects.requireNonNull(widget,
                "Widget to add cannot be null."));
        widgets.forEach(this::doAddWidget);
        updateClient();
    }

    /**
     * Adds the given widget as child of this dashboard at the specific index.
     * <p>
     * The index specifies the intended position within the root item list
     * returned by {@code getChildren()}. It should not be used with
     * {@code getWidgets()} since the position in the flattened widget list
     * returned by {@code getWidgets()} might not match the intended position.
     * <p>
     * In case the specified widget has already been added to another parent, it
     * will be removed from there and added to this one.
     *
     * @param index
     *            the index, where the widget will be added. The index must be
     *            non-negative and may not exceed the children count
     * @param widget
     *            the widget to add, not {@code null}
     *
     * @see #getWidgets()
     * @see #getChildren()
     */
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
    public void remove(Collection<DashboardWidget> widgets) {
        Objects.requireNonNull(widgets, "Widgets to remove cannot be null.");
        List<DashboardWidget> toRemove = new ArrayList<>(widgets.size());
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
            toRemove.forEach(this::doRemoveItem);
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
        doRemoveItem(section);
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
     * Returns the gap of the dashboard. This value adjusts the gap between
     * elements within the dashboard.
     *
     * @return the gap of the dashboard
     */
    public String getGap() {
        return getStyle().get("--vaadin-dashboard-gap");
    }

    /**
     * Sets the gap of the dashboard. This value adjusts the gap between
     * elements within the dashboard.
     *
     * @param gap
     *            the new gap. Pass in {@code null} to set the gap back to the
     *            default value.
     */
    public void setGap(String gap) {
        getStyle().set("--vaadin-dashboard-gap", gap);
    }

    /**
     * Returns the padding of the dashboard. This value adjusts the space around
     * the outer edges of the dashboard.
     *
     * @return the padding of the dashboard
     */
    public String getPadding() {
        return getStyle().get("--vaadin-dashboard-padding");
    }

    /**
     * Sets the padding of the dashboard. This value adjusts the space around
     * the outer edges of the dashboard.
     *
     * @param padding
     *            the new padding. Pass in {@code null} to set the padding back
     *            to the default value.
     */
    public void setPadding(String padding) {
        getStyle().set("--vaadin-dashboard-padding", padding);
    }

    /**
     * Sets the dashboard editable.
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
     * @return whether the dashboard is editable
     */
    public boolean isEditable() {
        return getElement().getProperty("editable", false);
    }

    /**
     * Sets the dashboard layout dense.
     *
     * @param dense
     *            whether to set the dashboard layout dense
     */
    public void setDenseLayout(boolean dense) {
        getElement().setProperty("denseLayout", dense);
    }

    /**
     * Returns whether the dashboard layout is dense.
     *
     * @return whether the dashboard layout is dense
     */
    public boolean isDenseLayout() {
        return getElement().getProperty("denseLayout", false);
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

    /**
     * Adds an item selected change listener to this dashboard.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addItemSelectedChangedListener(
            ComponentEventListener<DashboardItemSelectedChangedEvent> listener) {
        return addListener(DashboardItemSelectedChangedEvent.class, listener);
    }

    /**
     * Adds an item move mode change listener to this dashboard.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addItemMoveModeChangedListener(
            ComponentEventListener<DashboardItemMoveModeChangedEvent> listener) {
        return addListener(DashboardItemMoveModeChangedEvent.class, listener);
    }

    /**
     * Adds an item resize mode change listener to this dashboard.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addItemResizeModeChangedListener(
            ComponentEventListener<DashboardItemResizeModeChangedEvent> listener) {
        return addListener(DashboardItemResizeModeChangedEvent.class, listener);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(DashboardI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public DashboardI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(DashboardI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> {
                    if (i18n.equals(this.i18n)) {
                        setI18nWithJS();
                    }
                }));
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

    /**
     * Sets the root heading level used by sections and widgets, which controls
     * their <code>aria-level</code> attributes on title elements. The nested
     * widgets will have their {@code aria-level} one higher than the root
     * heading level.
     * <p>
     * For example, if root heading level is set to {@code 1}:
     * <ul>
     * <li>Sections and non-nested widgets will have {@code aria-level="1"}</li>
     * <li>Nested widgets will have {@code aria-level="2"}</li>
     * </ul>
     * Setting it {@code null} resets it to the default value of {@code 2}.
     *
     * @param rootHeadingLevel
     *            the root heading level property, {@code null} to remove
     */
    public void setRootHeadingLevel(Integer rootHeadingLevel) {
        if (rootHeadingLevel == null) {
            getElement().removeProperty("rootHeadingLevel");
        } else {
            getElement().setProperty("rootHeadingLevel", rootHeadingLevel);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag();
        getElement().executeJs(
                "Vaadin.FlowComponentHost.patchVirtualContainer(this);");
        customizeItemMovedEvent();
        doUpdateClient();
    }

    Component getItem(int nodeId) {
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

    private void withoutClientUpdate(Runnable action) {
        suppressClientUpdates.set(true);
        try {
            action.run();
        } finally {
            suppressClientUpdates.remove();
        }
    }

    void updateClient() {
        if (suppressClientUpdates.get() || pendingUpdate) {
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
                itemRepresentation = "{ component: $%d, items: [ %s ], id: %d }"
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

    private void setI18nWithJS() {
        JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(i18n);

        // Remove properties with null values to prevent errors in web
        // component
        removeNullValuesFromJsonObject(i18nJson);

        // Assign new I18N object to WC, by merging the existing
        // WC I18N, and the values from the new DashboardI18n instance,
        // into an empty object
        getElement().executeJs("this.i18n = Object.assign({}, this.i18n, $0);",
                i18nJson);
    }

    private void removeNullValuesFromJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keys()) {
            if (jsonObject.get(key).getType() == JsonType.NULL) {
                jsonObject.remove(key);
            }
        }
    }

    private static String getWidgetRepresentation(DashboardWidget widget,
            int itemIndex) {
        return "{ component: $%d, colspan: %d, rowspan: %d, id: %d  }"
                .formatted(itemIndex, widget.getColspan(), widget.getRowspan(),
                        widget.getElement().getNode().getId());
    }

    private void doRemoveAll() {
        new ArrayList<>(childrenComponents).forEach(this::doRemoveItem);
    }

    private void doRemoveItem(Component item) {
        getElement().removeChild(item.getElement());
        childrenComponents.remove(item);
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
            section.reorderWidgets(reorderedItems.stream()
                    .map(DashboardWidget.class::cast).toList());
        }
        Component movedItem = reorderedItems.stream().filter(
                item -> itemNodeId == item.getElement().getNode().getId())
                .findAny().orElseThrow();
        fireEvent(new DashboardItemMovedEvent(this, true, movedItem,
                getChildren().toList(), section));
    }

    private void initItemResizedClientEventListener() {
        String idKey = "event.detail.item.id";
        String colspanKey = "event.detail.item.colspan";
        String rowspanKey = "event.detail.item.rowspan";
        getElement().addEventListener("dashboard-item-resized", e -> {
            if (!isEditable()) {
                return;
            }
            handleItemResizedClientEvent(e, idKey, colspanKey, rowspanKey);
        }).addEventData(idKey).addEventData(colspanKey)
                .addEventData(rowspanKey);
    }

    private void handleItemResizedClientEvent(DomEvent e, String idKey,
            String colspanKey, String rowspanKey) {
        int nodeId = (int) e.getEventData().getNumber(idKey);
        int colspan = (int) e.getEventData().getNumber(colspanKey);
        int rowspan = (int) e.getEventData().getNumber(rowspanKey);
        DashboardWidget resizedWidget = getWidgets().stream()
                .filter(child -> nodeId == child.getElement().getNode().getId())
                .findAny().orElseThrow();
        withoutClientUpdate(() -> {
            resizedWidget.setColspan(colspan);
            resizedWidget.setRowspan(rowspan);
        });
        fireEvent(new DashboardItemResizedEvent(this, true, resizedWidget,
                getChildren().toList()));
    }

    private void initItemRemovedClientEventListener() {
        String idKey = "event.detail.item.id";
        getElement().addEventListener("dashboard-item-removed", e -> {
            if (!isEditable()) {
                return;
            }
            handleItemRemovedClientEvent(e, idKey);
        }).addEventData(idKey);
    }

    private void handleItemRemovedClientEvent(DomEvent e, String idKey) {
        int nodeId = (int) e.getEventData().getNumber(idKey);
        Component removedItem = getItem(nodeId);
        withoutClientUpdate(removedItem::removeFromParent);
        fireEvent(new DashboardItemRemovedEvent(this, true, removedItem,
                getChildren().toList()));
    }

    private void customizeItemMovedEvent() {
        getElement().executeJs(
                """
                        this.addEventListener('dashboard-item-moved', (e) => {
                          function mapItems(items) {
                            return items.map(({id, items}) => ({
                              id,
                              ...(items && { items: mapItems(items) })
                            }));
                          }
                          const flowItemMovedEvent = new CustomEvent('dashboard-item-moved-flow', {
                            detail: {
                              item: e.detail.item.id,
                              items: mapItems(e.detail.items),
                              section: e.detail.section?.id
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
                    .get(index)).getNumber("id");
            items.add(nodeIdToItems.get(nodeIdFromClient));
        }
        return items;
    }

    private static JsonArray getSectionItems(JsonArray items,
            int sectionNodeId) {
        for (int rootLevelIdx = 0; rootLevelIdx < items
                .length(); rootLevelIdx++) {
            JsonObject item = items.get(rootLevelIdx);
            int itemNodeId = (int) item.getNumber("id");
            if (sectionNodeId == itemNodeId) {
                JsonObject sectionObj = items.get(rootLevelIdx);
                return sectionObj.getArray("items");
            }
        }
        return null;
    }

    /**
     * Checks whether the Dashboard component feature flag is active. Succeeds
     * if the flag is enabled, and throws otherwise.
     *
     * @throws ExperimentalFeatureException
     *             when the {@link FeatureFlags#DASHBOARD_COMPONENT} feature is
     *             not enabled
     */
    private void checkFeatureFlag() {
        boolean enabled = featureFlagEnabled || getFeatureFlags()
                .isEnabled(FeatureFlags.DASHBOARD_COMPONENT);
        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * Gets the feature flags for the current UI.
     * <p>
     * Not private in order to support mocking
     *
     * @return the current set of feature flags
     */
    FeatureFlags getFeatureFlags() {
        return FeatureFlags
                .get(UI.getCurrent().getSession().getService().getContext());
    }

    /**
     * Only for test use.
     */
    void setFeatureFlagEnabled(boolean featureFlagEnabled) {
        this.featureFlagEnabled = featureFlagEnabled;
    }

    /**
     * The internationalization properties for {@link Dashboard}.
     */
    public static class DashboardI18n implements Serializable {

        private String selectSection;
        private String selectWidget;
        private String remove;
        private String resize;
        private String resizeApply;
        private String resizeShrinkWidth;
        private String resizeGrowWidth;
        private String resizeShrinkHeight;
        private String resizeGrowHeight;
        private String move;
        private String moveApply;
        private String moveForward;
        private String moveBackward;

        /**
         * Gets the accessible name of section focus buttons
         *
         * @return the accessible name of section focus button, or {@code null}
         *         if not set
         */
        public String getSelectSection() {
            return selectSection;
        }

        /**
         * Sets the accessible name of section focus buttons
         *
         * @param selectSection
         *            the accessible name of section focus button to set
         * @return this instance for method chaining
         */
        public DashboardI18n setSelectSection(String selectSection) {
            this.selectSection = selectSection;
            return this;
        }

        /**
         * Gets the accessible name of widget focus buttons
         *
         * @return the accessible name of widget focus button, or {@code null}
         *         if not set
         */
        public String getSelectWidget() {
            return selectWidget;
        }

        /**
         * Sets the accessible name of widget focus buttons
         *
         * @param selectWidget
         *            the accessible name of widget focus button to set
         * @return this instance for method chaining
         */
        public DashboardI18n setSelectWidget(String selectWidget) {
            this.selectWidget = selectWidget;
            return this;
        }

        /**
         * Gets the accessible name of dashboard item remove buttons
         *
         * @return the accessible name of dashboard item remove button, or
         *         {@code null} if not set
         */
        public String getRemove() {
            return remove;
        }

        /**
         * Sets the accessible name of dashboard item remove buttons
         *
         * @param remove
         *            the accessible name of dashboard item remove button to set
         * @return this instance for method chaining
         */
        public DashboardI18n setRemove(String remove) {
            this.remove = remove;
            return this;
        }

        /**
         * Gets the accessible name of widget resize handles
         *
         * @return the accessible name of widget resize handle, or {@code null}
         *         if not set
         */
        public String getResize() {
            return resize;
        }

        /**
         * Sets the accessible name of widget resize handles
         *
         * @param resize
         *            the accessible name of widget resize handle to set
         * @return this instance for method chaining
         */
        public DashboardI18n setResize(String resize) {
            this.resize = resize;
            return this;
        }

        /**
         * Gets the accessible name of widget resize apply buttons
         *
         * @return the accessible name of widget resize apply button, or
         *         {@code null} if not set
         */
        public String getResizeApply() {
            return resizeApply;
        }

        /**
         * Sets the accessible name of widget resize apply buttons
         *
         * @param resizeApply
         *            the accessible name of widget resize apply button to set
         * @return this instance for method chaining
         */
        public DashboardI18n setResizeApply(String resizeApply) {
            this.resizeApply = resizeApply;
            return this;
        }

        /**
         * Gets the accessible name of widget resize shrink width buttons
         *
         * @return the accessible name of widget resize shrink width button, or
         *         {@code null} if not set
         */
        public String getResizeShrinkWidth() {
            return resizeShrinkWidth;
        }

        /**
         * Sets the accessible name of widget resize shrink width buttons
         *
         * @param resizeShrinkWidth
         *            the accessible name of widget resize shrink width button
         *            to set
         * @return this instance for method chaining
         */
        public DashboardI18n setResizeShrinkWidth(String resizeShrinkWidth) {
            this.resizeShrinkWidth = resizeShrinkWidth;
            return this;
        }

        /**
         * Gets the accessible name of widget resize grow width buttons
         *
         * @return the accessible name of widget resize grow width button, or
         *         {@code null} if not set
         */
        public String getResizeGrowWidth() {
            return resizeGrowWidth;
        }

        /**
         * Sets the accessible name of widget resize grow width buttons
         *
         * @param resizeGrowWidth
         *            the accessible name of widget resize grow width button to
         *            set
         * @return this instance for method chaining
         */
        public DashboardI18n setResizeGrowWidth(String resizeGrowWidth) {
            this.resizeGrowWidth = resizeGrowWidth;
            return this;
        }

        /**
         * Gets the accessible name of widget resize shrink height buttons
         *
         * @return the accessible name of widget resize shrink height button, or
         *         {@code null} if not set
         */
        public String getResizeShrinkHeight() {
            return resizeShrinkHeight;
        }

        /**
         * Sets the accessible name of widget resize shrink height buttons
         *
         * @param resizeShrinkHeight
         *            the accessible name of widget resize shrink height button
         *            to set
         * @return this instance for method chaining
         */
        public DashboardI18n setResizeShrinkHeight(String resizeShrinkHeight) {
            this.resizeShrinkHeight = resizeShrinkHeight;
            return this;
        }

        /**
         * Gets the accessible name of widget resize grow height buttons
         *
         * @return the accessible name of widget resize grow height button, or
         *         {@code null} if not set
         */
        public String getResizeGrowHeight() {
            return resizeGrowHeight;
        }

        /**
         * Sets the accessible name of widget resize grow height buttons
         *
         * @param resizeGrowHeight
         *            the accessible name of widget resize grow height button to
         *            set
         * @return this instance for method chaining
         */
        public DashboardI18n setResizeGrowHeight(String resizeGrowHeight) {
            this.resizeGrowHeight = resizeGrowHeight;
            return this;
        }

        /**
         * Gets the accessible name of dashboard item drag handles
         *
         * @return the accessible name of dashboard item drag handle, or
         *         {@code null} if not set
         */
        public String getMove() {
            return move;
        }

        /**
         * Sets the accessible name of dashboard item drag handles
         *
         * @param move
         *            the accessible name of dashboard item drag handle to set
         * @return this instance for method chaining
         */
        public DashboardI18n setMove(String move) {
            this.move = move;
            return this;
        }

        /**
         * Gets the accessible name of dashboard item move apply buttons
         *
         * @return the accessible name of dashboard item move apply button, or
         *         {@code null} if not set
         */
        public String getMoveApply() {
            return moveApply;
        }

        /**
         * Sets the accessible name of dashboard item move apply buttons
         *
         * @param moveApply
         *            the accessible name of dashboard item move apply button to
         *            set
         * @return this instance for method chaining
         */
        public DashboardI18n setMoveApply(String moveApply) {
            this.moveApply = moveApply;
            return this;
        }

        /**
         * Gets the accessible name of dashboard item move forward buttons
         *
         * @return the accessible name of dashboard item move forward button, or
         *         {@code null} if not set
         */
        public String getMoveForward() {
            return moveForward;
        }

        /**
         * Sets the accessible name of dashboard item move forward buttons
         *
         * @param moveForward
         *            the accessible name of dashboard item move forward button
         *            to set
         * @return this instance for method chaining
         */
        public DashboardI18n setMoveForward(String moveForward) {
            this.moveForward = moveForward;
            return this;
        }

        /**
         * Gets the accessible name of dashboard item move backward buttons
         *
         * @return the accessible name of dashboard item move backward button,
         *         or {@code null} if not set
         */
        public String getMoveBackward() {
            return moveBackward;
        }

        /**
         * Sets the accessible name of dashboard item move backward buttons
         *
         * @param moveBackward
         *            the accessible name of dashboard item move backward button
         *            to set
         * @return this instance for method chaining
         */
        public DashboardI18n setMoveBackward(String moveBackward) {
            this.moveBackward = moveBackward;
            return this;
        }
    }
}

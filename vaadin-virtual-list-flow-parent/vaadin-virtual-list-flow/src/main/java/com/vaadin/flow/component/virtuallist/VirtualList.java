/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.virtuallist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.virtuallist.paging.PagelessDataCommunicator;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.selection.SelectionModel.Single;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.selection.SingleSelectionEvent;
import com.vaadin.flow.data.selection.SingleSelectionListener;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;

/**
 * Virtual List allows you to render a long list of items inside a scrollable
 * container without sacrificing performance. Each item is rendered on the fly
 * as the user scrolls the list. To use the component, you need to assign it a
 * set of data items and a renderer that is used for rendering each individual
 * data item. The height of an item is determined by its content and can change
 * dynamically.
 * <p>
 * This component supports {@link DataProvider}s to load data asynchronously and
 * {@link Renderer}s to render the markup for each item.
 * <p>
 *
 * @author Vaadin Ltd.
 *
 * @param <T>
 *            the type of the items supported by the list
 */
@Tag("vaadin-virtual-list")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.6.0-rc1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/virtual-list", version = "24.6.0-rc1")
@JsModule("@vaadin/virtual-list/src/vaadin-virtual-list.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./virtualListConnector.js")
public class VirtualList<T> extends Component implements HasDataProvider<T>,
        HasStyle, HasSize, Focusable<VirtualList<T>> {

    private final class UpdateQueue implements Update {
        private transient List<Runnable> queue = new ArrayList<>();

        private UpdateQueue(int size) {
            enqueue("$connector.updateSize", size);
        }

        @Override
        public void set(int start, List<JsonValue> items) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()));
        }

        @Override
        public void clear(int start, int length) {
            enqueue("$connector.clear", start, length);
        }

        @Override
        public void commit(int updateId) {
            getDataCommunicator().confirmUpdate(updateId);
            queue.forEach(Runnable::run);
            queue.clear();
        }

        private void enqueue(String name, Serializable... arguments) {
            queue.add(() -> getElement().callJsFunction(name, arguments));
        }
    }

    private final ArrayUpdater arrayUpdater = new ArrayUpdater() {
        @Override
        public Update startUpdate(int sizeChange) {
            return new UpdateQueue(sizeChange);
        }

        @Override
        public void initialize() {
            initConnector();
        }
    };

    private Renderer<T> renderer;

    private SelectionMode selectionMode;
    private SelectionModel<VirtualList<T>, T> selectionModel;
    private SerializableFunction<T, String> itemAccessibleNameGenerator = item -> null;

    private final CompositeDataGenerator<T> dataGenerator = new CompositeDataGenerator<>();
    private final List<Registration> renderingRegistrations = new ArrayList<>();
    private transient T placeholderItem;

    private final DataCommunicator<T> dataCommunicator = new PagelessDataCommunicator<>(
            dataGenerator, arrayUpdater,
            data -> getElement().callJsFunction("$connector.updateData", data),
            getElement().getNode());

    /**
     * Creates an empty list.
     */
    public VirtualList() {
        setRenderer((ValueProvider<T, String>) String::valueOf);
        addAttachListener((e) -> this.setPlaceholderItem(this.placeholderItem));

        // Use NONE selection mode by default
        setSelectionMode(SelectionMode.NONE);

        initSelection();
    }

    private void initConnector() {
        getUI().orElseThrow(() -> new IllegalStateException(
                "Connector can only be initialized for an attached VirtualList"))
                .getPage().executeJs(
                        "window.Vaadin.Flow.virtualListConnector.initLazy($0)",
                        getElement());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initSelection() {
        // Generate "selected" property for selected items
        dataGenerator.addDataGenerator((item, jsonObject) -> {
            if (this.getSelectionModel().isSelected(item)) {
                jsonObject.put("selected", true);
            }
            var accessibleName = this.itemAccessibleNameGenerator.apply(item);
            if (accessibleName != null) {
                jsonObject.put("accessibleName", accessibleName);
            }
        });

        // Set up SingleSelectionEvent and MultiSelectionEvent listeners to
        // refresh the items in data communicator when selection changes. This
        // is to ensure the data generator labels selected items with "selected"
        // property on selection change.
        ComponentUtil.addListener(this, SingleSelectionEvent.class,
                (ComponentEventListener) ((ComponentEventListener<SingleSelectionEvent<VirtualList<T>, T>>) event -> {
                    Stream.of(event.getValue(), event.getOldValue())
                            .filter(Objects::nonNull)
                            .forEach(item -> getDataCommunicator()
                                    .refresh((T) item));
                }));

        ComponentUtil.addListener(this, MultiSelectionEvent.class,
                (ComponentEventListener) ((ComponentEventListener<MultiSelectionEvent<VirtualList<T>, T>>) event -> {
                    Stream.concat(event.getAddedSelection().stream(),
                            event.getRemovedSelection().stream())
                            .filter(Objects::nonNull)
                            .forEach(item -> getDataCommunicator()
                                    .refresh((T) item));
                }));

    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider, "The dataProvider cannot be null");
        getDataCommunicator().setDataProvider(dataProvider, null);
    }

    /**
     * Returns the data provider of this list.
     *
     * @return the data provider of this list, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() { // NOSONAR
        return getDataCommunicator().getDataProvider();
    }

    /**
     * Returns the data communicator of this list.
     *
     * @return the data communicator, not {@code null}
     */
    public DataCommunicator<T> getDataCommunicator() {
        return dataCommunicator;
    }

    /**
     * Sets a renderer for the items in the list, by using a
     * {@link ValueProvider}. The String returned by the provider is used to
     * render each item.
     *
     * @param valueProvider
     *            a provider for the label string for each item in the list, not
     *            <code>null</code>
     */
    public void setRenderer(ValueProvider<T, String> valueProvider) {
        Objects.requireNonNull(valueProvider,
                "The valueProvider must not be null");
        this.setRenderer(LitRenderer.<T> of("${item.label}")
                .withProperty("label", valueProvider));
    }

    /**
     * Sets a renderer for the items in the list.
     * <p>
     * When set, a same renderer is used for the placeholder item. See
     * {@link #setPlaceholderItem(Object)} for details.
     *
     * @param renderer
     *            a renderer for the items in the list, not <code>null</code>
     */
    public void setRenderer(Renderer<T> renderer) {
        Objects.requireNonNull(renderer, "The renderer must not be null");

        renderingRegistrations.forEach(Registration::remove);
        renderingRegistrations.clear();

        var rendering = renderer.render(getElement(),
                dataCommunicator.getKeyMapper());

        rendering.getDataGenerator().ifPresent(renderingDataGenerator -> {
            Registration renderingDataGeneratorRegistration = dataGenerator
                    .addDataGenerator(renderingDataGenerator);
            renderingRegistrations.add(renderingDataGeneratorRegistration);
        });

        renderingRegistrations.add(rendering.getRegistration());

        this.renderer = renderer;

        getDataCommunicator().reset();

        // Changing the renderer may also affect how the placeholder item is
        // processed by the data generator. Call setPlaceholderItem to make sure
        // the sent placeholder item is up to date.
        this.setPlaceholderItem(this.placeholderItem);
    }

    /**
     * Sets an item to be shown as placeholder in the list while the real data
     * in being fetched from the server.
     * <p>
     * Setting a placeholder item improves the user experience of the list while
     * scrolling, since the placeholder uses the same renderer set with
     * {@link #setRenderer(Renderer)}, maintaining the same height for
     * placeholders and actual items.
     * <p>
     * When no placeholder item is set (or when set to <code>null</code>), an
     * empty placeholder element is created.
     * <p>
     * Note: when using {@link ComponentRenderer}s, the component used for the
     * placeholder is statically stamped in the list. It can not be modified,
     * nor receives any events.
     *
     * @param placeholderItem
     *            the item used as placeholder in the list, while the real data
     *            is being fetched from the server
     */
    public void setPlaceholderItem(T placeholderItem) {
        this.placeholderItem = placeholderItem;

        runBeforeClientResponse(() -> {
            var json = Json.createObject();

            if (placeholderItem != null) {
                // Use the renderer's data generator to create the final
                // placeholder item which should be sent to the client. In the
                // case of ComponentRenderer, the generator also creates a
                // placeholder element which is automatically sent to the client
                // and the resulting json object will include its nodeid.
                dataGenerator.generateData(placeholderItem, json);
            }

            var appId = UI.getCurrent() != null
                    ? UI.getCurrent().getInternals().getAppId()
                    : "";
            getElement().callJsFunction("$connector.setPlaceholderItem", json,
                    appId);
        });
    }

    /**
     * Gets the placeholder item of this list, or <code>null</code> if none has
     * been set.
     *
     * @return the placeholder item
     */
    public T getPlaceholderItem() {
        return placeholderItem;
    }

    private void runBeforeClientResponse(Command command) {
        getElement().getNode()
                .runWhenAttached(ui -> ui.getInternals().getStateTree()
                        .beforeClientResponse(getElement().getNode(),
                                context -> command.execute()));
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        super.onEnabledStateChanged(enabled);

        /*
         * Rendered component's enabled state needs to be updated via rendering
         */
        setRenderer(renderer);
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setRequestedRange(int start, int length) {
        getDataCommunicator().setRequestedRange(start, length);
    }

    /**
     * Scrolls to the given row index. Scrolls so that the element is shown at
     * the start of the visible area whenever possible.
     * <p>
     * If the index parameter exceeds current item set size the grid will scroll
     * to the end.
     *
     * @param rowIndex
     *            zero based index of the item to scroll to in the current view.
     */
    public void scrollToIndex(int rowIndex) {
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, ctx -> getElement()
                        .executeJs("this.scrollToIndex($0)", rowIndex)));
    }

    /**
     * Scrolls to the first element.
     */
    public void scrollToStart() {
        scrollToIndex(0);
    }

    /**
     * Scrolls to the last element of the list.
     */
    public void scrollToEnd() {
        scrollToIndex(Integer.MAX_VALUE);
    }

    /**
     * Returns the selection mode for this virtual list.
     *
     * @return the selection mode, not null
     */
    public SelectionMode getSelectionMode() {
        assert selectionMode != null : "No selection mode set by "
                + getClass().getName() + " constructor";
        return selectionMode;
    }

    /**
     * Sets the virtual list's selection mode.
     *
     * @param selectionMode
     *            the selection mode to switch to, not {@code null}
     * @return the used selection model
     *
     * @see SelectionMode
     * @see VirtualListSingleSelectionModel
     * @see VirtualListMultiSelectionModel
     * @see VirtualListNoneSelectionModel
     */
    public SelectionModel<VirtualList<T>, T> setSelectionMode(
            SelectionMode selectionMode) {
        Objects.requireNonNull(selectionMode, "Selection mode cannot be null.");

        if (selectionMode == SelectionMode.SINGLE) {
            setSelectionModel(new VirtualListSingleSelectionModel<>(this),
                    selectionMode);
        } else if (selectionMode == SelectionMode.MULTI) {
            setSelectionModel(new VirtualListMultiSelectionModel<>(this),
                    selectionMode);
        } else {
            setSelectionModel(new VirtualListNoneSelectionModel<>(),
                    selectionMode);
        }
        return selectionModel;
    }

    private Set<T> getItemsFromKeys(JsonArray keys) {
        return JsonUtils.stream(keys).map(
                key -> getDataCommunicator().getKeyMapper().get(key.asString()))
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    @ClientCallable
    private void updateSelection(JsonArray addedKeys, JsonArray removedKeys) {
        var addedItems = getItemsFromKeys(addedKeys);
        var removedItems = getItemsFromKeys(removedKeys);

        if (selectionModel instanceof VirtualListSingleSelectionModel model) {
            model.setSelectedItem(
                    addedItems.isEmpty() ? null : addedItems.iterator().next());
        } else if (selectionModel instanceof VirtualListMultiSelectionModel model) {
            model.updateSelection(addedItems, removedItems);
        }
    }

    /**
     * Sets the selection model for the virtual list.
     * <p>
     * The default selection model is {@link VirtualListNoneSelectionModel}.
     *
     * @param model
     *            the selection model to use, not {@code null}
     * @param selectionMode
     *            the selection mode this selection model corresponds to, not
     *            {@code null}
     *
     * @see #setSelectionMode(SelectionMode)
     */
    private void setSelectionModel(SelectionModel<VirtualList<T>, T> model,
            SelectionMode selectionMode) {
        Objects.requireNonNull(model, "selection model cannot be null");
        Objects.requireNonNull(selectionMode, "selection mode cannot be null");

        selectionModel = model;
        this.selectionMode = selectionMode;
        
        getElement().removeProperty("__deselectionDisallowed");
        getElement().setProperty("selectionMode",
                SelectionMode.NONE.equals(selectionMode) ? null
                        : selectionMode.name().toLowerCase());

    }

    /**
     * Adds a selection listener to the current selection model.
     * <p>
     * This is a shorthand for
     * {@code virtualList.getSelectionModel().addSelectionListener()}. To get
     * more detailed selection events, use {@link #getSelectionModel()} and
     * either
     * {@link VirtualListSingleSelectionModel#addSelectionListener(SelectionListener)
     * or
     * {@link VirtualListMultiSelectionModel#addSelectionListener(SelectionListener)
     * depending on the used selection mode.
     *
     * @param listener
     *            the listener to add
     * @return a registration handle to remove the listener
     * @throws UnsupportedOperationException
     *             if {@link SelectionMode#NONE} is in use
     */
    public Registration addSelectionListener(
            SelectionListener<VirtualList<T>, T> listener) {
        return getSelectionModel().addSelectionListener(listener);
    }

    /**
     * Use this virtual list as a single select in {@link Binder}.
     * <p>
     * Throws {@link IllegalStateException} if the virtual list is not using a
     * {@link VirtualListSingleSelectionModel}.
     *
     * @return the single select wrapper that can be used in binder
     * @throws IllegalStateException
     *             if not using a single selection model
     */
    public SingleSelect<VirtualList<T>, T> asSingleSelect() {
        var model = getSelectionModel();
        if (!(model instanceof VirtualListSingleSelectionModel)) {
            throw new IllegalStateException(
                    "VirtualList is not in single select mode, "
                            + "it needs to be explicitly set to such with "
                            + "setSelectionMode(SelectionMode.SINGLE) before "
                            + "being able to use single selection features.");
        }
        return ((VirtualListSingleSelectionModel<T>) model).asSingleSelect();
    }

    /**
     * Use this virtual list as a multiselect in {@link Binder}.
     * <p>
     * Throws {@link IllegalStateException} if the virtual list is not using a
     * {@link VirtualListMultiSelectionModel}.
     *
     * @return the multiselect wrapper that can be used in binder
     * @throws IllegalStateException
     *             if not using a multiselection model
     */
    public MultiSelect<VirtualList<T>, T> asMultiSelect() {
        var model = getSelectionModel();
        if (!(model instanceof VirtualListMultiSelectionModel)) {
            throw new IllegalStateException(
                    "VirtualList is not in multi select mode, "
                            + "it needs to be explicitly set to such with "
                            + "setSelectionMode(SelectionMode.MULTI) before "
                            + "being able to use multi selection features.");
        }
        return ((VirtualListMultiSelectionModel<T>) model).asMultiSelect();
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @see #getSelectionModel()
     * @see VirtualListSingleSelectionModel#getSelectedItems()
     * @see VirtualListMultiSelectionModel#getSelectedItems()
     *
     * @return a set with the selected items, never <code>null</code>
     */
    public Set<T> getSelectedItems() {
        return getSelectionModel().getSelectedItems();
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @param item
     *            the item to select, not null
     *
     * @see #getSelectionModel()
     * @see VirtualListSingleSelectionModel#select(Object)
     * @see VirtualListMultiSelectionModel#select(Object)
     */
    public void select(T item) {
        getSelectionModel().select(item);
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @param item
     *            the item to deselect, not null
     *
     * @see #getSelectionModel()
     * @see VirtualListSingleSelectionModel#deselect(Object)
     * @see VirtualListMultiSelectionModel#deselect(Object)
     */
    public void deselect(T item) {
        getSelectionModel().deselect(item);
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @see #getSelectionModel()
     * @see VirtualListSingleSelectionModel#deselectAll()
     * @see VirtualListMultiSelectionModel#deselectAll()
     */
    public void deselectAll() {
        getSelectionModel().deselectAll();
    }

    /**
     * Returns the selection model for this virtual list.
     *
     * @return the selection model, not null
     */
    public SelectionModel<VirtualList<T>, T> getSelectionModel() {
        assert selectionModel != null : "No selection model set by "
                + getClass().getName() + " constructor";
        return selectionModel;
    }

    /**
     * A function that generates accessible names for virtual list items.
     *
     * @param itemAccessibleNameGenerator
     *            the item accessible name generator to set, not {@code null}
     * @throws NullPointerException
     *             if {@code itemAccessibleNameGenerator} is {@code null}
     */
    public void setItemAccessibleNameGenerator(
            SerializableFunction<T, String> itemAccessibleNameGenerator) {
        Objects.requireNonNull(itemAccessibleNameGenerator,
                "Part name generator can not be null");
        this.itemAccessibleNameGenerator = itemAccessibleNameGenerator;
        getDataCommunicator().reset();
    }

    /**
     * Gets the function that generates accessible names for virtual list items.
     *
     * @return the item accessible name generator
     */
    public SerializableFunction<T, String> getItemAccessibleNameGenerator() {
        return itemAccessibleNameGenerator;
    }

    /**
     * Selection mode representing the built-in selection models in virtual
     * list.
     * <p>
     * These enums can be used in
     * {@link VirtualList#setSelectionMode(SelectionMode)} to easily switch
     * between the built-in selection models.
     *
     * @see VirtualList#setSelectionMode(SelectionMode)
     * @see VirtualList#setSelectionModel(SelectionModel, SelectionMode)
     */
    public enum SelectionMode {

        /**
         * Single selection mode that maps to built-in {@link Single}.
         *
         * @see VirtualListSingleSelectionModel
         */
        SINGLE,

        /**
         * Multiselection mode that maps to built-in
         * {@link SelectionModel.Multi}.
         *
         * @see VirtualListMultiSelectionModelπ
         */
        MULTI,

        /**
         * Selection model that doesn't allow selection.
         *
         * @see VirtualListNoneSelectionModel
         */
        NONE;
    }
}

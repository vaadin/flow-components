package com.vaadin.flow.component.gridpro;

/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.grid.ColumnPathRenderer;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

@Tag("vaadin-grid-pro")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/grid-pro", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-grid-pro", version = "23.1.0-beta1")
@JsModule("@vaadin/grid-pro/src/vaadin-grid-pro.js")
@JsModule("@vaadin/grid-pro/src/vaadin-grid-pro-edit-column.js")
@JsModule("./gridProConnector.js")
/**
 * Grid Pro is an extension of the Grid component that provides inline editing
 * with full keyboard navigation.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the grid bean type
 *
 */
public class GridPro<E> extends Grid<E> {

    private Map<String, Column<E>> idToColumnMap = new HashMap<>();

    /**
     * Instantiates a new CrudGrid for the supplied bean type.
     *
     * @param beanType
     *            the beanType for the item
     *
     */
    public GridPro(Class<E> beanType) {
        super(beanType);
        setup();
    }

    /**
     * Creates a new instance, with page size of 50.
     */
    public GridPro() {
        super();
        setup();
    }

    /**
     * Creates a new instance, with the specified page size.
     * <p>
     * The page size influences the {@link Query#getLimit()} sent by the client,
     * but it's up to the webcomponent to determine the actual query limit,
     * based on the height of the component and scroll position. Usually the
     * limit is 3 times the page size (e.g. 150 items with a page size of 50).
     *
     * @param pageSize
     *            the page size. Must be greater than zero.
     */
    public GridPro(int pageSize) {
        super(pageSize);
        setup();
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        super.onEnabledStateChanged(enabled);
        getElement().setProperty("_editingDisabled", !enabled);
    }

    private void setup() {
        addItemPropertyChangedListener(e -> {
            EditColumn<E> column = (EditColumn<E>) this.idToColumnMap
                    .get(e.getPath());

            if (column.getEditorType().equals("custom")) {
                column.getItemUpdater().accept(e.getItem(), null);
            } else {
                column.getItemUpdater().accept(e.getItem(),
                        e.getSourceItem().get(e.getPath()).asString());
            }

            getDataProvider().refreshItem(e.getItem());
        });

        addCellEditStartedListener(e -> {
            EditColumn<E> column = (EditColumn<E>) this.idToColumnMap
                    .get(e.getPath());

            if (column.getEditorType().equals("custom")) {
                column.getEditorField()
                        .setValue(column.getValueProvider().apply(e.getItem()));
            }
        });
    }

    /**
     * Server-side component for the {@code <vaadin-grid-edit-column>} element.
     *
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link GridPro#removeColumn(Column)} to avoid sending extra data.
     *
     * @param <T>
     *            type of the underlying grid this column is compatible with
     */
    @Tag("vaadin-grid-pro-edit-column")
    @NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
    @JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
    public static class EditColumn<T> extends Column<T> {

        private ItemUpdater<T, String> itemUpdater;
        private AbstractField editorField;
        private ValueProvider<T, ?> valueProvider;

        /**
         * Constructs a new Column for use inside a Grid.
         *
         * @param grid
         *            the grid this column is attached to
         * @param columnId
         *            unique identifier of this column
         * @param renderer
         *            the renderer to use in this column, must not be
         *            {@code null}
         */
        public EditColumn(GridPro<T> grid, String columnId,
                Renderer<T> renderer) {
            super(grid, columnId, renderer);
        }

        /**
         * Sets the itemUpdater function that will be called on item changed.
         *
         * @param itemUpdater
         *            the callback function that is called when item is changed.
         *            It receives two arguments: item and newValue.
         * @return this column instance
         */
        protected EditColumn<T> setItemUpdater(
                ItemUpdater<T, String> itemUpdater) {
            this.itemUpdater = itemUpdater;
            return this;
        }

        /**
         * Gets the itemUpdater function that will be called on item changed.
         *
         * @return the instance of itemUpdater for this column
         */
        protected ItemUpdater<T, String> getItemUpdater() {
            return itemUpdater;
        }

        protected AbstractField getEditorField() {
            return editorField;
        }

        protected void setEditorField(AbstractField editorField) {
            this.editorField = editorField;
        }

        /**
         * Sets the type of the editor that is used for modifying cell value.
         *
         * @param type
         *            the type of the editor
         * @return this column instance
         *
         * @see EditorType
         */
        protected EditColumn<T> setEditorType(EditorType type) {
            getElement().setProperty("editorType",
                    type == null ? "text" : type.getTypeName());
            return this;
        }

        /**
         * Gets the type of the editor that is used for modifying cell value.
         *
         * @return the editor type
         */
        @Synchronize("editor-type-changed")
        protected String getEditorType() {
            return getElement().getProperty("editorType", "text");
        }

        /**
         * Sets the list of options that is used for select type of the editor.
         *
         * @param options
         *            the list of options
         * @return this column instance
         */
        protected EditColumn<T> setOptions(List<String> options) {
            getElement().setPropertyJson("editorOptions",
                    JsonSerializer.toJson(options));
            return this;
        }

        /**
         * Gets the list of options that is used for select type of the editor.
         *
         * @return the list of options
         */
        @Synchronize("editor-options-changed")
        protected List<String> getOptions() {
            return JsonSerializer.toObjects(String.class,
                    (JsonArray) getElement().getPropertyRaw("editorOptions"));
        }

        public ValueProvider<T, ?> getValueProvider() {
            return valueProvider;
        }

        public void setValueProvider(ValueProvider<T, ?> valueProvider) {
            this.valueProvider = valueProvider;
        }
    }

    /**
     * Adds a new edit column to this {@link GridPro} with a value provider.
     *
     * @param valueProvider
     *            the value provider
     * @return an edit column configurer for configuring the column editor
     *
     * @see Grid#addColumn(ValueProvider)
     * @see EditColumnConfigurator#text(ItemUpdater)
     * @see EditColumnConfigurator#checkbox(ItemUpdater)
     * @see EditColumnConfigurator#select(ItemUpdater, List)
     * @see #removeColumn(Column)
     */
    public EditColumnConfigurator<E> addEditColumn(
            ValueProvider<E, ?> valueProvider) {
        EditColumn<E> column = this.addColumn(valueProvider,
                this::createEditColumn);

        return new EditColumnConfigurator<>(column, valueProvider);
    }

    /**
     * Adds a new edit column to this {@link GridPro} with a value provider and
     * renderer which is used to display the content when the cell is not in the
     * edit mode.
     *
     * @param valueProvider
     *            the value provider
     * @param renderer
     *            the renderer
     * @return an edit column configurer for configuring the column editor
     *
     * @see Grid#addColumn(Renderer)
     * @see EditColumnConfigurator#text(ItemUpdater)
     * @see EditColumnConfigurator#checkbox(ItemUpdater)
     * @see EditColumnConfigurator#select(ItemUpdater, List)
     * @see #removeColumn(Column)
     */
    public EditColumnConfigurator<E> addEditColumn(
            ValueProvider<E, ?> valueProvider, Renderer<E> renderer) {
        String columnId = createColumnId(false);

        EditColumn<E> column = this.addColumn(
                (new ColumnComponentPathRenderer<>(columnId, value -> {
                    Object item = valueProvider.apply(value);
                    if (item != null) {
                        return item.toString();
                    } else {
                        return "";
                    }
                }, renderer)), this::createEditColumn);
        idToColumnMap.put(columnId, column);

        return new EditColumnConfigurator<>(column, valueProvider);
    }

    /**
     * Adds a new edit column to this {@link GridPro} with a value provider and
     * sorting properties.
     *
     * @param valueProvider
     *            the value provider
     * @param sortingProperties
     *            the sorting properties to use for this column
     * @return an edit column configurer for configuring the column editor
     *
     * @see Grid#addColumn(ValueProvider, String[])
     * @see Column#setComparator(ValueProvider)
     * @see Column#setSortProperty(String...)
     * @see EditColumnConfigurator#text(ItemUpdater)
     * @see EditColumnConfigurator#checkbox(ItemUpdater)
     * @see EditColumnConfigurator#select(ItemUpdater, List)
     * @see #removeColumn(Column)
     */
    public <V extends Comparable<? super V>> EditColumnConfigurator<E> addEditColumn(
            ValueProvider<E, V> valueProvider, String... sortingProperties) {
        EditColumn<E> column = addColumn(valueProvider, this::createEditColumn);
        column.setComparator(valueProvider);
        column.setSortProperty(sortingProperties);
        return new EditColumnConfigurator<>(column, valueProvider);
    }

    /**
     * Adds a new edit column for the given property name.
     * <p>
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #GridPro(Class)}.
     *
     * @param propertyName
     *            the property name of the new column, not <code>null</code>
     * @return and edit column configurer for configuring the column editor
     *
     * @see Grid#addColumn(String)
     * @see EditColumnConfigurator#text(ItemUpdater)
     * @see EditColumnConfigurator#checkbox(ItemUpdater)
     * @see EditColumnConfigurator#select(ItemUpdater, List)
     * @see #removeColumn(Column)
     */
    public EditColumnConfigurator<E> addEditColumn(String propertyName) {
        EditColumn<E> column = this.addColumn(propertyName,
                this::createEditColumn);
        ValueProvider<E, ?> valueProvider = item -> getPropertySet()
                .getProperty(propertyName).get().getGetter().apply(item);
        return new EditColumnConfigurator<>(column, valueProvider);
    }

    /**
     * Sets the value of the webcomponent's property enterNextRow. Default
     * values is false. When true, pressing Enter while in cell edit mode will
     * move focus to the editable cell in the next row (Shift + Enter - same,
     * but for previous row).
     *
     * @param enterNextRow
     *            when <code>true</code>, pressing Enter while in cell edit mode
     *            will move focus to the editable cell in the next row (Shift +
     *            Enter - same, but for previous row)
     */
    public void setEnterNextRow(boolean enterNextRow) {
        getElement().setProperty("enterNextRow", enterNextRow);
    }

    /**
     * Gets the value of the webcomponent's property enterNextRow. Default
     * values is false. When true, pressing Enter while in cell edit mode will
     * move focus to the editable cell in the next row (Shift + Enter - same,
     * but for previous row).
     *
     * @return enterNextRow value
     */
    @Synchronize("enter-next-row-changed")
    public boolean getEnterNextRow() {
        return getElement().getProperty("enterNextRow", false);
    }

    /**
     * Sets the value of the webcomponent's property singleCellEdit. Default
     * values is false. When true, after moving to next or previous editable
     * cell using Tab / Shift+Tab, it will be focused without edit mode.
     *
     * @param singleCellEdit
     *            when <code>true</code>, after moving to next or previous
     *            editable cell using Tab / Shift+Tab, it will be focused
     *            without edit mode
     */
    public void setSingleCellEdit(boolean singleCellEdit) {
        getElement().setProperty("singleCellEdit", singleCellEdit);
    }

    /**
     * Gets the value of the webcomponent's property singleCellEdit. Default
     * values is false. When true, after moving to next or previous editable
     * cell using Tab / Shift+Tab, it will be focused without edit mode.
     *
     * @return singleCellEdit value
     */
    @Synchronize("single-cell-edit-changed")
    public boolean getSingleCellEdit() {
        return getElement().getProperty("singleCellEdit", false);
    }

    /**
     * Sets the value of the webcomponent's property editOnClick. Default values
     * is false. When true, cell edit mode gets activated on a single click
     * instead of the default double click.
     *
     * @param editOnClick
     *            when <code>true</code>, cell edit mode gets activated on a
     *            single click instead of the default double click
     */
    public void setEditOnClick(boolean editOnClick) {
        getElement().setProperty("editOnClick", editOnClick);
    }

    /**
     * Gets the value of the webcomponent's property editOnClick. Default values
     * is false. When true, cell edit mode gets activated on a single click
     * instead of the default double click.
     *
     * @return editOnClick value
     */
    @Synchronize("edit-on-click-changed")
    public boolean getEditOnClick() {
        return getElement().getProperty("editOnClick", false);
    }

    /**
     * Creates a new edit column instance for this {@link GridPro} instance.
     * <p>
     * This method must not return <code>null</code>.
     *
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @param columnId
     *            internal column id
     * @return edit column instance
     * @see Renderer
     */
    protected EditColumn<E> createEditColumn(Renderer<E> renderer,
            String columnId) {
        EditColumn<E> column = new EditColumn<>(this, columnId, renderer);
        idToColumnMap.put(columnId, column);
        return column;
    }

    /**
     * Event fired when the user starts to edit an existing item.
     *
     * @param <E>
     *            the bean type
     */
    @DomEvent("cell-edit-started")
    public static class CellEditStartedEvent<E>
            extends ComponentEvent<GridPro<E>> {

        private E item;
        private String path;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param item
         *            the item to be edited, provided in JSON as internally
         *            represented in Grid
         * @param path
         *            item subproperty that was changed
         */
        public CellEditStartedEvent(GridPro<E> source, boolean fromClient,
                @EventData("event.detail.item") JsonObject item,
                @EventData("event.detail.path") String path) {
            super(source, fromClient);
            this.item = source.getDataCommunicator().getKeyMapper()
                    .get(item.getString("key"));
            this.path = path;
        }

        /**
         * Gets an instance of edited item.
         *
         * @return the instance of edited item
         */
        public E getItem() {
            return item;
        }

        /**
         * Gets the key of the column where item was edited.
         *
         * @return the key of the column
         */
        private String getPath() {
            return path;
        }
    }

    /**
     * Registers a listener to be notified when the user starts to edit an
     * existing item.
     *
     * @param listener
     *            a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addCellEditStartedListener(
            ComponentEventListener<CellEditStartedEvent<E>> listener) {
        return ComponentUtil.addListener(this, CellEditStartedEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Event fired when the user has edited an existing item.
     *
     * @param <E>
     *            the bean type
     */
    @DomEvent("item-property-changed")
    public static class ItemPropertyChangedEvent<E>
            extends ComponentEvent<GridPro<E>> {

        private E item;
        private JsonObject sourceItem;
        private String path;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param item
         *            the item to be edited, provided in JSON as internally
         *            represented in Grid
         * @param path
         *            item subproperty that was changed
         */
        public ItemPropertyChangedEvent(GridPro<E> source, boolean fromClient,
                @EventData("event.detail.item") JsonObject item,
                @EventData("event.detail.path") String path) {
            super(source, fromClient);
            this.sourceItem = item;
            this.item = source.getDataCommunicator().getKeyMapper()
                    .get(item.getString("key"));
            this.path = path;
        }

        /**
         * Gets an instance of edited item.
         *
         * @return the instance of edited item
         */
        public E getItem() {
            return item;
        }

        /**
         * Gets an instance of edited item with the relations to the columns.
         *
         * @return the instance of edited item
         */
        private JsonObject getSourceItem() {
            return sourceItem;
        }

        /**
         * Gets the key of the column where item was edited.
         *
         * @return the key of the column
         */
        private String getPath() {
            return path;
        }
    }

    /**
     * Registers a listener to be notified when the user has edited an existing
     * item.
     *
     * @param listener
     *            a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addItemPropertyChangedListener(
            ComponentEventListener<ItemPropertyChangedEvent<E>> listener) {
        return ComponentUtil.addListener(this, ItemPropertyChangedEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Renderer for edit columns that use custom template for rendering its
     * value (only the value from the object model).
     *
     * @param <SOURCE>
     *            the object model type
     * @see GridPro#addEditColumn(ValueProvider, Renderer)
     */
    class ColumnComponentPathRenderer<SOURCE>
            extends ColumnPathRenderer<SOURCE> {
        private Renderer<SOURCE> representationRenderer;

        /**
         * Creates a new renderer based on the property, value provider for that
         * property, and renderer for its visual representation in column
         *
         * @param property
         *            the property name
         * @param provider
         *            the value provider for the property
         * @param renderer
         *            the renderer for the visual representation
         */
        public ColumnComponentPathRenderer(String property,
                ValueProvider<SOURCE, ?> provider, Renderer<SOURCE> renderer) {
            super(property, provider);
            representationRenderer = renderer;
        }

        @Override
        public Rendering<SOURCE> render(Element container,
                DataKeyMapper<SOURCE> keyMapper, Element contentTemplate) {

            Rendering<SOURCE> columnPathRendering = super.render(container,
                    keyMapper, contentTemplate);
            Rendering<SOURCE> representationRendering = representationRenderer
                    .render(container, keyMapper);

            return new Rendering<SOURCE>() {
                @Override
                public Optional<DataGenerator<SOURCE>> getDataGenerator() {
                    CompositeDataGenerator<SOURCE> compositeDataGenerator = new CompositeDataGenerator<>();
                    compositeDataGenerator.addDataGenerator(
                            representationRendering.getDataGenerator().get());
                    compositeDataGenerator.addDataGenerator(
                            columnPathRendering.getDataGenerator().get());
                    return Optional.of(compositeDataGenerator);
                }

                @Override
                public Element getTemplateElement() {
                    return representationRendering.getTemplateElement();
                }
            };
        }
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(GridProVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants).map(GridProVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(GridProVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(GridProVariant::getVariantName)
                        .collect(Collectors.toList()));
    }
}

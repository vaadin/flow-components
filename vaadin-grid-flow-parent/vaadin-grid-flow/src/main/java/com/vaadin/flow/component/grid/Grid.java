/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.grid.GridArrayUpdater.UpdateQueueData;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorImpl;
import com.vaadin.flow.component.grid.editor.EditorRenderer;
import com.vaadin.flow.data.binder.BeanPropertySet;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.event.SortEvent.SortNotifier;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.data.provider.DataViewUtils;
import com.vaadin.flow.data.provider.HasDataGenerators;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.selection.SelectionModel.Single;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.selection.SingleSelectionListener;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.internal.ReflectTools;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.slf4j.LoggerFactory;

/**
 * Grid is a component for showing tabular data. A basic Grid uses plain text to
 * display information in rows and columns. However, rich content can be used to
 * provide additional information in a more legible fashion using component
 * renderers or Lit renderers. The Grid supports the following features.
 * <p>
 * Dynamic Height:<br>
 * Grid has a default height of 400 pixels. It becomes scrollable when its items
 * overflow the allocated space. In addition to setting any fixed or relative
 * value, the height of a grid can be set by the number of items in the dataset,
 * meaning that the grid will grow and shrink based on the row count.
 * <p>
 * Selection:<br>
 * Grid selection is not enabled by default. Grid supports single and
 * multi-select. The former allows the user to select exactly one item while the
 * latter enables multiple items to be selected. In single selection mode, the
 * user can select and deselect rows by clicking anywhere on the row. In
 * multi-select mode, the user can use a checkbox column to select and deselect
 * rows.
 * <p>
 * Columns:<br>
 * Column alignment, freezing (fixed position), grouping, headers & footers,
 * visibility, and width can be configured. Users can be allowed to resize and
 * reorder columns.
 * <p>
 * Sorting:<br>
 * Any column can be made sortable. Enable sorting to allow the user to sort
 * items alphabetically, numerically, by date, etc. You can also sort columns
 * that contain rich and/or custom content by defining which property to sort
 * by. For example, you can have a column containing a person’s profile picture,
 * name and email sorted by the person’s last name. Sorting helps users find and
 * analyze the data, so it’s generally recommended to enable it for all
 * applicable columns, except in cases where the order of items is an essential
 * part of the data itself (such as prioritized lists).
 * <p>
 * Filtering:<br>
 * Filtering allows the user to quickly find a specific item or subset of items.
 * You can add filters to Grid columns or use external filter fields.
 * <p>
 * Item Details:<br>
 * Item Details are expandable content areas that can be displayed below the
 * regular content of a row, used to display more information about an item. By
 * default, an item’s details are toggled by clicking on the item’s row. The
 * default toggle behavior can be replaced by programmatically toggling the
 * details visibility, for example, from a button click.
 * <p>
 * Context Menu:<br>
 * You can use Context Menu to provide shortcuts to the user. It appears on
 * right (default) or left click. In a mobile browser, a long press opens the
 * menu.
 * <p>
 * Drag and Drop:<br>
 * Grid supports drag and drop, for example to reorder rows and to drag rows
 * between grids.
 * <p>
 * Inline Editing:<br>
 * Grid can be configured to allow inline editing. Editing can be either
 * buffered and non-buffered. Buffered means changes must be explicitly
 * committed, while non-buffered automatically commit changes on blur (when a
 * field loses focus).
 * <p>
 * Styling Rows and Columns:<br>
 * You can style individual cells based on the data, for example, to highlight
 * changes or important information.
 * <p>
 * Cell Focus:<br>
 * Cells can be focused by clicking on a cell or with the keyboard.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the grid bean type
 *
 */
@Tag("vaadin-grid")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/grid", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-grid", version = "23.1.0-beta1")
@JsModule("@vaadin/grid/src/vaadin-grid.js")
@JsModule("@vaadin/grid/src/vaadin-grid-column.js")
@JsModule("@vaadin/grid/src/vaadin-grid-sorter.js")
@JsModule("@vaadin/checkbox/src/vaadin-checkbox.js")
@JsModule("@vaadin/polymer-legacy-adapter/template-renderer.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./gridConnector.js")
public class Grid<T> extends Component implements HasStyle, HasSize,
        Focusable<Grid<T>>, SortNotifier<Grid<T>, GridSortOrder<T>>, HasTheme,
        HasDataGenerators<T>, HasListDataView<T, GridListDataView<T>>,
        HasDataView<T, Void, GridDataView<T>>,
        HasLazyDataView<T, Void, GridLazyDataView<T>> {

    /**
     * behavior when parsing nested properties which may contain
     * <code>null</code> values in the property chain
     */
    public enum NestedNullBehavior {
        /**
         * throw a NullPointerException if there is a nested <code>null</code>
         * value
         */
        THROW,
        /**
         * silently ignore any exceptions caused by nested <code>null</code>
         * values
         */
        ALLOW_NULLS
    }

    private NestedNullBehavior nestedNullBehavior = NestedNullBehavior.THROW;

    // package-private because it's used in tests
    static final String DRAG_SOURCE_DATA_KEY = "drag-source-data";

    protected static class UpdateQueue implements Update {
        private final ArrayList<SerializableRunnable> queue = new ArrayList<>();
        private final UpdateQueueData data;

        protected UpdateQueue(UpdateQueueData data, int size) {
            this.data = data;
            // 'size' property is not synchronized by the web component since
            // there are no events for it, but we
            // need to sync it otherwise server will overwrite client value with
            // the old server one
            enqueue("$connector.updateSize", size);
            if (data.getUniqueKeyProperty() != null) {
                enqueue("$connector.updateUniqueItemIdPath",
                        data.getUniqueKeyProperty());
            }
            getElement().setProperty("size", size);
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
            enqueue("$connector.confirm", updateId);
            commit();
        }

        public void commit() {
            queue.forEach(SerializableRunnable::run);
            queue.clear();
        }

        public void enqueue(String name, Serializable... arguments) {
            queue.add(() -> getElement().callJsFunction(name, arguments));
        }

        protected Element getElement() {
            return data.getElement();
        }

        /**
         * Gets {@link UpdateQueueData} for this queue.
         *
         * @return the {@link UpdateQueueData} object.
         */
        public UpdateQueueData getData() {
            return data;
        }
    }

    /**
     * Selection mode representing the built-in selection models in grid.
     * <p>
     * These enums can be used in {@link Grid#setSelectionMode(SelectionMode)}
     * to easily switch between the built-in selection models.
     *
     * @see Grid#setSelectionMode(SelectionMode)
     * @see Grid#setSelectionModel(GridSelectionModel, SelectionMode)
     */
    public enum SelectionMode {

        /**
         * Single selection mode that maps to built-in {@link Single}.
         *
         * @see GridSingleSelectionModel
         */
        SINGLE {
            @Override
            protected <T> GridSelectionModel<T> createModel(Grid<T> grid) {
                return new AbstractGridSingleSelectionModel<T>(grid) {

                    @SuppressWarnings("unchecked")
                    @Override
                    protected void fireSelectionEvent(
                            SelectionEvent<Grid<T>, T> event) {
                        grid.fireEvent((ComponentEvent<Grid<T>>) event);
                    }

                    @Override
                    public void setDeselectAllowed(boolean deselectAllowed) {
                        super.setDeselectAllowed(deselectAllowed);
                        grid.getElement().setProperty("__deselectDisallowed",
                                !deselectAllowed);
                    }
                };
            }
        },

        /**
         * Multiselection mode that maps to built-in
         * {@link SelectionModel.Multi}.
         *
         * @see GridMultiSelectionModel
         */
        MULTI {
            @Override
            protected <T> GridSelectionModel<T> createModel(Grid<T> grid) {
                return new AbstractGridMultiSelectionModel<T>(grid) {

                    @SuppressWarnings("unchecked")
                    @Override
                    protected void fireSelectionEvent(
                            SelectionEvent<Grid<T>, T> event) {
                        grid.fireEvent((ComponentEvent<Grid<?>>) event);
                    }
                };
            }
        },

        /**
         * Selection model that doesn't allow selection.
         *
         * @see GridNoneSelectionModel
         */
        NONE {
            @Override
            protected <T> GridSelectionModel<T> createModel(Grid<T> grid) {
                return new GridNoneSelectionModel<>();
            }
        };

        /**
         * Creates the selection model to use with this enum.
         *
         * @param <T>
         *            the type of items in the grid
         * @param grid
         *            the grid to create the selection model for
         * @return the selection model
         */
        protected abstract <T> GridSelectionModel<T> createModel(Grid<T> grid);
    }

    /**
     * Server-side component for the {@code <vaadin-grid-column>} element.
     *
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * @param <T>
     *            type of the underlying grid this column is compatible with
     */
    @Tag("vaadin-grid-column")
    @NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
    @JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
    public static class Column<T> extends AbstractColumn<Column<T>> {

        private final String columnInternalId; // for internal implementation
                                               // only
        private String columnKey; // defined and used by the user

        private boolean sortingEnabled;

        private Component editorComponent;
        private EditorRenderer<T> editorRenderer;

        private SortOrderProvider sortOrderProvider = direction -> {
            String key = getKey();
            if (key == null) {
                return Stream.empty();
            }
            return Stream.of(new QuerySortOrder(key, direction));
        };

        private SerializableComparator<T> comparator;

        private Registration columnDataGeneratorRegistration;
        private Registration editorDataGeneratorRegistration;

        private Renderer<T> renderer;
        private Rendering<T> rendering;

        private SerializableFunction<T, String> classNameGenerator = item -> null;

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
        @SuppressWarnings("unchecked")
        public Column(Grid<T> grid, String columnId, Renderer<T> renderer) {
            super(grid);
            Objects.requireNonNull(renderer);
            this.columnInternalId = columnId;
            this.renderer = renderer;

            comparator = (a, b) -> 0;

            rendering = renderer.render(getElement(), (KeyMapper<T>) getGrid()
                    .getDataCommunicator().getKeyMapper());

            Optional<DataGenerator<T>> dataGenerator = rendering
                    .getDataGenerator();

            if (dataGenerator.isPresent()) {
                columnDataGeneratorRegistration = grid
                        .addDataGenerator(dataGenerator.get());
            }

            getElement().setAttribute("suppress-template-warning", true);
        }

        protected void destroyDataGenerators() {
            if (columnDataGeneratorRegistration != null) {
                columnDataGeneratorRegistration.remove();
                columnDataGeneratorRegistration = null;
            }
            if (editorDataGeneratorRegistration != null) {
                editorDataGeneratorRegistration.remove();
                editorDataGeneratorRegistration = null;
            }
        }

        protected String getInternalId() {
            return columnInternalId;
        }

        /**
         * Get the renderer used for this column.
         * <p>
         * <strong>Note:</strong> Mutating the renderer after the Grid has been
         * rendered on the client will not change the column, and can lead to
         * undefined behavior.
         *
         * @return the renderer used for this column, should never be
         *         {@code null}
         */
        public Renderer<T> getRenderer() {
            return renderer;
        }

        /**
         * Sets the width of this column as a CSS-string.
         *
         * @see #setFlexGrow(int)
         *
         * @param width
         *            the width to set this column to, as a CSS-string, not
         *            {@code null}
         * @return this column, for method chaining
         */
        public Column<T> setWidth(String width) {
            getElement().setProperty("width", width);
            return this;
        }

        /**
         * Gets the width of this column as a CSS-string.
         *
         * @see Grid#addColumnResizeListener(ComponentEventListener)
         *
         * @return the width of this column as a CSS-string
         */
        @Synchronize("column-drag-resize")
        public String getWidth() {
            return getElement().getProperty("width");
        }

        /**
         * Sets the flex grow ratio for this column. When set to 0, column width
         * is fixed.
         *
         * @see #setWidth(String)
         *
         * @param flexGrow
         *            the flex grow ratio
         * @return this column, for method chaining
         */
        public Column<T> setFlexGrow(int flexGrow) {
            getElement().setProperty("flexGrow", flexGrow);
            return this;
        }

        /**
         * Gets the flex grow value, by default 1.
         *
         * @see Grid#addColumnResizeListener(ComponentEventListener)
         *
         * @return the flex grow value, by default 1
         */
        @Synchronize("column-drag-resize")
        public int getFlexGrow() {
            return getElement().getProperty("flexGrow", 1);
        }

        /**
         * Enables or disables automatic width for this column.
         * <p>
         * Automatically sets the width of the column based on the column
         * contents when this is set to {@code true}.
         * <p>
         * For performance reasons the column width is calculated automatically
         * only once when the grid items are rendered for the first time and the
         * calculation only considers the rows which are currently rendered in
         * DOM (a bit more than what is currently visible). If the grid is
         * scrolled, or the cell content changes, the column width might not
         * match the contents anymore.
         * <p>
         * Hidden columns are ignored in the calculation and their widths are
         * not automatically updated when you show a column that was initially
         * hidden.
         * <p>
         * You can manually trigger the auto sizing behavior again by calling
         * {@link Grid#recalculateColumnWidths()}.
         * <p>
         * The column width may still grow larger when {@code flexGrow} is not
         * 0.
         *
         * @see Grid#recalculateColumnWidths()
         * @see Column#setFlexGrow
         *
         * @param autoWidth
         *            whether to enable or disable automatic width on this
         *            column
         * @return this column, for method chaining
         */
        public Column<T> setAutoWidth(boolean autoWidth) {
            getElement().setProperty("autoWidth", autoWidth);
            return this;
        }

        /**
         * Gets this column's auto width state.
         *
         * @return whether this column has automatic width enabled
         */
        public boolean isAutoWidth() {
            return getElement().getProperty("autoWidth", false);
        }

        /**
         * Sets the user-defined identifier to map this column. The key can be
         * used to fetch the column later with
         * {@link Grid#getColumnByKey(String)}.
         * <p>
         * The key is also used as the {@link #setSortProperty(String...)
         * backend sort property} for this column if no sort property or sort
         * order provider has been set for this column.
         * <p>
         * The key has to be unique within the grid, and it can't be changed
         * after set once.
         *
         * @see #setSortProperty(String...)
         * @see #setSortOrderProvider(SortOrderProvider)
         *
         * @param key
         *            the identifier key, can't be {@code null}
         * @return this column
         */
        public Column<T> setKey(String key) {
            Objects.requireNonNull(key, "Column key cannot be null");
            if (this.columnKey != null) {
                throw new IllegalStateException("Column key cannot be changed");
            }
            getGrid().setColumnKey(key, this);
            this.columnKey = key;
            return this;
        }

        /**
         * Gets the user-defined key for this column, or {@code null} if no key
         * has been set.
         *
         * @return the user-defined key
         */
        public String getKey() {
            return columnKey;
        }

        /**
         * Gets the underlying {@code <vaadin-grid-column>} element.
         * <p>
         * <strong>It is highly discouraged to directly use the API exposed by
         * the returned element.</strong>
         *
         * @return the root element of this component
         */
        @Override
        public Element getElement() {
            return super.getElement();
        }

        /**
         * Sets a comparator to use with in-memory sorting with this column.
         * Sorting with a back-end is done using
         * {@link Column#setSortProperty(String...)}.
         * <p>
         * <strong>Note:</strong> calling this method automatically sets the
         * column as sortable with {@link #setSortable(boolean)}.
         * <p>
         * <strong>Note:</strong> Comparator is not serializable. If you need to
         * write serializable implementation, use inlined class of
         * {@link SerializableComparator} instead of Lambda expression.
         *
         * @param comparator
         *            the comparator to use when sorting data in this column
         * @return this column
         */
        public Column<T> setComparator(Comparator<T> comparator) {
            Objects.requireNonNull(comparator, "Comparator must not be null");
            setSortable(true);
            this.comparator = comparator::compare;
            return this;
        }

        /**
         * Sets a comparator to use with in-memory sorting with this column
         * based on the return type of the given {@link ValueProvider}.Sorting
         * with a back-end is done using
         * {@link Column#setSortProperty(String[])}.
         * <p>
         * <strong>Note:</strong> calling this method automatically sets the
         * column as sortable with {@link #setSortable(boolean)}.
         *
         * @param <V>
         *            the value of the column
         * @param keyExtractor
         *            the value provider used to extract the {@link Comparable}
         *            sort key
         * @return this column
         * @see Comparator#comparing(java.util.function.Function)
         */
        public <V extends Comparable<? super V>> Column<T> setComparator(
                ValueProvider<T, V> keyExtractor) {
            Objects.requireNonNull(keyExtractor,
                    "Key extractor must not be null");
            setComparator(Comparator.comparing(keyExtractor,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            return this;
        }

        /**
         * Gets the comparator to use with in-memory sorting for this column
         * when sorting in the given direction.
         * <p>
         * <strong>Note:</strong> calling this method automatically sets the
         * column as sortable with {@link #setSortable(boolean)}.
         *
         * @param sortDirection
         *            the direction this column is sorted by
         * @return comparator for this column
         */
        public SerializableComparator<T> getComparator(
                SortDirection sortDirection) {
            Objects.requireNonNull(comparator,
                    "No comparator defined for sorted column.");
            setSortable(true);
            boolean reverse = sortDirection != SortDirection.ASCENDING;
            return reverse ? comparator.reversed()::compare : comparator;
        }

        /**
         * Sets strings describing back end properties to be used when sorting
         * this column.
         * <p>
         * <strong>Note:</strong> calling this method automatically sets the
         * column as sortable with {@link #setSortable(boolean)}.
         *
         * @param properties
         *            the array of strings describing backend properties
         * @return this column
         */
        public Column<T> setSortProperty(String... properties) {
            Objects.requireNonNull(properties,
                    "Sort properties must not be null");
            setSortable(true);
            sortOrderProvider = dir -> Arrays.stream(properties)
                    .map(s -> new QuerySortOrder(s, dir));
            return this;
        }

        /**
         * Sets the sort orders when sorting this column. The sort order
         * provider is a function which provides {@link QuerySortOrder} objects
         * to describe how to sort by this column.
         * <p>
         * The default provider uses the sort properties set with
         * {@link #setSortProperty(String...)}.
         * <p>
         * <strong>Note:</strong> calling this method automatically sets the
         * column as sortable with {@link #setSortable(boolean)}.
         *
         * @param provider
         *            the function to use when generating sort orders with the
         *            given direction
         * @return this column
         */
        public Column<T> setSortOrderProvider(SortOrderProvider provider) {
            Objects.requireNonNull(provider,
                    "Sort order provider must not be null");
            setSortable(true);
            sortOrderProvider = provider;
            return this;
        }

        /**
         * Gets the sort orders to use with back-end sorting for this column
         * when sorting in the given direction.
         *
         * @see #setSortProperty(String...)
         * @see #setId(String)
         * @see #setSortOrderProvider(SortOrderProvider)
         *
         * @param direction
         *            the sorting direction
         * @return stream of sort orders
         */
        public Stream<QuerySortOrder> getSortOrder(SortDirection direction) {
            return sortOrderProvider.apply(direction);
        }

        /**
         * Sets whether the user can sort this column or not.
         *
         * @param sortable
         *            {@code true} if the column can be sorted by the user;
         *            {@code false} if not
         * @return this column
         */
        public Column<T> setSortable(boolean sortable) {
            if (this.sortingEnabled == sortable) {
                return this;
            }
            this.sortingEnabled = sortable;

            HeaderRow defaultHeaderRow = getGrid().getDefaultHeaderRow();
            if (defaultHeaderRow != null) {
                defaultHeaderRow.getCell(this).getColumn()
                        .setSortingIndicators(sortable);
            }

            return this;
        }

        /**
         * Gets whether this column is sortable (e.g. shows the sorting
         * indicators at the client-side).
         *
         * @return <code>true</code> if the column is sortable,
         *         <code>false</code> otherwise
         */
        public boolean isSortable() {
            return sortingEnabled;
        }

        /**
         * Sets a header text to the column.
         * <p>
         * If there are no header rows when calling this method, the first
         * header row will be created. If there are header rows, the header will
         * be set on the first created header row and it will override any
         * existing header.
         *
         * @param labelText
         *            the text to be shown at the column header
         * @return this column, for method chaining
         */
        public Column<T> setHeader(String labelText) {
            HeaderRow defaultHeaderRow = getGrid().getDefaultHeaderRow();
            if (defaultHeaderRow == null) {
                defaultHeaderRow = getGrid().addFirstHeaderRow();
            }
            defaultHeaderRow.getCell(this).setText(labelText);
            grid.updateClientSideSorterIndicators();
            return this;
        }

        /**
         * Sets a footer text to the column.
         * <p>
         * If there are no footer rows when calling this method, the first
         * footer row will be created. If there are footer rows, the footer will
         * be set on the bottom footer row and it will override any existing
         * footer.
         *
         * @param labelText
         *            the text to be shown at the column footer
         * @return this column, for method chaining
         */
        public Column<T> setFooter(String labelText) {
            getGrid().getColumnLayers().get(0).asFooterRow().getCell(this)
                    .setText(labelText);
            return this;
        }

        /**
         * Sets a header component to the column.
         * <p>
         * If there are no header rows when calling this method, the first
         * header row will be created. If there are header rows, the header will
         * be set on the first created header row and it will override any
         * existing header.
         *
         * @param headerComponent
         *            the component to be used in the header of the column
         * @return this column, for method chaining
         */
        public Column<T> setHeader(Component headerComponent) {
            HeaderRow defaultHeaderRow = getGrid().getDefaultHeaderRow();
            if (defaultHeaderRow == null) {
                defaultHeaderRow = getGrid().addFirstHeaderRow();
            }
            defaultHeaderRow.getCell(this).setComponent(headerComponent);
            grid.updateClientSideSorterIndicators();
            return this;
        }

        /**
         * Sets a footer component to the column.
         * <p>
         * If there are no footer rows when calling this method, the first
         * footer row will be created. If there are footer rows, the footer will
         * be set on the bottom footer row and it will override any existing
         * footer.
         *
         * @param footerComponent
         *            the component to be used in the footer of the column
         * @return this column, for method chaining
         */
        public Column<T> setFooter(Component footerComponent) {
            getGrid().getColumnLayers().get(0).asFooterRow().getCell(this)
                    .setComponent(footerComponent);
            return this;
        }

        /**
         * Sets a component to use for editing values of this column in the
         * editor row. This is a convenient way for use in simple cases where
         * the same component can be used to edit all the items. Use
         * {@link #setEditorComponent(SerializableFunction)} to support more
         * complex cases.
         *
         * @param editorComponent
         *            the editor component, or <code>null</code> to remove the
         *            editor component for this column
         * @return this column
         *
         * @see Grid#getEditor()
         * @see Binder#bind(HasValue, ValueProvider, Setter)
         */
        public Column<T> setEditorComponent(Component editorComponent) {
            if (editorComponent == null) {
                setEditorComponent(
                        (SerializableFunction<T, ? extends Component>) null);
            } else {
                setEditorComponent(item -> editorComponent);
            }
            this.editorComponent = editorComponent;
            return this;
        }

        /**
         * Sets a function that returns the editor component to be used for an
         * specific item in the editor row.
         *
         * @param componentCallback
         *            the editor component function, or <code>null</code> to
         *            remove the editor component for this column
         *
         * @return this column
         *
         * @see Grid#getEditor()
         * @see #setEditorComponent(Component)
         */
        public Column<T> setEditorComponent(
                SerializableFunction<T, ? extends Component> componentCallback) {

            editorComponent = null;
            if (editorRenderer == null && componentCallback != null) {
                setupColumnEditor();
            }
            if (editorRenderer != null) {
                editorRenderer.setComponentFunction(componentCallback);
            }

            return this;
        }

        /**
         * Gets the editor component that is used for this column.
         *
         * @return the editor component, or <code>null</code> if no component is
         *         set, or if it was set by using
         *         {@link #setEditorComponent(SerializableFunction)}.
         *
         * @see #setEditorComponent(Component)
         */
        public Component getEditorComponent() {
            return editorComponent;
        }

        /**
         * Sets the function that is used for generating CSS class names for
         * cells in this column. Returning {@code null} from the generator
         * results in no custom class name being set. Multiple class names can
         * be returned from the generator as space-separated.
         * <p>
         * If {@link Grid#setClassNameGenerator(SerializableFunction)} is used
         * together with this method, resulting class names from both methods
         * will be effective. Class names generated by grid are applied to the
         * cells before the class names generated by column. This means that if
         * the classes contain conflicting style properties, column's classes
         * will win.
         *
         * @param classNameGenerator
         *            the class name generator to set, not {@code null}
         * @return this column
         * @throws NullPointerException
         *             if {@code classNameGenerator} is {@code null}
         * @see Grid#setClassNameGenerator(SerializableFunction)
         */
        public Column<T> setClassNameGenerator(
                SerializableFunction<T, String> classNameGenerator) {
            Objects.requireNonNull(classNameGenerator,
                    "Class name generator can not be null");
            this.classNameGenerator = classNameGenerator;
            getGrid().getDataCommunicator().reset();
            return this;
        }

        /**
         * Gets the function that is used for generating CSS class names for
         * cells in this column.
         *
         * @return the class name generator
         */
        public SerializableFunction<T, String> getClassNameGenerator() {
            return classNameGenerator;
        }

        @Override
        protected Column<?> getBottomLevelColumn() {
            return this;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void setupColumnEditor() {
            editorRenderer = new EditorRenderer<>((Editor) grid.getEditor(),
                    columnInternalId);

            Rendering<T> editorRendering = editorRenderer.render(getElement(),
                    null, rendering.getTemplateElement());

            Optional<DataGenerator<T>> dataGenerator = editorRendering
                    .getDataGenerator();
            if (dataGenerator.isPresent()) {
                editorDataGeneratorRegistration = grid
                        .addDataGenerator((DataGenerator) dataGenerator.get());
            }
        }
    }

    /**
     * A helper base class for creating extensions for the Grid component.
     *
     * @param <T>
     *            the grid bean type
     */
    public abstract static class AbstractGridExtension<T>
            implements DataGenerator<T> {

        private Grid<T> grid;
        private Registration registration;

        /**
         * Constructs a new grid extension, extending the given grid.
         *
         * @param grid
         *            the grid to extend
         */
        public AbstractGridExtension(Grid<T> grid) {
            extend(grid);
        }

        /**
         * A helper method for refreshing the client-side representation of a
         * single data item.
         *
         * @param item
         *            the item to refresh
         */
        protected void refresh(T item) {
            getGrid().getDataCommunicator().refresh(item);
        }

        /**
         * Adds this extension to the given grid.
         *
         * @param grid
         *            the grid to extend
         */
        protected void extend(Grid<T> grid) {
            this.grid = grid;
            registration = getGrid().addDataGenerator(this);
        }

        /**
         * Remove this extension from its target.
         */
        protected void remove() {
            registration.remove();
        }

        /**
         * Gets the Grid this extension extends.
         *
         * @return the grid this extension extends
         */
        protected Grid<T> getGrid() {
            return grid;
        }
    }

    /**
     * Class for managing visible details rows.
     *
     */
    private class DetailsManager extends AbstractGridExtension<T> {

        private final HashSet<T> detailsVisible = new HashSet<>();

        /**
         * Constructs a new details manager for the given grid.
         *
         * @param grid
         *            the grid whose details are to be managed
         */
        public DetailsManager(Grid<T> grid) {
            super(grid);
        }

        /**
         * Sets the visibility of details for given item.
         *
         * @param item
         *            the item to show details for
         * @param visible
         *            {@code true} if details component should be visible;
         *            {@code false} if it should be hidden
         */
        public void setDetailsVisible(T item, boolean visible) {
            boolean refresh = false;
            if (!visible) {
                refresh = detailsVisible.remove(item);
            } else {
                detailsVisible.add(item);
                refresh = true;
            }

            if (itemDetailsDataGenerator != null && refresh) {
                refresh(item);
                if (!detailsVisible.contains(item)) {
                    itemDetailsDataGenerator.destroyData(item);
                }
            }
        }

        /**
         * Returns the visibility of the details component for the given item.
         *
         * @param item
         *            the item to check
         *
         * @return {@code true} if details component should be visible;
         *         {@code false} if it should be hidden
         */
        public boolean isDetailsVisible(T item) {
            return itemDetailsDataGenerator != null
                    && detailsVisible.contains(item);
        }

        @Override
        public void generateData(T item, JsonObject jsonObject) {
            if (itemDetailsDataGenerator != null && isDetailsVisible(item)) {
                jsonObject.put("detailsOpened", true);
                itemDetailsDataGenerator.generateData(item, jsonObject);
            }
        }

        /**
         * Remove the displayed details and remove details item from the list
         *
         * @param item
         *            item to removed
         */
        @Override
        public void destroyData(T item) {
            detailsVisible.remove(item);
            if (itemDetailsDataGenerator != null) {
                itemDetailsDataGenerator.destroyData(item);
            }
        }

        /**
         * Remove the displayed details but keep the items from list of details
         */
        @Override
        public void destroyAllData() {
            if (itemDetailsDataGenerator != null) {
                itemDetailsDataGenerator.destroyAllData();
            }
        }

        @Override
        public void refreshData(T item) {
            if (itemDetailsDataGenerator != null) {
                if (isDetailsVisible(item)) {
                    itemDetailsDataGenerator.refreshData(item);
                } else {
                    itemDetailsDataGenerator.destroyData(item);
                }
            }
        }

        private void setDetailsVisibleFromClient(Set<T> items) {
            Set<T> toRefresh = new HashSet<>();
            toRefresh.addAll(detailsVisible);
            toRefresh.addAll(items);

            detailsVisible.clear();
            detailsVisible.addAll(items);
            if (itemDetailsDataGenerator != null) {
                for (T item : toRefresh) {
                    refresh(item);
                }
            }
        }
    }

    private class GridArrayUpdaterImpl implements GridArrayUpdater {
        private UpdateQueueData data;
        private SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory;

        public GridArrayUpdaterImpl(
                SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory) {
            this.updateQueueFactory = updateQueueFactory;
        }

        @Override
        public UpdateQueue startUpdate(int sizeChange) {
            return updateQueueFactory.apply(data, sizeChange);
        }

        @Override
        public void initialize() {
            initConnector();
            updateSelectionModeOnClient();
            setRequestedRange(0, getPageSize());
        }

        @Override
        public void setUpdateQueueData(UpdateQueueData data) {
            this.data = data;
        }

        @Override
        public UpdateQueueData getUpdateQueueData() {
            return data;
        }

    }

    private final GridArrayUpdater arrayUpdater;

    private final CompositeDataGenerator<T> gridDataGenerator;
    private final DataCommunicator<T> dataCommunicator;

    private int nextColumnId = 0;

    private GridSelectionModel<T> selectionModel;
    private SelectionMode selectionMode;

    private final DetailsManager detailsManager;
    private Element detailsTemplate;

    private Map<String, Column<T>> idToColumnMap = new HashMap<>();
    private Map<String, Column<T>> keyToColumnMap = new HashMap<>();

    private final List<GridSortOrder<T>> sortOrder = new ArrayList<>();

    // This callback is only used by GridListDataView when the data filtering
    // is being changed through its API
    private SerializableConsumer<?> filterSlot;

    private Class<T> beanType;
    private PropertySet<T> propertySet;

    private DataGenerator<T> itemDetailsDataGenerator;
    private List<Registration> detailsRenderingRegistrations = new ArrayList<>();

    /**
     * Keeps track of the layers of column and column-group components. The
     * layers are in order from innermost to outmost.
     */
    private List<ColumnLayer> columnLayers = new ArrayList<>();
    private HeaderRow defaultHeaderRow;

    private String uniqueKeyProperty;

    private ValueProvider<T, String> uniqueKeyProvider;

    private Editor<T> editor;

    private SerializableSupplier<Editor<T>> editorFactory = this::createEditor;

    private boolean verticalScrollingEnabled = true;

    private SerializableFunction<T, String> classNameGenerator = item -> null;
    private SerializablePredicate<T> dropFilter = item -> true;
    private SerializablePredicate<T> dragFilter = item -> true;
    private Map<String, SerializableFunction<T, String>> dragDataGenerators = new HashMap<>();

    private Registration dataProviderChangeRegistration;

    /**
     * Creates a new instance, with page size of 50.
     */
    public Grid() {
        this(50);
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
    public Grid(int pageSize) {
        this(pageSize, null, new DataCommunicatorBuilder<>());
    }

    /**
     * Creates a new grid with an initial set of columns for each of the bean's
     * properties. The property-values of the bean will be converted to Strings.
     * Full names of the properties will be used as the
     * {@link Column#setKey(String) column keys} and the property captions will
     * be used as the {@link Column#setHeader(String) column headers}. The
     * generated columns will be sortable by default, if the property is
     * {@link Comparable}.
     * <p>
     * When autoCreateColumns is <code>true</code>, only the direct properties
     * of the bean are included and they will be in alphabetical order. Use
     * {@link Grid#setColumns(String...)} to define which properties to include
     * and in which order. You can also add a column for an individual property
     * with {@link #addColumn(String)}. Both of these methods support also
     * sub-properties with dot-notation, eg.
     * <code>"property.nestedProperty"</code>.
     *
     * @param beanType
     *            the bean type to use, not <code>null</code>
     * @param autoCreateColumns
     *            when <code>true</code>, columns are created automatically for
     *            the properties of the beanType
     */
    public Grid(Class<T> beanType, boolean autoCreateColumns) {
        this();
        configureBeanType(beanType, autoCreateColumns);
    }

    /**
     * Creates a new grid with an initial set of columns for each of the bean's
     * properties. The property-values of the bean will be converted to Strings.
     * Full names of the properties will be used as the
     * {@link Column#setKey(String) column keys} and the property captions will
     * be used as the {@link Column#setHeader(String) column headers}. The
     * generated columns will be sortable by default, if the property is
     * {@link Comparable}.
     * <p>
     * By default, only the direct properties of the bean are included and they
     * will be in alphabetical order. Use {@link Grid#setColumns(String...)} to
     * define which properties to include and in which order. You can also add a
     * column for an individual property with {@link #addColumn(String)}. Both
     * of these methods support also sub-properties with dot-notation, eg.
     * <code>"property.nestedProperty"</code>.
     *
     * @param beanType
     *            the bean type to use, not <code>null</code>
     */
    public Grid(Class<T> beanType) {
        this(beanType, true);
    }

    /**
     * Creates a new grid with an initial set of columns for each of the bean's
     * properties. The property-values of the bean will be converted to Strings.
     * Full names of the properties will be used as the
     * {@link Column#setKey(String) column keys} and the property captions will
     * be used as the {@link Column#setHeader(String) column headers}.
     * <p>
     * You can add columns for nested properties of the bean with
     * {@link #addColumn(String)}.
     *
     * @param beanType
     *            the bean type to use, not <code>null</code>
     * @param updateQueueBuilder
     *            the builder for new {@link UpdateQueue} instance
     * @param dataCommunicatorBuilder
     *            Builder for {@link DataCommunicator} implementation this Grid
     *            uses to handle all data communication.
     * @param <B>
     *            the data communicator builder type
     * @param <U>
     *            the GridArrayUpdater type
     */
    protected <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> Grid(
            Class<T> beanType,
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueBuilder,
            B dataCommunicatorBuilder) {
        this(50, updateQueueBuilder, dataCommunicatorBuilder);
        Objects.requireNonNull(beanType, "Bean type can't be null");
        Objects.requireNonNull(dataCommunicatorBuilder,
                "Data communicator builder can't be null");
        this.beanType = beanType;
        propertySet = BeanPropertySet.get(beanType);
        propertySet.getProperties()
                .filter(property -> !property.isSubProperty())
                .forEach(this::addColumn);
    }

    /**
     * Creates a new instance, with the specified page size and data
     * communicator.
     * <p>
     * The page size influences the {@link Query#getLimit()} sent by the client,
     * but it's up to the webcomponent to determine the actual query limit,
     * based on the height of the component and scroll position. Usually the
     * limit is 3 times the page size (e.g. 150 items with a page size of 50).
     *
     * @param pageSize
     *            the page size. Must be greater than zero.
     * @param updateQueueBuilder
     *            the builder for new {@link UpdateQueue} instance
     * @param dataCommunicatorBuilder
     *            Builder for {@link DataCommunicator} implementation this Grid
     *            uses to handle all data communication.
     * @param <B>
     *            the data communicator builder type
     * @param <U>
     *            the GridArrayUpdater type
     *
     */
    @SuppressWarnings("unchecked")
    protected <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> Grid(
            int pageSize,
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueBuilder,
            B dataCommunicatorBuilder) {
        Objects.requireNonNull(dataCommunicatorBuilder,
                "Data communicator builder can't be null");
        arrayUpdater = createDefaultArrayUpdater(
                Optional.ofNullable(updateQueueBuilder)
                        .orElseGet(() -> UpdateQueue::new));
        arrayUpdater.setUpdateQueueData(
                new UpdateQueueData(getElement(), getUniqueKeyProperty()));
        gridDataGenerator = new CompositeDataGenerator<>();
        gridDataGenerator.addDataGenerator(this::generateUniqueKeyData);
        gridDataGenerator.addDataGenerator(this::generateStyleData);
        gridDataGenerator.addDataGenerator(this::generateRowsDragAndDropAccess);
        gridDataGenerator.addDataGenerator(this::generateDragData);

        dataCommunicator = dataCommunicatorBuilder.build(getElement(),
                gridDataGenerator, (U) arrayUpdater,
                this::getUniqueKeyProvider);

        detailsManager = new DetailsManager(this);
        setPageSize(pageSize);
        setSelectionModel(SelectionMode.SINGLE.createModel(this),
                SelectionMode.SINGLE);

        columnLayers.add(new ColumnLayer(this));

        addDragStartListener(this::onDragStart);
        addDragEndListener(this::onDragEnd);

        getElement().setAttribute("suppress-template-warning", true);
    }

    private void generateUniqueKeyData(T item, JsonObject jsonObject) {
        String uniqueKeyPropertyName = arrayUpdater.getUpdateQueueData()
                .getUniqueKeyProperty();
        if (uniqueKeyPropertyName != null
                && !jsonObject.hasKey(uniqueKeyPropertyName)) {
            jsonObject.put(uniqueKeyPropertyName,
                    getUniqueKeyProvider().apply(item));
        }
    }

    protected void initConnector() {
        getUI().orElseThrow(() -> new IllegalStateException(
                "Connector can only be initialized for an attached Grid"))
                .getPage()
                .executeJs("window.Vaadin.Flow.gridConnector.initLazy($0)",
                        getElement());
    }

    /**
     * Builder for {@link DataCommunicator} object.
     *
     * @param <T>
     *            the grid bean type
     *
     * @param <U>
     *            the ArrayUpdater type
     */
    protected static class DataCommunicatorBuilder<T, U extends ArrayUpdater>
            implements Serializable {

        /**
         * Build a new {@link DataCommunicator} object for the given Grid
         * instance.
         *
         * @param element
         *            the target grid element
         * @param dataGenerator
         *            the {@link CompositeDataGenerator} for the data
         *            communicator
         * @param arrayUpdater
         *            the {@link ArrayUpdater} for the data communicator
         * @param uniqueKeyProviderSupplier
         *            the unique key value provider supplier for the data
         *            communicator
         * @return the build data communicator object
         */
        protected DataCommunicator<T> build(Element element,
                CompositeDataGenerator<T> dataGenerator, U arrayUpdater,
                SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
            return new DataCommunicator<>(
                    dataGenerator, arrayUpdater, data -> element
                            .callJsFunction("$connector.updateFlatData", data),
                    element.getNode());
        }
    }

    protected GridArrayUpdater createDefaultArrayUpdater(
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory) {
        return new GridArrayUpdaterImpl(updateQueueFactory);
    }

    /**
     * Adds a new text column to this {@link Grid} with a value provider and
     * default column factory. The value is converted to String when sent to the
     * client by using {@link String#valueOf(Object)}.
     * <p>
     * <em>NOTE:</em> For displaying components, see
     * {@link #addComponentColumn(ValueProvider)}. For using build-in renderers,
     * see {@link #addColumn(Renderer)}.
     * </p>
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     * <p>
     * <em>NOTE:</em> This method is a shorthand for
     * {@link #addColumn(ValueProvider, BiFunction)}
     * </p>
     *
     * @param valueProvider
     *            the value provider
     * @return the created column
     * @see #addComponentColumn(ValueProvider)
     * @see #addColumn(Renderer)
     * @see #removeColumn(Column)
     * @see #getDefaultColumnFactory()
     * @see #addColumn(ValueProvider, BiFunction)
     */
    public Column<T> addColumn(ValueProvider<T, ?> valueProvider) {
        BiFunction<Renderer<T>, String, Column<T>> defaultFactory = getDefaultColumnFactory();
        return addColumn(valueProvider, defaultFactory);
    }

    /**
     * Adds a new text column to this {@link Grid} with a value provider and
     * column factory provided. The value is converted to String when sent to
     * the client by using {@link String#valueOf(Object)}.
     * <p>
     * <em>NOTE:</em> For displaying components, see
     * {@link #addComponentColumn(ValueProvider)}. For using build-in renderers,
     * see {@link #addColumn(Renderer)}.
     * </p>
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * @param valueProvider
     *            the value provider
     * @param columnFactory
     *            the method that creates a new column instance for this
     *            {@link Grid} instance.
     * @return the created column
     * @see #addColumn(ValueProvider)
     * @see #addComponentColumn(ValueProvider)
     * @see #addColumn(Renderer)
     * @see #removeColumn(Column)
     */
    protected <C extends Column<T>> C addColumn(
            ValueProvider<T, ?> valueProvider,
            BiFunction<Renderer<T>, String, C> columnFactory) {
        String columnId = createColumnId(false);

        C column = addColumn(
                new ColumnPathRenderer<T>(columnId,
                        item -> formatValueToSendToTheClient(
                                applyValueProvider(valueProvider, item))),
                columnFactory);
        ((Column<T>) column).comparator = ((a, b) -> compareMaybeComparables(
                applyValueProvider(valueProvider, a),
                applyValueProvider(valueProvider, b)));
        return column;
    }

    private Object applyValueProvider(ValueProvider<T, ?> valueProvider,
            T item) {
        Object value;
        try {
            value = valueProvider.apply(item);
        } catch (NullPointerException npe) {
            value = null;
            if (NestedNullBehavior.THROW == nestedNullBehavior) {
                throw npe;
            }
        }
        return value;
    }

    private String formatValueToSendToTheClient(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    /**
     * Adds a new column that shows components.
     * <p>
     * This is a shorthand for {@link #addColumn(Renderer)} with a
     * {@link ComponentRenderer}.
     * <p>
     * <em>NOTE:</em> Using {@link ComponentRenderer} is not as efficient as the
     * built in renderers or using {@link TemplateRenderer}.
     * </p>
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * @param componentProvider
     *            a value provider that will return a component for the given
     *            item
     * @param <V>
     *            the component type
     * @return the new column
     * @see #addColumn(Renderer)
     * @see #removeColumn(Column)
     */
    public <V extends Component> Column<T> addComponentColumn(
            ValueProvider<T, V> componentProvider) {
        return addColumn(new ComponentRenderer<>(componentProvider));
    }

    /**
     * Adds a new text column to this {@link Grid} with a value provider and
     * sorting properties.The value is converted to a JSON value by using
     * {@link JsonSerializer#toJson(Object)}. The sorting properties are used to
     * configure backend sorting for this column. In-memory sorting is
     * automatically configured using the return type of the given
     * {@link ValueProvider}.
     *
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * @see Column#setComparator(ValueProvider)
     * @see Column#setSortProperty(String...)
     * @see #removeColumn(Column)
     *
     * @param valueProvider
     *            the value provider
     * @param sortingProperties
     *            the sorting properties to use with this column
     * @param <V>
     *            the type of the column
     * @return the created column
     */
    public <V extends Comparable<? super V>> Column<T> addColumn(
            ValueProvider<T, V> valueProvider, String... sortingProperties) {
        Column<T> column = addColumn(valueProvider);
        column.setComparator(valueProvider);
        column.setSortProperty(sortingProperties);
        return column;
    }

    /**
     * Adds a new text column to this {@link Grid} with a renderer and default
     * column factory.
     * <p>
     * See implementations of the {@link Renderer} interface for built-in
     * renderer options with type safe APIs. For a renderer using template
     * binding, use {@link TemplateRenderer#of(String)}.
     * <p>
     * <em>NOTE:</em> You can add component columns easily using the
     * {@link #addComponentColumn(ValueProvider)}, but using
     * {@link ComponentRenderer} is not as efficient as the built in renderers
     * or using {@link TemplateRenderer}.
     * </p>
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     * <p>
     * <em>NOTE:</em> This method is a shorthand for
     * {@link #addColumn(Renderer, BiFunction)}
     * </p>
     *
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @return the created column
     *
     * @see #getDefaultColumnFactory()
     * @see TemplateRenderer#of(String)
     * @see #addComponentColumn(ValueProvider)
     * @see #removeColumn(Column)
     * @see #addColumn(Renderer, BiFunction)
     */
    public Column<T> addColumn(Renderer<T> renderer) {
        BiFunction<Renderer<T>, String, Column<T>> defaultFactory = getDefaultColumnFactory();
        return addColumn(renderer, defaultFactory);
    }

    /**
     * Adds a new text column to this {@link Grid} with a renderer and column
     * factory provided.
     * <p>
     * See implementations of the {@link Renderer} interface for built-in
     * renderer options with type safe APIs. For a renderer using template
     * binding, use {@link TemplateRenderer#of(String)}.
     * <p>
     * <em>NOTE:</em> You can add component columns easily using the
     * {@link #addComponentColumn(ValueProvider)}, but using
     * {@link ComponentRenderer} is not as efficient as the built in renderers
     * or using {@link TemplateRenderer}.
     * </p>
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @param columnFactory
     *            the method that creates a new column instance for this
     *            {@link Grid} instance.
     * @return the created column
     *
     * @see #addColumn(Renderer)
     * @see TemplateRenderer#of(String)
     * @see #addComponentColumn(ValueProvider)
     * @see #removeColumn(Column)
     */
    protected <C extends Column<T>> C addColumn(Renderer<T> renderer,
            BiFunction<Renderer<T>, String, C> columnFactory) {
        String columnId = createColumnId(true);

        C column = columnFactory.apply(renderer, columnId);
        idToColumnMap.put(columnId, column);
        column.getElement().setProperty("_flowId", columnId);

        /*
         * Properties don't automatically synchronize to non-visible columns.
         * This bypasses the limitation in order to set the _flowId property for
         * hidden columns also (needed by column reorder event).
         */
        column.addAttachListener(e -> {
            e.getUI().beforeClientResponse(this, ctx -> {
                // Make sure the non-visible column is still attached to UI
                if (!column.isVisible() && column.getUI().isPresent()) {
                    int nodeId = column.getElement().getNode().getId();
                    String appId = e.getUI().getInternals().getAppId();
                    this.getElement().executeJs(
                            "Vaadin.Flow.clients[$0].getByNodeId($1)._flowId = $2",
                            appId, nodeId, columnId);
                }
            });
        });

        AbstractColumn<?> current = column;
        columnLayers.get(0).addColumn(column);

        for (int i = 1; i < columnLayers.size(); i++) {
            ColumnGroup group = new ColumnGroup(this, current);
            columnLayers.get(i).addColumn(group);
            current = group;
        }
        getElement().appendChild(current.getElement());

        getDataCommunicator().reset();

        return column;
    }

    /**
     * Creates a new column instance for this {@link Grid} instance.
     * <p>
     * This method must not return <code>null</code>.
     *
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @param columnId
     *            internal column id
     * @return column instance
     * @deprecated This method should not be used outside.
     *             {@link #getDefaultColumnFactory} should be used instead.
     * @see #createColumnId(boolean)
     * @see Renderer
     */
    @Deprecated
    protected Column<T> createColumn(Renderer<T> renderer, String columnId) {
        return new Column<>(this, columnId, renderer);
    }

    /**
     * Gives a reference to the column factory.
     * <p>
     * This method must not return <code>null</code>.
     *
     * @return method for column creation
     */
    protected BiFunction<Renderer<T>, String, Column<T>> getDefaultColumnFactory() {
        return this::createColumn;
    }

    /**
     * Adds a new text column to this {@link Grid} with a template renderer,
     * sorting properties and default column factory. The values inside the
     * renderer are converted to JSON values by using
     * {@link JsonSerializer#toJson(Object)}.
     * <p>
     * <em>NOTE:</em> You can add component columns easily using the
     * {@link #addComponentColumn(ValueProvider)}, but using
     * {@link ComponentRenderer} is not as efficient as the built in renderers
     * or using {@link TemplateRenderer}.
     * <p>
     * This constructor attempts to automatically configure both in-memory and
     * backend sorting using the given sorting properties and matching those
     * with the property names used in the given renderer.
     * <p>
     * <strong>Note:</strong> if a property of the renderer that is used as a
     * sorting property does not extend Comparable, no in-memory sorting is
     * configured for it.
     *
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * <p>
     * <strong>Note:</strong> This method is a shorthand for
     * {@link ##addColumn(Renderer, BiFunction, String...)}
     * </p>
     *
     * @see #getDefaultColumnFactory()
     * @see #addColumn(Renderer, BiFunction, String...)
     * @see #removeColumn(Column)
     *
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @param sortingProperties
     *            the sorting properties to use for this column
     * @return the created column
     */
    public Column<T> addColumn(Renderer<T> renderer,
            String... sortingProperties) {
        BiFunction<Renderer<T>, String, Column<T>> defaultFactory = getDefaultColumnFactory();
        return addColumn(renderer, defaultFactory, sortingProperties);
    }

    /**
     * Adds a new text column to this {@link Grid} with a template renderer,
     * sorting properties and column factory provided. The values inside the
     * renderer are converted to JSON values by using
     * {@link JsonSerializer#toJson(Object)}.
     * <p>
     * <em>NOTE:</em> You can add component columns easily using the
     * {@link #addComponentColumn(ValueProvider)}, but using
     * {@link ComponentRenderer} is not as efficient as the built in renderers
     * or using {@link TemplateRenderer}.
     * <p>
     * This constructor attempts to automatically configure both in-memory and
     * backend sorting using the given sorting properties and matching those
     * with the property names used in the given renderer.
     * <p>
     * <strong>Note:</strong> if a property of the renderer that is used as a
     * sorting property does not extend Comparable, no in-memory sorting is
     * configured for it.
     *
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * @see #addColumn(Renderer, String...)
     * @see #removeColumn(Column)
     *
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @param columnFactory
     *            the method that creates a new column instance for this
     *            {@link Grid} instance.
     * @param sortingProperties
     *            the sorting properties to use for this column
     * @return the created column
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <C extends Column<T>> C addColumn(Renderer<T> renderer,
            BiFunction<Renderer<T>, String, C> columnFactory,
            String... sortingProperties) {
        C column = addColumn(renderer, columnFactory);

        Map<String, ValueProvider<T, ?>> valueProviders = renderer
                .getValueProviders();
        Set<String> valueProvidersKeySet = valueProviders.keySet();
        List<String> matchingSortingProperties = Arrays
                .stream(sortingProperties)
                .filter(valueProvidersKeySet::contains)
                .collect(Collectors.toList());

        column.setSortProperty(matchingSortingProperties
                .toArray(new String[matchingSortingProperties.size()]));
        Comparator<T> combinedComparator = (a, b) -> 0;
        Comparator nullsLastComparator = Comparator
                .nullsLast(Comparator.naturalOrder());
        for (String sortProperty : matchingSortingProperties) {
            ValueProvider<T, ?> provider = valueProviders.get(sortProperty);
            combinedComparator = combinedComparator.thenComparing((a, b) -> {
                Object aa = provider.apply(a);
                if (!(aa instanceof Comparable)) {
                    return 0;
                }
                Object bb = provider.apply(b);
                return nullsLastComparator.compare(aa, bb);
            });
        }

        return column;
    }

    /**
     * Adds a new column for the given property name with the default column
     * factory. The property values are converted to Strings in the grid cells.
     * The property's full name will be used as the {@link Column#setKey(String)
     * column key} and the property caption will be used as the
     * {@link Column#setHeader(String) column header}.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * If the property is {@link Comparable}, the created column is sortable by
     * default. This can be changed with the {@link Column#setSortable(boolean)}
     * method.
     * <p>
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     *
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * <p>
     * <strong>Note:</strong> This method is a shorthand for
     * {@link #addColumn(String, BiFunction)}
     * </p>
     *
     * @see #getDefaultColumnFactory()
     * @see #addColumn(String, BiFunction)
     * @see #removeColumn(Column)
     *
     * @param propertyName
     *            the property name of the new column, not <code>null</code>
     * @return the created column
     */
    public Column<T> addColumn(String propertyName) {
        BiFunction<Renderer<T>, String, Column<T>> defaultFactory = getDefaultColumnFactory();
        return addColumn(propertyName, defaultFactory);
    }

    /**
     * Adds a new column for the given property name with the column factory
     * provided. The property values are converted to Strings in the grid cells.
     * The property's full name will be used as the {@link Column#setKey(String)
     * column key} and the property caption will be used as the
     * {@link Column#setHeader(String) column header}.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * If the property is {@link Comparable}, the created column is sortable by
     * default. This can be changed with the {@link Column#setSortable(boolean)}
     * method.
     * <p>
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     *
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * @see #addColumn(String)
     * @see #removeColumn(Column)
     *
     * @param propertyName
     *            the property name of the new column, not <code>null</code>
     * @param columnFactory
     *            the method that creates a new column instance for this
     *            {@link Grid} instance.
     * @return the created column
     */
    protected <C extends Column<T>> C addColumn(String propertyName,
            BiFunction<Renderer<T>, String, C> columnFactory) {
        checkForBeanGrid();
        Objects.requireNonNull(propertyName, "Property name can't be null");

        PropertyDefinition<T, ?> property;
        try {
            property = propertySet.getProperty(propertyName).get();
        } catch (NoSuchElementException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Can't resolve property name '"
                    + propertyName + "' from '" + propertySet + "'");
        }
        return addColumn(property, columnFactory);
    }

    private Column<T> addColumn(PropertyDefinition<T, ?> property) {
        BiFunction<Renderer<T>, String, Column<T>> defaultFactory = getDefaultColumnFactory();
        return addColumn(property, defaultFactory);
    }

    private <C extends Column<T>> C addColumn(PropertyDefinition<T, ?> property,
            BiFunction<Renderer<T>, String, C> columnFactory) {
        @SuppressWarnings("unchecked")
        C column = (C) addColumn(item -> runPropertyValueGetter(property, item),
                columnFactory).setHeader(property.getCaption());
        try {
            column.setKey(property.getName());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Multiple columns for the same property: "
                            + property.getName());
        }

        if (Comparable.class.isAssignableFrom(property.getType())) {
            column.setSortable(true);
        }
        return column;
    }

    private Object runPropertyValueGetter(PropertyDefinition<T, ?> property,
            T item) {
        return property.getGetter().apply(item);
    }

    /**
     * Adds a new columns for the given property names. The property values are
     * converted to Strings in the grid cells. The properties' full names will
     * be used as the {@link Column#setKey(String) column key} and the
     * properties' caption will be used as the {@link Column#setHeader(String)
     * column header}.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * If the property is {@link Comparable}, the created column is sortable by
     * default. This can be changed with the {@link Column#setSortable(boolean)}
     * method.
     * <p>
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     *
     * <p>
     * Every added column sends data to the client side regardless of its
     * visibility state. Don't add a new column at all or use
     * {@link Grid#removeColumn(Column)} to avoid sending extra data.
     * </p>
     *
     * @param propertyNames
     *            the property names of the new columns, not <code>null</code>
     * @see #addColumn(String)
     * @see #removeColumn(Column)
     */
    public void addColumns(String... propertyNames) {
        checkForBeanGrid();
        Objects.requireNonNull(propertyNames, "Property names can't be null");
        Stream.of(propertyNames).forEach(this::addColumn);
    }

    /**
     * Sets the columns and their order based on the given properties.
     * <p>
     * This is a shortcut for removing all columns and then calling
     * {@link #addColumn(String)} for each of the given propertyNames.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * Note that this also resets the headers and footers.
     * <p>
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     *
     * @param propertyNames
     *            the properties to create columns for
     */
    public void setColumns(String... propertyNames) {
        checkForBeanGrid();
        getColumns().forEach(this::removeColumn);
        Stream.of(propertyNames).forEach(this::addColumn);
    }

    /**
     * Sets the defined columns as sortable, based on the given property names.
     * <p>
     * This is a shortcut for setting all columns not sortable and then calling
     * {@link Column#setSortable(boolean)} for each of the columns defined by
     * the given propertyNames.
     * <p>
     * You can set sortable columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     *
     * @param propertyNames
     *            the property names used to reference the columns
     *
     * @throws IllegalArgumentException
     *             if any of the propertyNames refers to a non-existing column
     *
     * @see #setColumns(String...)
     * @see #getColumnByKey(String)
     */
    public void setSortableColumns(String... propertyNames) {
        checkForBeanGrid();
        getColumns().forEach(col -> col.setSortable(false));
        for (String property : propertyNames) {
            Column<T> column = getColumnByKey(property);
            if (column == null) {
                throw new IllegalArgumentException(
                        "The column for the property '" + property
                                + "' could not be found");
            }
            column.setSortable(true);
        }
    }

    private void checkForBeanGrid() {
        if (propertySet == null) {
            throw new UnsupportedOperationException(
                    "This method can't be used for a Grid that isn't constructed from a bean type. "
                            + "To construct Grid from a bean type, please provide a beanType argument"
                            + "to the constructor: Grid<Person> grid = new Grid<>(Person.class)");
        }
    }

    /**
     * Sets a user-defined identifier for given column.
     *
     * @see Column#setKey(String)
     *
     * @param column
     *            the column
     * @param key
     *            the user-defined identifier
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void setColumnKey(String key, Column column) {
        if (keyToColumnMap.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Duplicate key for columns: " + key);
        }
        keyToColumnMap.put(key, column);
    }

    protected String createColumnId(boolean increment) {
        int id = nextColumnId;
        if (increment) {
            nextColumnId++;
        }
        return "col" + id;
    }

    /**
     * Adds a new header row on the top of the existing header rows.
     * <p>
     * If there are no existing header rows, this will create the first row.
     *
     * @return the created header row
     */
    public HeaderRow prependHeaderRow() {
        if (getHeaderRows().size() == 0) {
            return addFirstHeaderRow();
        }
        return insertColumnLayer(getLastHeaderLayerIndex() + 1).asHeaderRow();
    }

    /**
     * Adds a new header row to the bottom of the existing header rows.
     * <p>
     * If there are no existing header rows, this will create the first row.
     *
     * @return the created header row
     */
    public HeaderRow appendHeaderRow() {
        if (getHeaderRows().size() == 0) {
            return addFirstHeaderRow();
        }
        return insertInmostColumnLayer(true, false).asHeaderRow();
    }

    protected HeaderRow addFirstHeaderRow() {
        defaultHeaderRow = columnLayers.get(0).asHeaderRow();
        columnLayers.get(0).updateSortingIndicators(true);
        return defaultHeaderRow;
    }

    protected HeaderRow getDefaultHeaderRow() {
        return defaultHeaderRow;
    }

    /**
     * Adds a new footer row on the top of the existing footer rows.
     * <p>
     * If there are no existing footer rows, this will create the first row.
     *
     * @return the created footer row
     */
    public FooterRow prependFooterRow() {
        if (getFooterRows().size() == 0) {
            return columnLayers.get(0).asFooterRow();
        }
        return insertInmostColumnLayer(false, true).asFooterRow();
    }

    /**
     * Adds a new footer row to the bottom of the existing footer rows.
     * <p>
     * If there are no existing footer rows, this will create the first row.
     *
     * @return the created header row
     */
    public FooterRow appendFooterRow() {
        if (getFooterRows().size() == 0) {
            return columnLayers.get(0).asFooterRow();
        }
        return insertColumnLayer(getLastFooterLayerIndex() + 1).asFooterRow();
    }

    protected List<ColumnLayer> getColumnLayers() {
        return Collections.unmodifiableList(columnLayers);
    }

    /**
     * Gets all of the header rows in the Grid, in order from top to bottom.
     *
     * @return the header rows of the Grid
     */
    public List<HeaderRow> getHeaderRows() {
        List<HeaderRow> rows = columnLayers.stream()
                .filter(ColumnLayer::isHeaderRow).map(ColumnLayer::asHeaderRow)
                .collect(Collectors.toList());
        Collections.reverse(rows);
        return rows;
    }

    /**
     * Gets all of the footer rows in the Grid, in order from top to bottom.
     *
     * @return the footer rows of the Grid
     */
    public List<FooterRow> getFooterRows() {
        return columnLayers.stream().filter(ColumnLayer::isFooterRow)
                .map(ColumnLayer::asFooterRow).collect(Collectors.toList());
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(GridVariant... variants) {
        getThemeNames().addAll(Stream.of(variants)
                .map(GridVariant::getVariantName).collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(GridVariant... variants) {
        getThemeNames().removeAll(Stream.of(variants)
                .map(GridVariant::getVariantName).collect(Collectors.toList()));
    }

    /**
     * Creates a new layer containing same amount of column-groups as the next
     * inner layer, adds it to layers list and returns the layer.
     *
     * @param index
     *            index to insert to, must be > 0
     */
    private ColumnLayer insertColumnLayer(int index) {

        ColumnLayer innerLayer = columnLayers.get(index - 1);
        List<AbstractColumn<?>> groups = ColumnGroupHelpers
                .wrapInSeparateColumnGroups(innerLayer.getColumns(), this);

        ColumnLayer layer = new ColumnLayer(this, groups);
        columnLayers.add(index, layer);

        return layer;
    }

    /**
     * Creates a new layer from the provided columns, inserts the layer into
     * given index and returns the new layer.
     * <p>
     * The user of this method should make sure that the DOM corresponds the
     * column layer structure.
     *
     * @param index
     *            the index to insert
     * @param columns
     *            the column components that the new layer will wrap
     * @return the new layer
     */
    protected ColumnLayer insertColumnLayer(int index,
            List<AbstractColumn<?>> columns) {
        ColumnLayer layer = new ColumnLayer(this, columns);
        columnLayers.add(index, layer);
        return layer;
    }

    /**
     * Removes the given layer and moves the columns on the lower level to its
     * place.
     *
     * @param layer
     *            the layer to remove, not the bottom layer
     */
    protected void removeColumnLayer(ColumnLayer layer) {
        if (layer.equals(columnLayers.get(0))) {
            throw new IllegalArgumentException(
                    "The bottom column layer cannot be removed");
        }
        layer.getColumns().forEach(column -> {
            Element parent = column.getElement().getParent();
            int insertIndex = parent.indexOfChild(column.getElement());
            parent.insertChild(insertIndex,
                    ((ColumnGroup) column).getChildColumns().stream()
                            .map(HasElement::getElement)
                            .toArray(Element[]::new));
            column.getElement().removeFromParent();
        });
        columnLayers.remove(layer);
    }

    private ColumnLayer insertInmostColumnLayer(boolean forHeaderRow,
            boolean forFooterRow) {
        ColumnLayer bottomLayer = columnLayers.get(0);
        List<AbstractColumn<?>> columns = bottomLayer.getColumns();

        List<AbstractColumn<?>> groups = ColumnGroupHelpers
                .wrapInSeparateColumnGroups(columns, this);

        ColumnLayer newBottomLayer = new ColumnLayer(this, columns);

        IntStream.range(0, groups.size()).forEach(i -> {
            // Move templates from columns to column-groups
            if (forFooterRow) {
                groups.get(i)
                        .setFooterRenderer(columns.get(i).getFooterRenderer());
                columns.get(i).setFooterRenderer(null);
            }
            if (forHeaderRow) {
                groups.get(i)
                        .setHeaderRenderer(columns.get(i).getHeaderRenderer());
                columns.get(i).setHeaderRenderer(null);
            }
        });

        if (forFooterRow && bottomLayer.isHeaderRow()) {
            // Keep headers in the inner-most layer
            newBottomLayer.setHeaderRow(bottomLayer.asHeaderRow());
            bottomLayer.setHeaderRow(null);
        }
        if (forHeaderRow && bottomLayer.isFooterRow()) {
            // Keep footers in the inner-most layer
            newBottomLayer.setFooterRow(bottomLayer.asFooterRow());
            bottomLayer.setFooterRow(null);
        }

        bottomLayer.setColumns(groups);

        columnLayers.add(0, newBottomLayer);

        if (bottomLayer.isHeaderRow()
                && bottomLayer.asHeaderRow().equals(defaultHeaderRow)) {
            bottomLayer.updateSortingIndicators(true);
            newBottomLayer.updateSortingIndicators(false);
        }

        return newBottomLayer;
    }

    /**
     * Gets the last index of a column layer that is a header layer
     */
    private int getLastHeaderLayerIndex() {
        for (int i = columnLayers.size() - 1; i >= 0; i--) {
            if (columnLayers.get(i).isHeaderRow()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the last index of a column layer that is a footer layer
     */
    private int getLastFooterLayerIndex() {
        for (int i = columnLayers.size() - 1; i >= 0; i--) {
            if (columnLayers.get(i).isFooterRow()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to either {@link GridListDataView} or
     *             {@link GridLazyDataView}
     */
    @Deprecated
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider, "data provider cannot be null");
        handleDataProviderChange(dataProvider);

        deselectAll();
        filterSlot = getDataCommunicator().setDataProvider(dataProvider, null);

        /*
         * The visibility of the selectAll checkbox depends on whether the
         * DataProvider is inMemory or not. When changing the DataProvider, its
         * visibility needs to be revalidated.
         */
        if (getSelectionModel() instanceof GridMultiSelectionModel) {
            GridMultiSelectionModel<T> model = (GridMultiSelectionModel<T>) getSelectionModel();
            model.setSelectAllCheckboxVisibility(
                    model.getSelectAllCheckboxVisibility());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Because the stream is collected to a list anyway, use
     *             {@link HasListDataView#setItems(Collection)} or
     *             {@link #setItems(CallbackDataProvider.FetchCallback)}
     *             instead.
     */
    @Deprecated
    public void setItems(Stream<T> streamOfItems) {
        setItems(DataProvider.fromStream(streamOfItems));
    }

    /**
     * Returns the data provider of this grid.
     * <p>
     * To get information and control over the items in the grid, use either
     * {@link #getListDataView()} or {@link #getLazyDataView()} instead.
     *
     * @return the data provider of this grid, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return getDataCommunicator().getDataProvider();
    }

    @Override
    public GridDataView<T> setItems(DataProvider<T, Void> dataProvider) {
        setDataProvider(dataProvider);
        return getGenericDataView();
    }

    @Override
    public GridDataView<T> setItems(
            InMemoryDataProvider<T> inMemoryDataProvider) {
        // We don't use DataProvider.withConvertedFilter() here because it's
        // implementation does not apply the filter converter if Query has a
        // null filter
        DataProvider<T, Void> convertedDataProvider = new DataProviderWrapper<T, Void, SerializablePredicate<T>>(
                inMemoryDataProvider) {
            @Override
            protected SerializablePredicate<T> getFilter(Query<T, Void> query) {
                // Just ignore the query filter (Void) and apply the
                // predicate only
                return Optional.ofNullable(inMemoryDataProvider.getFilter())
                        .orElse(item -> true);
            }
        };
        return setItems(convertedDataProvider);
    }

    /**
     * Gets the generic data view for the grid. This data view should only be
     * used when {@link #getListDataView()} or {@link #getLazyDataView()} is not
     * applicable for the underlying data provider.
     *
     * @return the generic {@link DataView} implementation for grid
     * @see #getListDataView()
     * @see #getLazyDataView()
     */
    @Override
    public GridDataView<T> getGenericDataView() {
        return new GridDataView<>(getDataCommunicator(), this);
    }

    @Override
    public GridListDataView<T> setItems(ListDataProvider<T> dataProvider) {
        setDataProvider(dataProvider);
        return getListDataView();
    }

    /**
     * Gets the list data view for the grid. This data view should only be used
     * when the items are in-memory set with:
     * <ul>
     * <li>{@link #setItems(Collection)}</li>
     * <li>{@link #setItems(Object[])}</li>
     * <li>{@link #setItems(ListDataProvider)}</li>
     * </ul>
     * If the items are not in-memory an exception is thrown. When the items are
     * fetched lazily, use {@link #getLazyDataView()} instead.
     *
     * @return the list data view that provides access to the items in the grid
     */
    @Override
    public GridListDataView<T> getListDataView() {
        return new GridListDataView<>(getDataCommunicator(), this,
                this::onInMemoryFilterOrSortingChange);
    }

    // Overridden for now to delegate to setDataProvider for setup
    @Override
    public GridLazyDataView<T> setItems(
            BackEndDataProvider<T, Void> dataProvider) {
        setDataProvider(dataProvider);
        return getLazyDataView();
    }

    /**
     * Gets the lazy data view for the grid. This data view should only be used
     * when the items are provided lazily from the backend with:
     * <ul>
     * <li>{@link #setItems(CallbackDataProvider.FetchCallback)}</li>
     * <li>{@link #setItems(CallbackDataProvider.FetchCallback, CallbackDataProvider.CountCallback)}</li>
     * <li>{@link #setItems(BackEndDataProvider)}</li>
     * </ul>
     * If the items are not fetched lazily an exception is thrown. When the
     * items are in-memory, use {@link #getListDataView()} instead.
     *
     * @return the lazy data view that provides access to the data bound to the
     *         grid
     */
    @Override
    public GridLazyDataView<T> getLazyDataView() {
        return new GridLazyDataView<>(getDataCommunicator(), this);
    }

    /**
     * Returns the data communicator of this Grid.
     *
     * @return the data communicator, not {@code null}
     */
    public DataCommunicator<T> getDataCommunicator() {
        return dataCommunicator;
    }

    /**
     * Gets the current page size, which is the number of items fetched at a
     * time from the dataprovider.
     *
     * @return the current page size
     */
    public int getPageSize() {
        return getElement().getProperty("pageSize", 50);
    }

    /**
     * Sets the page size, which is the number of items fetched at a time from
     * the data provider. With the default value of {@code 50}, the grid might
     * fetch items for example as: {@code 0-49, 50-149, 150-200...}.
     * <p>
     * <em>Note:</em> the number of items in the server-side memory can be
     * considerably higher than the page size, since the component can show more
     * than one page at a time.
     * <p>
     * Setting the pageSize after the Grid has been rendered effectively resets
     * the component, and the current page(s) and sent over again.
     * <p>
     * With automatically extending grid, controlling the item count and how
     * much it is increased when scrolling is possible via
     * {@link #getLazyDataView()}.
     *
     * @param pageSize
     *            the maximum number of items sent per request. Should be
     *            greater than zero
     */
    public void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException(
                    "The pageSize should be greater than zero. Was "
                            + pageSize);
        }
        getElement().setProperty("pageSize", pageSize);
        getElement().callJsFunction("$connector.reset");
        getDataCommunicator().setPageSize(pageSize);
        setRequestedRange(0, pageSize);
        getDataCommunicator().reset();
    }

    /**
     * Returns the selection model for this grid.
     *
     * @return the selection model, not null
     */
    public GridSelectionModel<T> getSelectionModel() {
        assert selectionModel != null : "No selection model set by "
                + getClass().getName() + " constructor";
        return selectionModel;
    }

    /**
     * Sets the selection model for the grid.
     * <p>
     * This method is for setting a custom selection model, and is
     * {@code protected} because {@link #setSelectionMode(SelectionMode)} should
     * be used for easy switching between built-in selection models.
     * <p>
     * The default selection model is {@link GridSingleSelectionModel}.
     * <p>
     * To use a custom selection model, you can e.g. extend the grid call this
     * method with your custom selection model.
     *
     * @param model
     *            the selection model to use, not {@code null}
     * @param selectionMode
     *            the selection mode this selection model corresponds to, not
     *            {@code null}
     *
     * @see #setSelectionMode(SelectionMode)
     */
    protected void setSelectionModel(GridSelectionModel<T> model,
            SelectionMode selectionMode) {
        Objects.requireNonNull(model, "selection model cannot be null");
        Objects.requireNonNull(selectionMode, "selection mode cannot be null");
        if (selectionModel != null
                && selectionModel instanceof AbstractGridExtension) {
            ((AbstractGridExtension<?>) selectionModel).remove();
        }
        selectionModel = model;
        this.selectionMode = selectionMode;
        updateSelectionModeOnClient();
    }

    protected void updateSelectionModeOnClient() {
        getElement().callJsFunction("$connector.setSelectionMode",
                selectionMode.name());
    }

    /**
     * Sets the grid's selection mode.
     * <p>
     * To use your custom selection model, you can use
     * {@link #setSelectionModel(GridSelectionModel, SelectionMode)}, see
     * existing selection model implementations for example.
     *
     * @param selectionMode
     *            the selection mode to switch to, not {@code null}
     * @return the used selection model
     *
     * @see SelectionMode
     * @see GridSelectionModel
     * @see #setSelectionModel(GridSelectionModel, SelectionMode)
     */
    public GridSelectionModel<T> setSelectionMode(SelectionMode selectionMode) {
        Objects.requireNonNull(selectionMode, "Selection mode cannot be null.");
        GridSelectionModel<T> model = selectionMode.createModel(this);
        setSelectionModel(model, selectionMode);
        return model;
    }

    /**
     * Use this grid as a single select in {@link Binder}.
     * <p>
     * Throws {@link IllegalStateException} if the grid is not using a
     * {@link GridSingleSelectionModel}.
     *
     * @return the single select wrapper that can be used in binder
     * @throws IllegalStateException
     *             if not using a single selection model
     */
    public SingleSelect<Grid<T>, T> asSingleSelect() {
        GridSelectionModel<T> model = getSelectionModel();
        if (!(model instanceof GridSingleSelectionModel)) {
            throw new IllegalStateException(
                    "Grid is not in single select mode, "
                            + "it needs to be explicitly set to such with "
                            + "setSelectionMode(SelectionMode.SINGLE) before "
                            + "being able to use single selection features.");
        }
        return ((GridSingleSelectionModel<T>) model).asSingleSelect();
    }

    /**
     * Use this grid as a multiselect in {@link Binder}.
     * <p>
     * Throws {@link IllegalStateException} if the grid is not using a
     * {@link GridMultiSelectionModel}.
     *
     * @return the multiselect wrapper that can be used in binder
     * @throws IllegalStateException
     *             if not using a multiselection model
     */
    public MultiSelect<Grid<T>, T> asMultiSelect() {
        GridSelectionModel<T> model = getSelectionModel();
        if (!(model instanceof GridMultiSelectionModel)) {
            throw new IllegalStateException("Grid is not in multi select mode, "
                    + "it needs to be explicitly set to such with "
                    + "setSelectionMode(SelectionMode.MULTI) before "
                    + "being able to use multi selection features.");
        }
        return ((GridMultiSelectionModel<T>) model).asMultiSelect();
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @see #getSelectionModel()
     * @see GridSelectionModel
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
     * @see GridSelectionModel
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
     * @see GridSelectionModel
     */
    public void deselect(T item) {
        getSelectionModel().deselect(item);
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @see #getSelectionModel()
     * @see GridSelectionModel
     */
    public void deselectAll() {
        getSelectionModel().deselectAll();
    }

    void doClientSideSelection(Set<T> items) {
        callSelectionFunctionForItems("doSelection", items);
    }

    void doClientSideDeselection(Set<T> items) {
        callSelectionFunctionForItems("doDeselection", items);
    }

    boolean isInActiveRange(T item) {
        return getDataCommunicator().getKeyMapper().has(item);
    }

    private void callSelectionFunctionForItems(String function, Set<T> items) {
        if (items.isEmpty()) {
            return;
        }
        JsonArray jsonArray = Json.createArray();
        for (T item : items) {
            JsonObject jsonObject = item != null
                    ? generateJsonForSelection(item)
                    : null;
            jsonArray.set(jsonArray.length(), jsonObject);
        }
        final SerializableRunnable jsFunctionCall = () -> getElement()
                .callJsFunction("$connector." + function, jsonArray, false);
        if (getElement().getNode().isAttached()) {
            jsFunctionCall.run();
        } else {
            getElement().getNode()
                    .runWhenAttached(ui -> ui.beforeClientResponse(this,
                            context -> jsFunctionCall.run()));
        }
    }

    private JsonObject generateJsonForSelection(T item) {
        JsonObject json = Json.createObject();
        json.put("key", getDataCommunicator().getKeyMapper().key(item));
        return json;
    }

    /**
     * Adds a selection listener to the current selection model.
     * <p>
     * This is a shorthand for
     * {@code grid.getSelectionModel().addSelectionListener()}. To get more
     * detailed selection events, use {@link #getSelectionModel()} and either
     * {@link GridSingleSelectionModel#addSingleSelectionListener(SingleSelectionListener)}
     * or
     * {@link GridMultiSelectionModel#addMultiSelectionListener(MultiSelectionListener)}
     * depending on the used selection mode.
     *
     * @param listener
     *            the listener to add
     * @return a registration handle to remove the listener
     * @throws UnsupportedOperationException
     *             if selection has been disabled with
     *             {@link SelectionMode#NONE}
     */
    public Registration addSelectionListener(
            SelectionListener<Grid<T>, T> listener) {
        return getSelectionModel().addSelectionListener(listener);
    }

    /**
     * Set the renderer to use for displaying the item details rows in this
     * grid.
     *
     * @param renderer
     *            the renderer to use for displaying item details rows,
     *            {@code null} to remove the current renderer
     */
    public void setItemDetailsRenderer(Renderer<T> renderer) {
        detailsRenderingRegistrations.forEach(Registration::remove);
        detailsRenderingRegistrations.clear();

        if (renderer == null) {
            return;
        }

        Rendering<T> rendering;
        if (renderer instanceof LitRenderer) {
            // LitRenderer
            if (detailsTemplate != null
                    && detailsTemplate.getParent() != null) {
                getElement().removeChild(detailsTemplate);
            }
            rendering = ((LitRenderer<T>) renderer).render(getElement(),
                    dataCommunicator.getKeyMapper(), "rowDetailsRenderer");
        } else {
            // TemplateRenderer or ComponentRenderer
            if (detailsTemplate == null) {
                rendering = renderer.render(getElement(),
                        getDataCommunicator().getKeyMapper());
                detailsTemplate = rendering.getTemplateElement();
                detailsTemplate.setAttribute("class", "row-details");
            } else {
                getElement().appendChild(detailsTemplate);
                rendering = renderer.render(getElement(),
                        getDataCommunicator().getKeyMapper(), detailsTemplate);
            }
        }

        rendering.getDataGenerator().ifPresent(renderingDataGenerator -> {
            itemDetailsDataGenerator = renderingDataGenerator;
            Registration detailsRenderingDataGeneratorRegistration = () -> {
                detailsManager.destroyAllData();
                itemDetailsDataGenerator = null;
            };
            detailsRenderingRegistrations
                    .add(detailsRenderingDataGeneratorRegistration);
        });

        detailsRenderingRegistrations.add(rendering.getRegistration());
    }

    /**
     * Returns whether column reordering is allowed. Default value is
     * {@code false}.
     *
     * @return true if reordering is allowed
     */
    @Synchronize("column-reordering-allowed-changed")
    public boolean isColumnReorderingAllowed() {
        return getElement().getProperty("columnReorderingAllowed", false);
    }

    /**
     * Sets whether or not column reordering is allowed. Default value is
     * {@code false}.
     *
     * @param columnReorderingAllowed
     *            specifies whether column reordering is allowed
     */
    public void setColumnReorderingAllowed(boolean columnReorderingAllowed) {
        if (isColumnReorderingAllowed() != columnReorderingAllowed) {
            getElement().setProperty("columnReorderingAllowed",
                    columnReorderingAllowed);
        }
    }

    /**
     * Gets a list of columns and/or column groups that are direct
     * child-elements of this grid (those that are top-most in the column
     * hierarchy).
     *
     * @return top-level columns and/or column groups of this grid
     */
    private List<ColumnBase<?>> getTopLevelColumns() {
        return getElement().getChildren().map(element -> element.getComponent())
                .filter(component -> component.isPresent()
                        && component.get() instanceof ColumnBase<?>)
                .map(component -> (ColumnBase<?>) component.get())
                .collect(Collectors.toList());
    }

    /**
     * Gets an unmodifiable list of all {@link Column}s currently in this
     * {@link Grid}.
     * <p>
     * <strong>Note:</strong> If column reordering is enabled with
     * {@link #setColumnReorderingAllowed(boolean)} and the user has reordered
     * the columns, the order of the list returned by this method might not be
     * correct.
     *
     * @return unmodifiable list of columns
     */
    public List<Column<T>> getColumns() {
        List<Column<T>> ret = new ArrayList<>();
        getTopLevelColumns().forEach(column -> appendChildColumns(ret, column));
        return Collections.unmodifiableList(ret);
    }

    /**
     * Gets a {@link Column} of this grid by its key.
     *
     * @see Column#setKey(String)
     *
     * @param columnKey
     *            the identifier key of the column to get
     * @return the column corresponding to the given column key, or {@code null}
     *         if no column has such key
     */
    public Column<T> getColumnByKey(String columnKey) {
        return keyToColumnMap.get(columnKey);
    }

    /**
     * Gets a {@link Column} of this grid by its internal id ({@code _flowId}).
     *
     * @param internalId
     *            the internal identifier of the column to get
     * @return the column corresponding to the given column identifier, or
     *         {@code null} if no column has such an identifier
     */
    Column<T> getColumnByInternalId(String internalId) {
        return idToColumnMap.get(internalId);
    }

    /**
     * Removes a column with the given column key from the Grid.
     *
     * @param columnKey
     *            the key of the column, assigned by
     *            {@link Column#setKey(String)}, or automatically created when
     *            using {@link Grid#Grid(Class)}. Cannot be <code>null</code>
     * @throws IllegalArgumentException
     *             if the column is not part of this Grid
     */
    public void removeColumnByKey(String columnKey) {
        Objects.requireNonNull(columnKey, "columnKey should not be null");

        Column<T> columnByKey = getColumnByKey(columnKey);
        if (columnByKey == null) {
            throw new IllegalArgumentException("The column with key '"
                    + columnKey + "' is not part of this Grid");
        }
        removeColumn(columnByKey);
    }

    void ensureOwner(Column<T> column) {
        if (!column.getGrid().equals(this)
                || column.getElement().getParent() == null) {
            throw new IllegalArgumentException("The column with key '"
                    + column.getKey() + "' is not owned by this Grid");
        }
    }

    /**
     * Removes a column from the Grid.
     *
     * @param column
     *            the column to be removed, not <code>null</code>
     * @throws NullPointerException
     *             if the column is {@code null}
     * @throws IllegalArgumentException
     *             if the column is not owned by this Grid
     */
    public void removeColumn(Column<T> column) {
        Objects.requireNonNull(column, "column should not be null");

        ensureOwner(column);
        List<GridSortOrder<T>> order = new ArrayList<>();
        setSortOrder(order, false);
        removeColumnAndColumnGroupsIfNeeded(column);
        column.destroyDataGenerators();
        keyToColumnMap.remove(column.getKey());
        idToColumnMap.remove(column.getInternalId());
    }

    /**
     * Removes columns from the Grid. Does nothing if the array is empty.
     *
     * @param columns
     *            the columns to be removed, not <code>null</code>
     * @throws NullPointerException
     *             if the column is {@code null}
     * @throws IllegalArgumentException
     *             if the column is not owned by this Grid
     */
    public void removeColumns(Column<T>... columns) {
        for (Column<T> column : columns) {
            removeColumn(column);
        }
    }

    /**
     * Removes all columns from this Grid.
     */
    public void removeAllColumns() {
        getColumns().forEach(c -> removeColumn(c));
    }

    private void removeColumnAndColumnGroupsIfNeeded(Column<?> column) {
        Component parent = column.getParent().get();
        parent.getElement().removeChild(column.getElement());
        columnLayers.get(0).removeColumn(column);
        if (!parent.equals(this)) {
            removeEmptyColumnGroups((ColumnGroup) parent, 1);
        }
    }

    private void removeEmptyColumnGroups(ColumnGroup columnGroup,
            int columnLayerIndex) {
        Component parent = columnGroup.getParent().get();
        if (columnGroup.getChildColumns().size() == 0) {
            parent.getElement().removeChild(columnGroup.getElement());
            columnLayers.get(columnLayerIndex).removeColumn(columnGroup);
            if (!parent.equals(this)) {
                removeEmptyColumnGroups((ColumnGroup) parent,
                        columnLayerIndex + 1);
            }
        }
    }

    /**
     * Sets the visibility of details component for given item.
     *
     * @param item
     *            the item to show details for
     * @param visible
     *            {@code true} if details component should be visible;
     *            {@code false} if it should be hidden
     */
    public void setDetailsVisible(T item, boolean visible) {
        detailsManager.setDetailsVisible(item, visible);
    }

    /**
     * Sets whether the item details can be opened and closed by clicking the
     * rows or not.
     *
     * @param detailsVisibleOnClick
     *            {@code true} to enable opening and closing item details by
     *            clicking the rows, {@code false} to disable this functionality
     * @see #setItemDetailsRenderer(Renderer)
     */
    public void setDetailsVisibleOnClick(boolean detailsVisibleOnClick) {
        getElement().setProperty("__disallowDetailsOnClick",
                !detailsVisibleOnClick);
    }

    /**
     * Gets whether the item details are opened and closed by clicking the rows
     * or not.
     *
     * @return {@code true} if clicking the rows opens and closes their item
     *         details, {@code false} otherwise
     * @see #setItemDetailsRenderer(Renderer)
     */
    public boolean isDetailsVisibleOnClick() {
        return !getElement().getProperty("__disallowDetailsOnClick", false);
    }

    /**
     * Returns the visibility of details component for given item.
     *
     * @param item
     *            the item to show details for
     *
     * @return {@code true} if details component should be visible;
     *         {@code false} if it should be hidden
     */
    public boolean isDetailsVisible(T item) {
        return detailsManager.isDetailsVisible(item);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Registration addSortListener(
            ComponentEventListener<SortEvent<Grid<T>, GridSortOrder<T>>> listener) {
        return addListener(SortEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Sets whether multiple column sorting is enabled on the client-side.
     *
     * @param multiSort
     *            {@code true} to enable sorting of multiple columns on the
     *            client-side, {@code false} to disable
     */
    public void setMultiSort(boolean multiSort) {
        getElement().setAttribute("multi-sort", multiSort);
    }

    /**
     * Gets whether multiple column sorting is enabled on the client-side.
     *
     * @see #setMultiSort(boolean)
     *
     * @return {@code true} if sorting of multiple columns is enabled,
     *         {@code false} otherwise
     */
    public boolean isMultiSort() {
        String multiSort = getElement().getAttribute("multi-sort");
        if (multiSort != null && multiSort.length() == 0) {
            multiSort = "true";
        }
        return Boolean.parseBoolean(multiSort);
    }

    @ClientCallable
    private void updateContextMenuTargetItem(String key, String colId) {
        getElement().setProperty("_contextMenuTargetItemKey", key);
        getElement().setProperty("_contextMenuTargetColumnId", colId);
    }

    /**
     * Adds a new context-menu for this grid.
     *
     * @return the added context-menu
     */
    public GridContextMenu<T> addContextMenu() {
        return new GridContextMenu<T>(this);
    }

    private List<Column<T>> fetchChildColumns(ColumnGroup columnGroup) {
        List<Column<T>> ret = new ArrayList<>();
        columnGroup.getChildColumns()
                .forEach(column -> appendChildColumns(ret, column));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private void appendChildColumns(List<Column<T>> list,
            ColumnBase<?> column) {
        if (column instanceof Column) {
            list.add((Column<T>) column);
        } else if (column instanceof ColumnGroup) {
            list.addAll(fetchChildColumns((ColumnGroup) column));
        }
    }

    @ClientCallable
    private void select(String key) {
        findByKey(String.valueOf(key))
                .ifPresent(getSelectionModel()::selectFromClient);
    }

    @ClientCallable
    private void deselect(String key) {
        findByKey(String.valueOf(key))
                .ifPresent(getSelectionModel()::deselectFromClient);
    }

    private Optional<T> findByKey(String key) {
        Objects.requireNonNull(key);
        Optional<T> item = Optional
                .ofNullable(getDataCommunicator().getKeyMapper().get(key));
        if (!item.isPresent()) {
            LoggerFactory.getLogger(Grid.class).debug("Key not found: %s. "
                    + "This can happen due to user action while changing"
                    + " the data provider.", key);
        }
        return item;
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void confirmUpdate(int id) {
        getDataCommunicator().confirmUpdate(id);
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setRequestedRange(int start, int length) {
        getDataCommunicator().setRequestedRange(start, length);
    }

    @ClientCallable
    private void setDetailsVisible(String key) {
        if (key == null) {
            detailsManager.setDetailsVisibleFromClient(Collections.emptySet());
        } else {
            findByKey(key).map(Collections::singleton)
                    .ifPresent(detailsManager::setDetailsVisibleFromClient);
        }
    }

    @ClientCallable
    private void sortersChanged(JsonArray sorters) {
        GridSortOrderBuilder<T> sortOrderBuilder = new GridSortOrderBuilder<>();
        for (int i = 0; i < sorters.length(); ++i) {
            JsonObject sorter = sorters.getObject(i);
            Column<T> column = idToColumnMap.get(sorter.getString("path"));
            if (column == null) {
                throw new IllegalArgumentException(
                        "Received a sorters changed call from the client for a non-existent column");
            }
            if (sorter.hasKey("direction")
                    && sorter.get("direction").getType() == JsonType.STRING) {
                switch (sorter.getString("direction")) {
                case "asc":
                    sortOrderBuilder.thenAsc(column);
                    break;
                case "desc":
                    sortOrderBuilder.thenDesc(column);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Received a sorters changed call from the client containing an invalid sorting direction");
                }
            }
        }
        setSortOrder(sortOrderBuilder.build(), true);
    }

    /**
     * Forces a defined sort order for the columns in the Grid. Setting
     * <code>null</code> or an empty list resets the ordering of all columns.
     * Columns not mentioned in the list are reset to the unsorted state.
     * <p>
     * For Grids with multi-sorting, the index of a given column inside the list
     * defines the sort priority. For example, the column at index 0 of the list
     * is sorted first, then on the index 1, and so on.
     *
     * @param order
     *            the list of sort orders to set on the client, or
     *            <code>null</code> to reset any sort orders.
     * @see #setMultiSort(boolean)
     * @see #getSortOrder()
     */
    public void sort(List<GridSortOrder<T>> order) {
        if (order == null) {
            order = Collections.emptyList();
        }
        setSortOrder(order, false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        updateClientSideSorterIndicators(sortOrder);
        if (getDataProvider() != null) {
            handleDataProviderChange(getDataProvider());
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dataProviderChangeRegistration != null) {
            dataProviderChangeRegistration.remove();
            dataProviderChangeRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    /**
     * Sets the sort orders of the Grid and updates data communicator's
     * in-memory and backend sorting accordingly.
     * <p>
     * Notifies sort listeners with updated sort orders and whether the sorting
     * updated originated from user.
     *
     * @param order
     *            sort order to be set to Grid.
     * @param userOriginated
     *            <code>true</code> if the sorting changes as a result of user
     *            interaction, <code>false</code> if changed by Grid API call.
     */
    private void setSortOrder(List<GridSortOrder<T>> order,
            boolean userOriginated) {
        Objects.requireNonNull(order, "Sort order list cannot be null");

        if (sortOrder.equals(order)) {
            return;
        }

        if (!userOriginated) {
            updateClientSideSorterIndicators(order);
        }

        sortOrder.clear();
        if (order.isEmpty()) {
            // Grid's sorting is being reset by Grid's sort API or by clicking
            // on a Grid's sortable column header, but the sorting, which was
            // set through the GridListDataView API, is being preserved.
            getDataCommunicator().setBackEndSorting(Collections.emptyList());
            getDataCommunicator().setInMemorySorting(
                    (SerializableComparator<T>) DataViewUtils
                            .getComponentSortComparator(this).orElse(null));
            fireEvent(new SortEvent<>(this, new ArrayList<>(sortOrder),
                    userOriginated));
            return;
        }
        sortOrder.addAll(order);
        updateSorting(userOriginated);
    }

    /**
     * Gets an list of the current sort orders in the Grid.
     *
     * @return an unmodifiable list of sort orders
     */
    public List<GridSortOrder<T>> getSortOrder() {
        return Collections.unmodifiableList(sortOrder);
    }

    private void updateClientSideSorterIndicators() {
        updateClientSideSorterIndicators(sortOrder);
    }

    private void updateClientSideSorterIndicators(
            List<GridSortOrder<T>> order) {
        JsonArray directions = Json.createArray();

        for (int i = 0; i < order.size(); i++) {
            GridSortOrder<T> gridSortOrder = order.get(i);
            JsonObject direction = Json.createObject();

            String columnId = gridSortOrder.getSorted().getInternalId();
            direction.put("column", columnId);

            if (gridSortOrder.getDirection() != null) {
                switch (gridSortOrder.getDirection()) {
                case ASCENDING:
                    direction.put("direction", "asc");
                    break;
                case DESCENDING:
                    direction.put("direction", "desc");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown gridSortOrder: "
                            + gridSortOrder.getDirection());
                }
            }
            directions.set(i, direction);
        }
        getElement().callJsFunction("$connector.setSorterDirections",
                directions);
    }

    /**
     * Updates an in-memory and backend sortings in Grid's data communicator
     * taking into account Grid's sort orders and in-memory comparator.
     * <p>
     * Notifies sort listeners with updated sort orders and whether the sorting
     * updated originated from user.
     *
     * @param userOriginated
     *            <code>true</code> if the sorting changes as a result of user
     *            interaction, <code>false</code> if changed by Grid API call.
     */
    private void updateSorting(boolean userOriginated) {
        // Set sort orders
        // In-memory comparator
        updateInMemorySorting((SerializableComparator<T>) DataViewUtils
                .getComponentSortComparator(this).orElse(null));

        // Back-end sort properties
        List<QuerySortOrder> sortProperties = new ArrayList<>();
        sortOrder.stream().map(
                order -> order.getSorted().getSortOrder(order.getDirection()))
                .forEach(s -> s.forEach(sortProperties::add));
        getDataCommunicator().setBackEndSorting(sortProperties);

        fireEvent(new SortEvent<>(this, new ArrayList<>(sortOrder),
                userOriginated));
    }

    /**
     * Creates a comparator for grid to sort rows.
     *
     * @return the comparator based on column sorting information.
     */
    protected SerializableComparator<T> createSortingComparator() {
        BinaryOperator<SerializableComparator<T>> operator = (comparator1,
                comparator2) -> {
            /*
             * thenComparing is defined to return a serializable comparator as
             * long as both original comparators are also serializable
             */
            return comparator1.thenComparing(comparator2)::compare;
        };
        return sortOrder.stream().map(
                order -> order.getSorted().getComparator(order.getDirection()))
                .reduce(operator).orElse(null);
    }

    /**
     * If <code>true</code>, the grid's height is defined by its rows. All items
     * are fetched from the {@link DataProvider}, and the Grid shows no vertical
     * scroll bar.
     * <p>
     * Note: <code>setHeightByRows</code> disables the grid's virtual scrolling
     * so that all the rows are rendered in the DOM at once. If the grid has a
     * large number of items, using the feature is discouraged to avoid
     * performance issues.
     *
     * @deprecated since 14.7 - use {@link #setAllRowsVisible(boolean)}
     * @see #setAllRowsVisible(boolean)
     *
     * @param heightByRows
     *            <code>true</code> to make Grid compute its height by the
     *            number of rows, <code>false</code> for the default behavior
     */
    @Deprecated
    public void setHeightByRows(boolean heightByRows) {
        setAllRowsVisible(heightByRows);
    }

    /**
     * Gets whether grid's height is defined by the number of its rows.
     *
     * @deprecated since 14.7 - use {@link #isAllRowsVisible()}
     * @see #isAllRowsVisible()
     *
     * @return <code>true</code> if Grid computes its height by the number of
     *         rows, <code>false</code> otherwise
     */
    @Deprecated
    public boolean isHeightByRows() {
        return isAllRowsVisible();
    }

    /**
     * If <code>true</code>, the grid's height is defined by its rows. All items
     * are fetched from the {@link DataProvider}, and the Grid shows no vertical
     * scroll bar.
     * <p>
     * Note: <code>setAllRowsVisible</code> disables the grid's virtual
     * scrolling so that all the rows are rendered in the DOM at once. If the
     * grid has a large number of items, using the feature is discouraged to
     * avoid performance issues.
     *
     * @param allRowsVisible
     *            <code>true</code> to make Grid compute its height by the
     *            number of rows, <code>false</code> for the default behavior
     */
    public void setAllRowsVisible(boolean allRowsVisible) {
        getElement().setProperty("allRowsVisible", allRowsVisible);
    }

    /**
     * Gets whether grid's height is defined by the number of its rows.
     *
     * @return <code>true</code> if Grid computes its height by the number of
     *         rows, <code>false</code> otherwise
     */
    @Synchronize("all-rows-visible-changed")
    public boolean isAllRowsVisible() {
        return getElement().getProperty("allRowsVisible", false);
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        super.onEnabledStateChanged(enabled);

        /*
         * The DataCommunicator needs to be reset so components rendered inside
         * the cells can be updated to the new enabled state. The enabled state
         * is passed as a property to the client via DataGenerators.
         */
        getDataCommunicator().reset();
    }

    /**
     * Adds a ValueProvider to this Grid that is not tied to a Column. This is
     * specially useful when the columns are defined via a template file instead
     * of the Java API.
     * <p>
     * The properties added to by this method are global to the Grid - they can
     * be used in any column.
     * <p>
     * ValueProviders are registered as {@link DataGenerator}s in the Grid. See
     * {@link #addDataGenerator(DataGenerator)}.
     *
     * @param property
     *            the property name used in the template. For example, in a
     *            template the uses {@code [[item.name]]}, the property is
     *            {@code name}. Not <code>null</code>
     * @param valueProvider
     *            the provider for values for the property, not
     *            <code>null</code>
     * @return a registration that can be used to remove the ValueProvider from
     *         the Grid
     */
    public Registration addValueProvider(String property,
            ValueProvider<T, ?> valueProvider) {
        Objects.requireNonNull(property);
        Objects.requireNonNull(valueProvider);

        return addDataGenerator(
                (item, data) -> data.put(property, JsonSerializer
                        .toJson(applyValueProvider(valueProvider, item))));
    }

    @Override
    public Registration addDataGenerator(DataGenerator<T> dataGenerator) {
        return gridDataGenerator.addDataGenerator(dataGenerator);
    }

    protected static int compareMaybeComparables(Object a, Object b) {
        if (hasCommonComparableBaseType(a, b)) {
            return compareComparables(a, b);
        }
        return compareComparables(Objects.toString(a, ""),
                Objects.toString(b, ""));
    }

    /**
     * Sets the bean type this grid is bound to and optionally adds a set of
     * columns for each of the bean's properties.
     *
     * The property-values of the bean will be converted to Strings. Full names
     * of the properties will be used as the {@link Column#setKey(String) column
     * keys} and the property captions will be used as the
     * {@link Column#setHeader(String) column headers}. The generated columns
     * will be sortable by default, if the property is {@link Comparable}.
     * <p>
     * When autoCreateColumns is <code>true</code>, only the direct properties
     * of the bean are included and they will be in alphabetical order. Use
     * {@link Grid#setColumns(String...)} to define which properties to include
     * and in which order. You can also add a column for an individual property
     * with {@link #addColumn(String)}. Both of these methods support also
     * sub-properties with dot-notation, eg.
     * <code>"property.nestedProperty"</code>.
     * <p>
     * This method can only be called for a newly instanced Grid without any
     * beanType or columns set.
     *
     * @param beanType
     *            the bean type to use, not <code>null</code>
     * @param autoCreateColumns
     *            when <code>true</code>, columns are created automatically for
     *            the properties of the beanType
     */
    public void configureBeanType(Class<T> beanType,
            boolean autoCreateColumns) {
        Objects.requireNonNull(beanType, "Bean type can't be null");

        if (this.beanType != null) {
            throw new IllegalStateException(
                    "configureBeanType can only be called for a Grid without a beanType set");
        }
        if (!this.getColumns().isEmpty()) {
            throw new IllegalStateException(
                    "configureBeanType can only be called for a Grid without any columns");
        }
        this.beanType = beanType;
        propertySet = BeanPropertySet.get(beanType);
        if (autoCreateColumns) {
            propertySet.getProperties()
                    .filter(property -> !property.isSubProperty())
                    .sorted((prop1, prop2) -> prop1.getName()
                            .compareTo(prop2.getName()))
                    .forEach(this::addColumn);
        }

    }

    /**
     * Returns the Class of bean this Grid is constructed with via
     * {@link #Grid(Class)}. Or null if not constructed from a bean type.
     *
     * @return the Class of bean this Grid is constructed with
     */
    public Class<T> getBeanType() {
        return beanType;
    }

    /**
     * Returns {@link PropertySet} of bean this Grid is constructed with via
     * {@link #Grid(Class)}. Or null if not constructed from a bean type.
     *
     * @return the {@link PropertySet} of bean this Grid is constructed with
     */
    public PropertySet<T> getPropertySet() {
        return propertySet;
    }

    /**
     * Adds an item click listener to this component.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     *
     * @see #addItemDoubleClickListener(ComponentEventListener)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addItemClickListener(
            ComponentEventListener<ItemClickEvent<T>> listener) {
        return addListener(ItemClickEvent.class,
                (ComponentEventListener) Objects.requireNonNull(listener));
    }

    /**
     * Adds a column resize listener to this component. Note that the listener
     * will be notified only for user-initiated column resize actions.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addColumnResizeListener(
            ComponentEventListener<ColumnResizeEvent<T>> listener) {
        return addListener(ColumnResizeEvent.class,
                (ComponentEventListener) Objects.requireNonNull(listener));
    }

    /**
     * Adds an item double click listener to this component.
     * <p>
     * Note that double click event happens along with a click event. It means
     * there is no way to get a double click event only (double click without a
     * click): a click listener added using
     * {@link #addItemClickListener(ComponentEventListener)} (if any) will also
     * be notified about a click event once a double click event is fired.
     * <p>
     * Double click event type is not fully supported by the mobile browsers
     * which means that double click event might not work (double click
     * listeners won't be notified) for such browsers.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     *
     * @see #addItemClickListener(ComponentEventListener)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addItemDoubleClickListener(
            ComponentEventListener<ItemDoubleClickEvent<T>> listener) {
        return addListener(ItemDoubleClickEvent.class,
                (ComponentEventListener) Objects.requireNonNull(listener));
    }

    /**
     * Adds a listener to the grid that will be notified, when a cell has been
     * focused. <br>
     * <br>
     * The listener will be notified, when
     * <ul>
     * <li>the navigation focus of a cell gets activated</li>
     * <li>the focus is restored to the browser if a cell had navigation focus
     * before the focus was lost</li>
     * <li>the navigation focus moves between header/body/footer sections</li>
     * </ul>
     * <br>
     * The listener will <b>not</b> be notified, when
     * <ul>
     * <li>the focus changes between focusable elements in the Grid cells
     * ("interaction mode")</li>
     * <li>on Grid Pro edit mode navigation ("interaction mode")</li>
     * <li>the focus changes between focusable elements in the cells in Flow
     * Grid's editor mode ("interaction mode")</li>
     * </ul>
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addCellFocusListener(
            ComponentEventListener<CellFocusEvent<T>> listener) {
        return addListener(CellFocusEvent.class,
                (ComponentEventListener) Objects.requireNonNull(listener));
    }

    /**
     * Enables or disables the vertical scrolling on the Grid web component. By
     * default, the scrolling is enabled.
     *
     * @param enabled
     *            <code>true</code> to enable vertical scrolling,
     *            <code>false</code> to disabled it
     */
    public void setVerticalScrollingEnabled(boolean enabled) {
        if (isVerticalScrollingEnabled() == enabled) {
            return;
        }
        verticalScrollingEnabled = enabled;
        getElement().callJsFunction("$connector.setVerticalScrollingEnabled",
                enabled);
    }

    /**
     * Gets whether the vertical scrolling on the Grid web component is enabled.
     *
     * @return <code>true</code> if the vertical scrolling is enabled,
     *         <code>false</code> otherwise
     */
    public boolean isVerticalScrollingEnabled() {
        return verticalScrollingEnabled;
    }

    /**
     * Gets the editor.
     * <p>
     * The editor is created using {@link #createEditor()}.
     *
     * @see #createEditor()
     *
     * @return the editor instance
     */
    public Editor<T> getEditor() {
        if (editor == null) {
            editor = editorFactory.get();
        }
        return editor;
    }

    /**
     * Sets the function that is used for generating CSS class names for all the
     * cells in the rows in this grid. Returning {@code null} from the generator
     * results in no custom class name being set. Multiple class names can be
     * returned from the generator as space-separated.
     * <p>
     * If {@link Column#setClassNameGenerator(SerializableFunction)} is used
     * together with this method, resulting class names from both methods will
     * be effective. Class names generated by grid are applied to the cells
     * before the class names generated by column. This means that if the
     * classes contain conflicting style properties, column's classes will win.
     *
     * @param classNameGenerator
     *            the class name generator to set, not {@code null}
     * @throws NullPointerException
     *             if {@code classNameGenerator} is {@code null}
     * @see Column#setClassNameGenerator(SerializableFunction)
     */
    public void setClassNameGenerator(
            SerializableFunction<T, String> classNameGenerator) {
        Objects.requireNonNull(classNameGenerator,
                "Class name generator can not be null");
        this.classNameGenerator = classNameGenerator;
        getDataCommunicator().reset();
    }

    /**
     * Updates the {@code width} of all columns which have {@code autoWidth} set
     * to {@code true}.
     *
     * @see Column#setAutoWidth(boolean)
     */
    public void recalculateColumnWidths() {
        // Defer column width recalculation to occur after the data was
        // refreshed. The data communicator will insert the JS call to refresh
        // the client side grid in the beforeClientResponse hook, we need to
        // match this here so that the column width recalculation runs after the
        // data was updated.
        getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(
                this,
                ctx -> getElement().callJsFunction("recalculateColumnWidths")));
    }

    /**
     * Gets the function that is used for generating CSS class names for rows in
     * this grid.
     *
     * @return the class name generator
     */
    public SerializableFunction<T, String> getClassNameGenerator() {
        return classNameGenerator;
    }

    private void generateStyleData(T item, JsonObject jsonObject) {
        JsonObject style = Json.createObject();

        String rowClassName = classNameGenerator.apply(item);
        if (rowClassName != null) {
            style.put("row", rowClassName);
        }

        idToColumnMap.forEach((id, column) -> {
            String cellClassName = column.getClassNameGenerator().apply(item);
            if (cellClassName != null) {
                style.put(id, cellClassName);
            }
        });

        if (style.keys().length > 0) {
            jsonObject.put("style", style);
        }
    }

    private void generateRowsDragAndDropAccess(T item, JsonObject jsonObject) {
        if (getDropMode() != null && !dropFilter.test(item)) {
            jsonObject.put("dropDisabled", true);
        }

        if (this.isRowsDraggable() && !dragFilter.test(item)) {
            jsonObject.put("dragDisabled", true);
        }
    }

    private void generateDragData(T item, JsonObject jsonObject) {
        JsonObject dragData = Json.createObject();

        this.dragDataGenerators.entrySet().forEach(entry -> dragData
                .put(entry.getKey(), entry.getValue().apply(item)));

        if (dragData.keys().length > 0) {
            jsonObject.put("dragData", dragData);
        }
    }

    /**
     * Creates a new Editor instance. Can be overridden to create a custom
     * Editor. If the Editor is a {@link AbstractGridExtension}, it will be
     * automatically added to {@link DataCommunicator}.
     *
     * @return editor
     */
    protected Editor<T> createEditor() {
        return new EditorImpl<>(this, propertySet);
    }

    /**
     * Gets optional value provider for unique key in row's generated JSON.
     *
     * @return ValueProvider for unique key for row or null if not set
     */
    protected ValueProvider<T, String> getUniqueKeyProvider() {
        return uniqueKeyProvider;
    }

    /**
     * Sets value provider for unique key in row's generated JSON.
     * <p>
     * <code>null</code> by default.
     *
     * @param uniqueKeyProvider
     *            ValueProvider for unique key for row
     */
    protected void setUniqueKeyProvider(
            ValueProvider<T, String> uniqueKeyProvider) {
        this.uniqueKeyProvider = uniqueKeyProvider;
    }

    /**
     * Gets property name for unique key in row's generated JSON.
     *
     * @return the optional property name for unique key
     */
    protected String getUniqueKeyProperty() {
        return uniqueKeyProperty;
    }

    /**
     * Sets property name for unique key in row's generated JSON.
     *
     * @param uniqueKeyProperty
     *            the new optional property name for unique key
     */
    protected void setUniqueKeyProperty(String uniqueKeyProperty) {
        this.uniqueKeyProperty = uniqueKeyProperty;
        arrayUpdater.getUpdateQueueData()
                .setUniqueKeyProperty(uniqueKeyProperty);
    }

    protected GridArrayUpdater getArrayUpdater() {
        return arrayUpdater;
    }

    /**
     * Callback which is called if a new data provider is set or any change
     * happen in the current data provider (an {@link DataChangeEvent} event is
     * fired).
     *
     * Default implementation closes the editor if it's opened.
     *
     * @see #setDataProvider(DataProvider)
     * @see DataChangeEvent
     * @see DataProviderListener
     *
     */
    protected void onDataProviderChange() {
        SerializableSupplier<Editor<T>> factory = editorFactory;
        editorFactory = () -> null;
        try {
            Editor<T> editor = getEditor();
            if (editor != null) {
                if (getEditor().isBuffered()) {
                    getEditor().cancel();
                } else {
                    getEditor().closeEditor();
                }
            }
        } finally {
            editorFactory = factory;
        }
    }

    private static boolean hasCommonComparableBaseType(Object a, Object b) {
        if (a instanceof Comparable<?> && b instanceof Comparable<?>) {
            Class<?> aClass = a.getClass();
            Class<?> bClass = b.getClass();

            if (aClass == bClass) {
                return true;
            }

            Class<?> baseType = ReflectTools.findCommonBaseType(aClass, bClass);
            if (Comparable.class.isAssignableFrom(baseType)) {
                return true;
            }
        }
        if ((a == null && b instanceof Comparable<?>)
                || (b == null && a instanceof Comparable<?>)) {
            return true;
        }

        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static int compareComparables(Object a, Object b) {
        return ((Comparator) Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(a, b);
    }

    private void handleDataProviderChange(DataProvider<T, ?> dataProvider) {
        onDataProviderChange();

        if (dataProviderChangeRegistration != null) {
            dataProviderChangeRegistration.remove();
        }

        dataProviderChangeRegistration = dataProvider
                .addDataProviderListener(event -> onDataProviderChange());
    }

    /**
     * Adds a drop listener to this component.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addDropListener(
            ComponentEventListener<GridDropEvent<T>> listener) {
        return addListener(GridDropEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Adds a drag start listener to this component.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addDragStartListener(
            ComponentEventListener<GridDragStartEvent<T>> listener) {
        return addListener(GridDragStartEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Adds a drag end listener to this component.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addDragEndListener(
            ComponentEventListener<GridDragEndEvent<T>> listener) {
        return addListener(GridDragEndEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Sets the drop mode of this drop target. When set to not {@code null},
     * grid fires drop events upon data drop over the grid or the grid rows.
     * <p>
     * When using {@link GridDropMode#ON_TOP}, and the grid is either empty or
     * has empty space after the last row, the drop can still happen on the
     * empty space, and the {@link GridDropEvent#getDropTargetItem()} will
     * return an empty optional.
     * <p>
     * When using {@link GridDropMode#BETWEEN} or
     * {@link GridDropMode#ON_TOP_OR_BETWEEN}, and there is at least one row in
     * the grid, any drop after the last row in the grid will get the last row
     * as the {@link GridDropEvent#getDropTargetItem()}. If there are no rows in
     * the grid, then it will return an empty optional.
     * <p>
     * If using {@link GridDropMode#ON_GRID}, then the drop will not happen on
     * any row, but instead just "on the grid". The target row will not be
     * present in this case.
     * <p>
     * <em>NOTE: Prefer not using a row specific {@link GridDropMode} with a
     * grid that enables sorting. If for example a new row gets added to a
     * specific location on drop event, it might not end up in the location of
     * the drop but rather where the active sorting configuration prefers to
     * place it. This behavior might feel unexpected for the users.
     *
     * @param dropMode
     *            Drop mode that describes the allowed drop locations within the
     *            Grid's row. Can be {@code null} to disable dropping on the
     *            grid.
     * @see GridDropEvent#getDropLocation()
     */
    public void setDropMode(GridDropMode dropMode) {
        // We need to add DnD mobile polyfill here by invoking
        // DndUtil.addMobileDndPolyfillIfNeeded. But, since DndUtil is in a Flow
        // internal package, DropTarget.create is called to invoke
        // addMobileDndPolyfillIfNeeded indirectly.
        DropTarget.create(this).setActive(false);
        getElement().setProperty("dropMode",
                dropMode == null ? null : dropMode.getClientName());
    }

    /**
     * Gets the drop mode of this drop target.
     *
     * @return Drop mode that describes the allowed drop locations within the
     *         Grid's row. {@code null} if dropping is not enabled.
     */
    public GridDropMode getDropMode() {
        String dropMode = getElement().getProperty("dropMode");
        Optional<GridDropMode> mode = Arrays.stream(GridDropMode.values())
                .filter(dm -> dm.getClientName().equals(dropMode)).findFirst();
        return mode.orElse(null);
    }

    /**
     * Sets whether the user can drag the grid rows or not.
     *
     * @param rowsDraggable
     *            {@code true} if the rows can be dragged by the user;
     *            {@code false} if not
     */
    public void setRowsDraggable(boolean rowsDraggable) {
        // We need to add DnD mobile polyfill here by invoking
        // DndUtil.addMobileDndPolyfillIfNeeded. But, since DndUtil is in a Flow
        // internal package, DragSource.create is called to invoke
        // addMobileDndPolyfillIfNeeded indirectly.
        DragSource.create(this).setDraggable(false);
        getElement().setProperty("rowsDraggable", rowsDraggable);
    }

    /**
     * Gets whether rows of the grid can be dragged.
     *
     * @return {@code true} if the rows are draggable, {@code false} otherwise
     */
    public boolean isRowsDraggable() {
        return getElement().getProperty("rowsDraggable", false);
    }

    /**
     * Gets the active drop filter.
     *
     * @return The drop filter function
     */
    public SerializablePredicate<T> getDropFilter() {
        return dropFilter;
    }

    /**
     * Gets the active drag filter.
     *
     * @return The drag filter function
     */
    public SerializablePredicate<T> getDragFilter() {
        return dragFilter;
    }

    /**
     * Sets the drop filter for this drag target.
     * <p>
     * When the drop mode of the grid has been set to one of
     * {@link GridDropMode#BETWEEN}, {@link GridDropMode#ON_TOP} or
     * {@link GridDropMode#ON_TOP_OR_BETWEEN}, by default all the visible rows
     * can be dropped over.
     * <p>
     * A drop filter function can be used to specify the rows that are available
     * for dropping over. The function receives an item and should return
     * {@code true} if the row can be dropped over, {@code false} otherwise.
     * <p>
     * <em>NOTE: If the filter conditions depend on a specific row that's
     * currently being dragged, you might want to have the grid's drop mode
     * disabled by default and set its value only on drag start to avoid the
     * small period of time during which the user might be able to drop over
     * unwanted rows. Once the drop end event occurs, the drop mode can be set
     * back to {@code null} to keep this consistent.
     * <p>
     * <em>NOTE: If the filtering conditions change dynamically, remember to
     * explicitly invoke {@code getDataProvider().refreshItem(item)} for the
     * relevant items to get the filters re-run for them.
     */
    public void setDropFilter(SerializablePredicate<T> dropFilter) {
        Objects.requireNonNull(dropFilter, "Drop filter can not be null");
        this.dropFilter = dropFilter;
        getDataCommunicator().reset();
    }

    /**
     * Sets the drag filter for this drag source.
     * <p>
     * When the {@link #setRowsDraggable(boolean)} has been used to enable
     * dragging, by default all the visible rows can be dragged.
     * <p>
     * A drag filter function can be used to specify the rows that are available
     * for dragging. The function receives an item and returns {@code true} if
     * the row can be dragged, {@code false} otherwise.
     * <p>
     * <em>NOTE: If the filtering conditions change dynamically, remember to
     * explicitly invoke {@code getDataProvider().refreshItem(item)} for the
     * relevant items to get the filters re-run for them.
     */
    public void setDragFilter(SerializablePredicate<T> dragFilter) {
        Objects.requireNonNull(dragFilter, "Drag filter can not be null");
        this.dragFilter = dragFilter;
        getDataCommunicator().reset();
    }

    /**
     * Sets a generator function for customizing drag data. The generated value
     * will be accessible using the same {@code type} as the generator is set
     * here. The function is executed for each item in the Grid during data
     * generation. Return a {@link String} to be appended to the row as {@code
     * type} data.
     * <p>
     * Note that IE11 only supports data type "text"
     *
     * @param type
     *            Type of the generated data. The generated value will be
     *            accessible during drop using this type.
     * @param dragDataGenerator
     *            Function to be executed on row data generation.
     */
    public void setDragDataGenerator(String type,
            SerializableFunction<T, String> dragDataGenerator) {
        this.dragDataGenerators.put(type, dragDataGenerator);

        JsonArray types = Json.createArray();

        this.dragDataGenerators.keySet()
                .forEach(t -> types.set(types.length(), t));
        this.getElement().setPropertyJson("__dragDataTypes", types);
        getDataCommunicator().reset();
    }

    /**
     * Sets explicit drag operation details for when the user is dragging the
     * selected items. By default, the drag data only covers the items in the
     * visible viewport and all the items outside of it, even if selected, are
     * excluded. Use this method to override the default drag data and the
     * number shown in drag image on selection drag.
     * <p>
     * Note that IE11 only supports data type "text"
     *
     * @param draggedItemsCount
     *            The number shown in the drag image on selection drag. Only
     *            values above 1 have any visible effect.
     * @param dragData
     *            The drag data for selection drag. The map should consist of
     *            data type:data -entries
     */
    public void setSelectionDragDetails(int draggedItemsCount,
            Map<String, String> dragData) {
        this.getElement().setProperty("__selectionDraggedItemsCount",
                draggedItemsCount);

        if (dragData != null) {
            JsonObject json = Json.createObject();
            dragData.entrySet()
                    .forEach(e -> json.put(e.getKey(), e.getValue()));
            this.getElement().setPropertyJson("__selectionDragData", json);
        } else {
            this.getElement().setProperty("__selectionDragData", null);
        }

    }

    /**
     * Adds a column reorder listener to this component.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addColumnReorderListener(
            ComponentEventListener<ColumnReorderEvent<T>> listener) {
        return addListener(ColumnReorderEvent.class,
                (ComponentEventListener) Objects.requireNonNull(listener));
    }

    /**
     * Sets a new column order for the grid.
     * <p>
     * The function doesn't support column removal: all columns must be present
     * in the list, otherwise {@link IllegalArgumentException} is thrown.
     * <p>
     * The {@link #getColumns()} function will reflect the new column ordering.
     * <p>
     * Fires the {@link ColumnReorderEvent} with
     * {@link ColumnReorderEvent#isFromClient()} returning {@code false}.
     * <p>
     * The method is atomic: if the requested reordering is not achievable, the
     * function fails cleanly with {@link IllegalArgumentException} without
     * doing any work.
     *
     * @see #setColumnOrder(List)
     * @param columns
     *            the new ordering of the columns, not {@code null}.
     * @throws NullPointerException
     *             if the {@code columns} parameter is {@code null}.
     * @throws IllegalArgumentException
     *             if a column is present two times in the list, or if the
     *             column is not owned by this Grid, or if the list doesn't
     *             contain all columns currently present in the Grid, or if the
     *             column rearranging would require to split a joined
     *             header/footer cell group.
     */
    public void setColumnOrder(Column<T>... columns) {
        setColumnOrder(Arrays.asList(columns));
    }

    /**
     * Sets a new column order for the grid.
     * <p>
     * The function doesn't support column removal: all columns must be present
     * in the list, otherwise {@link IllegalArgumentException} is thrown.
     * <p>
     * The {@link #getColumns()} function will reflect the new column ordering.
     * <p>
     * Fires the {@link ColumnReorderEvent} with
     * {@link ColumnReorderEvent#isFromClient()} returning {@code false}.
     * <p>
     * The method is atomic: if the requested reordering is not achievable, the
     * function fails cleanly with {@link IllegalArgumentException} without
     * doing any work.
     *
     * @see #setColumnOrder(Column[])
     * @param columns
     *            the new ordering of the columns, not {@code null}.
     * @throws NullPointerException
     *             if the {@code columns} parameter is {@code null}.
     * @throws IllegalArgumentException
     *             if a column is present two times in the list, or if the
     *             column is not owned by this Grid, or if the list doesn't
     *             contain all columns currently present in the Grid, or if the
     *             column rearranging would require to split a joined
     *             header/footer cell group.
     */
    public void setColumnOrder(List<Column<T>> columns) {
        new GridColumnOrderHelper<>(this).setColumnOrder(columns);
        updateClientSideSorterIndicators(sortOrder);
        fireColumnReorderEvent(getColumns());
    }

    private void fireColumnReorderEvent(List<Column<T>> columns) {
        fireEvent(new ColumnReorderEvent<>(this, false, columns));
    }

    /**
     * Scrolls to the given row index. Scrolls so that the row is shown at the
     * start of the visible area whenever possible.
     *
     * If the index parameter exceeds current item set size the grid will scroll
     * to the end.
     *
     * @param rowIndex
     *            zero based index of the item to scroll to in the current view.
     */
    public void scrollToIndex(int rowIndex) {
        getElement().callJsFunction("scrollToIndex", rowIndex);
    }

    /**
     * Scrolls to the beginning of the first data row.
     */
    public void scrollToStart() {
        scrollToIndex(0);
    }

    /**
     * Scrolls to the last data row of the grid.
     */
    public void scrollToEnd() {
        getUI().ifPresent(
                ui -> ui.beforeClientResponse(this, ctx -> getElement()
                        .executeJs("this.scrollToIndex(this._effectiveSize)")));
    }

    private void onDragStart(GridDragStartEvent<T> event) {
        ComponentUtil.setData(this, DRAG_SOURCE_DATA_KEY,
                event.getDraggedItems());
        getUI().ifPresent(
                ui -> ui.getInternals().setActiveDragSourceComponent(this));
    }

    private void onDragEnd(GridDragEndEvent<T> event) {
        ComponentUtil.setData(this, DRAG_SOURCE_DATA_KEY, null);
        getUI().ifPresent(
                ui -> ui.getInternals().setActiveDragSourceComponent(null));
    }

    /**
     * Set the behavior when facing nested <code>null</code> values. By default
     * the value is <code>NestedNullBehavior.THROW</code>.
     *
     * @param nestedNullBehavior
     *            the behavior when facing nested <code>null</code> values.
     */
    public void setNestedNullBehavior(NestedNullBehavior nestedNullBehavior) {
        this.nestedNullBehavior = nestedNullBehavior;
    }

    /**
     * Get the behavior when facing nested <code>null</code> values.
     *
     * @return The current behavior when facing nested <code>null</code> values.
     */
    public NestedNullBehavior getNestedNullBehavior() {
        return nestedNullBehavior;
    }

    private void onInMemoryFilterOrSortingChange(
            SerializablePredicate<T> filter,
            SerializableComparator<T> sortComparator) {
        updateInMemorySorting(sortComparator);
        updateInMemoryFiltering(filter);

        dataCommunicator.reset();
    }

    private void updateInMemoryFiltering(
            SerializablePredicate<T> componentInMemoryFilter) {
        assert filterSlot != null
                : "Filter Slot is supposed not to be empty when set the filter";
        // As long as the Grid currently contains only in-memory filter
        // and only list data view has a filter setup API, we can safely cast
        // the filter slot type into in-memory filter (predicate).
        @SuppressWarnings("unchecked")
        SerializableConsumer<SerializablePredicate<T>> inMemoryFilter = (SerializableConsumer<SerializablePredicate<T>>) filterSlot;
        inMemoryFilter.accept(componentInMemoryFilter);
    }

    /**
     * Updates an in-memory sorting in Grid's data communicator, taking into
     * account an internal sort orders of the Grid and a sort comparator,
     * handled by GridListDataView API.
     *
     * @param componentSorting
     *            Grid's in-memory sort comparator which is handled by
     *            GridListDataView API
     */
    private void updateInMemorySorting(
            SerializableComparator<T> componentSorting) {
        final SerializableComparator<T> currentClientSorting = createSortingComparator();
        if (componentSorting != null) {
            if (currentClientSorting != null) {
                getDataCommunicator().setInMemorySorting(combineSortings(
                        currentClientSorting, componentSorting));
            } else {
                getDataCommunicator().setInMemorySorting(componentSorting);
            }
        } else {
            getDataCommunicator().setInMemorySorting(currentClientSorting);
        }
    }

    private SerializableComparator<T> combineSortings(
            SerializableComparator<T> originalSorting,
            SerializableComparator<T> addedSorting) {
        Objects.requireNonNull(originalSorting);
        Objects.requireNonNull(addedSorting);
        return (c1, c2) -> {
            int result = originalSorting.compare(c1, c2);
            if (result == 0) {
                result = addedSorting.compare(c1, c2);
            }
            return result;
        };
    }
}

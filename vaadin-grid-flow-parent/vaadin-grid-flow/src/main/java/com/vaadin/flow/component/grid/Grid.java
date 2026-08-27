/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorRenderer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ItemIndexProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.selection.SelectionModel.Single;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

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
 * @since 1.0
 */
public class Grid<T> extends GridBase<Grid<T>, T>
        implements HasListDataView<T, GridListDataView<T>>,
        HasDataView<T, Void, GridDataView<T>>,
        HasLazyDataView<T, Void, GridLazyDataView<T>> {

    /**
     * behavior when parsing nested properties which may contain
     * <code>null</code> values in the property chain
     *
     * @since 14.5
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

    /**
     * @since 1.1
     * @deprecated To customize array updates, override
     *             {@link #createDefaultArrayUpdater()} and return a
     *             {@link GridArrayUpdater} whose
     *             {@link GridArrayUpdater#startUpdate(int) startUpdate} method
     *             returns a custom {@link Update} implementation. This class
     *             will be made private in Vaadin 26.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    protected static class UpdateQueue implements Update {
        private final ArrayList<SerializableRunnable> queue = new ArrayList<>();
        private final Element element;

        protected UpdateQueue(Element element, int size) {
            this.element = element;

            // 'size' property is not synchronized by the web component since
            // there are no events for it, but we
            // need to sync it otherwise server will overwrite client value with
            // the old server one
            enqueue("$connector.updateSize", size);
            getElement().setProperty("size", size);
        }

        @Override
        public void set(int start, List<JsonNode> items) {
            enqueue("$connector.set", start,
                    items.stream().collect(JacksonUtils.asArray()));
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
            return element;
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
        SINGLE,

        /**
         * Multiselection mode that maps to built-in
         * {@link SelectionModel.Multi}.
         *
         * @see GridMultiSelectionModel
         */
        MULTI,

        /**
         * Selection model that doesn't allow selection.
         *
         * @see GridNoneSelectionModel
         */
        NONE;

        /**
         * Creates the selection model to use with this enum.
         *
         * @param <T>
         *            the type of items in the grid
         * @param grid
         *            the grid to create the selection model for
         * @return the selection model
         */
        protected <T> GridSelectionModel<T> createModel(GridBase<?, T> grid) {
            return switch (this) {
            case SINGLE -> new AbstractGridSingleSelectionModel<T>(grid) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                protected void fireSelectionEvent(
                        SelectionEvent<GridBase<?, T>, T> event) {
                    ComponentUtil.fireEvent(grid, (ComponentEvent) event);
                }

                @Override
                public void setDeselectAllowed(boolean deselectAllowed) {
                    super.setDeselectAllowed(deselectAllowed);
                    grid.getElement().setProperty("__deselectDisallowed",
                            !deselectAllowed);
                }
            };
            case MULTI -> new AbstractGridMultiSelectionModel<T>(grid) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                protected void fireSelectionEvent(
                        SelectionEvent<GridBase<?, T>, T> event) {
                    ComponentUtil.fireEvent(grid, (ComponentEvent) event);
                }
            };
            case NONE -> new GridNoneSelectionModel<>();
            };
        }
    }

    /**
     * Multi-sort priority (visually indicated by numbers in column headers)
     * controls how columns are added to the sort order, when a column becomes
     * sorted, or the sort direction of a column is changed.
     * <p>
     * Use {@link Grid#setMultiSort(boolean, MultiSortPriority)} to customize
     * the multi-sort priority of an individual grid.
     *
     * @see Grid#setSelectionMode(SelectionMode)
     * @see Grid#setMultiSort(boolean, MultiSortPriority)
     * @since 23.2
     */
    public enum MultiSortPriority {
        /**
         * Whenever an unsorted column is sorted, it gets added at the end of
         * the sort order, after all the previously sorted columns. When the
         * sort direction of a column is changed by the user, the priority for
         * all the sorted columns remains unchanged.
         */
        APPEND,

        /**
         * Whenever an unsorted column is sorted, or the sort direction of a
         * column is changed, that column gets sort priority 1, and all the
         * other sorted columns are updated accordingly. This is the default
         * behavior of the component.
         */
        PREPEND
    }

    /**
     * Server-side component for the {@code <vaadin-grid-column>} element.
     *
     * @param <T>
     *            type of the underlying grid this column is compatible with
     */
    @Tag("vaadin-grid-column")
    public static class Column<T> extends AbstractColumn<Column<T>> {

        private final String columnInternalId; // for internal implementation
                                               // only
        private String columnKey; // defined and used by the user

        private boolean sortingEnabled;

        private Component editorComponent;
        private EditorRenderer<T> editorRenderer;
        private Registration editorRendererRegistration;

        private SortOrderProvider sortOrderProvider = direction -> {
            String key = getKey();
            if (key == null) {
                return Stream.empty();
            }
            return Stream.of(new QuerySortOrder(key, direction));
        };

        SerializableComparator<T> comparator;

        private final CompositeDataGenerator<T> compositeDataGenerator = new CompositeDataGenerator<>() {
            @Override
            public void generateData(T item, ObjectNode jsonObject) {
                if (Column.this.isVisible()) {
                    super.generateData(item, jsonObject);
                }
            }

            @Override
            public void refreshData(T item) {
                if (Column.this.isVisible()) {
                    super.refreshData(item);
                }
            }
        };
        private Registration compositeDataGeneratorRegistration;

        private Renderer<T> renderer;
        private List<Registration> rendererRegistrations = new ArrayList<>();

        private SerializableFunction<T, String> partNameGenerator = item -> null;
        private SerializableFunction<T, String> tooltipGenerator = item -> null;

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
        public Column(GridBase<?, T> grid, String columnId,
                Renderer<T> renderer) {
            super(grid);
            this.columnInternalId = columnId;
            comparator = (a, b) -> 0;
            compositeDataGeneratorRegistration = grid
                    .addDataGenerator(compositeDataGenerator);
            compositeDataGenerator.addDataGenerator(this::generatePartData);
            compositeDataGenerator.addDataGenerator(this::generateTooltipData);
            setupRenderer(renderer);
        }

        /**
         * Adds a data generator for this column.
         *
         * @param dataGenerator
         *            the data generator to add
         * @return a registration for removing the data generator
         * @since 25.3
         */
        protected Registration addDataGenerator(
                DataGenerator<T> dataGenerator) {
            return compositeDataGenerator.addDataGenerator(dataGenerator);
        }

        private void generatePartData(T item, ObjectNode jsonObject) {
            String partName = partNameGenerator.apply(item);
            if (partName != null) {
                jsonObject.withObjectProperty("part").put(columnInternalId,
                        partName);
            }
        }

        private void generateTooltipData(T item, ObjectNode jsonObject) {
            String tooltip = tooltipGenerator.apply(item);
            if (tooltip != null) {
                jsonObject.withObjectProperty("gridtooltips")
                        .put(columnInternalId, tooltip);
            }
        }

        @Override
        public void setVisible(boolean visible) {
            boolean refreshViewport = visible && !isVisible();
            super.setVisible(visible);
            if (refreshViewport) {
                getGrid().refreshViewport();
            }
        }

        protected void destroyDataGenerators() {
            if (rendererRegistrations != null) {
                rendererRegistrations.forEach(Registration::remove);
                rendererRegistrations.clear();
            }

            if (editorRendererRegistration != null) {
                editorRendererRegistration.remove();
                editorRendererRegistration = null;
            }

            if (compositeDataGeneratorRegistration != null) {
                compositeDataGeneratorRegistration.remove();
                compositeDataGeneratorRegistration = null;
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
         * @since 2.0
         */
        public Renderer<T> getRenderer() {
            return renderer;
        }

        /**
         * Set the renderer for this column.
         *
         * @param renderer
         *            the new renderer to be used for this column, must not be
         *            {@code null}
         *
         * @since 24.1
         */
        public Column<T> setRenderer(Renderer<T> renderer) {
            setupRenderer(renderer);

            // The editor renderer is a wrapper around the regular renderer, so
            // we need to apply it again afterwards
            if (editorRenderer != null) {
                setupEditorRenderer();
            }

            getGrid().refreshViewport();
            return this;
        }

        @SuppressWarnings({ "unchecked" })
        private void setupRenderer(Renderer<T> renderer) {
            this.renderer = Objects.requireNonNull(renderer,
                    "Renderer must not be null.");

            if (rendererRegistrations != null) {
                rendererRegistrations.forEach(Registration::remove);
                rendererRegistrations.clear();
            }

            Rendering<T> rendering = renderer.render(getElement(),
                    (KeyMapper<T>) getGrid().getDataCommunicator()
                            .getKeyMapper());

            rendering.getDataGenerator().ifPresent(dataGenerator -> {
                rendererRegistrations.add(
                        compositeDataGenerator.addDataGenerator(dataGenerator));
            });

            rendererRegistrations.add(rendering.getRegistration());
        }

        /**
         * Sets the width of this column as a CSS-string.
         * <p>
         * Please note that using the {@code em} length unit is discouraged as
         * it might lead to misalignment issues if the header, body, and footer
         * cells have different font sizes. Instead, use {@code rem} if you need
         * a length unit relative to the font size.
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
         * @since 4.0
         */
        public Column<T> setAutoWidth(boolean autoWidth) {
            getElement().setProperty("autoWidth", autoWidth);
            return this;
        }

        /**
         * Gets this column's auto width state.
         *
         * @return whether this column has automatic width enabled
         * @since 4.0
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
         * <strong>Note:</strong> the comparator is only used with in-memory
         * data providers, such as {@link ListDataProvider}. It has no effect
         * when the grid uses a lazy data provider. In that case, define the
         * sort properties with {@link Column#setSortProperty(String...)} and
         * implement the sorting in the data provider, which receives the
         * properties through {@link Query#getSortOrders()}.
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
         * based on the return type of the given {@link ValueProvider}. Sorting
         * with a back-end is done using
         * {@link Column#setSortProperty(String[])}.
         * <p>
         * <strong>Note:</strong> the comparator is only used with in-memory
         * data providers, such as {@link ListDataProvider}. It has no effect
         * when the grid uses a lazy data provider. In that case, define the
         * sort properties with {@link Column#setSortProperty(String...)} and
         * implement the sorting in the data provider, which receives the
         * properties through {@link Query#getSortOrders()}.
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
         * <strong>Note:</strong> as a side effect, calling this method also
         * sets the column as sortable with {@link #setSortable(boolean)}.
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
         * <strong>Note:</strong> sort properties are only used with lazy data
         * providers, which receive them through {@link Query#getSortOrders()}
         * and are responsible for applying the sorting. They are ignored by
         * in-memory data providers, such as {@link ListDataProvider}, even if
         * the items are originally loaded from a backend. Use
         * {@link Column#setComparator(Comparator)} for in-memory sorting.
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
         * <strong>Note:</strong> the sort orders are only used with lazy data
         * providers, which receive them through {@link Query#getSortOrders()}
         * and are responsible for applying the sorting. They are ignored by
         * in-memory data providers, such as {@link ListDataProvider}. Use
         * {@link Column#setComparator(Comparator)} for in-memory sorting.
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
         * <p>
         * <strong>Note:</strong> this method only controls the sorting UI in
         * the column header; it does not define how the data is sorted. Columns
         * created with {@link Grid#addColumn(ValueProvider)} are sorted
         * in-memory automatically, based on the values returned by the value
         * provider. For other columns, or when the grid uses a lazy data
         * provider, define the sorting with {@link #setComparator(Comparator)}
         * (in-memory) or {@link #setSortProperty(String...)} (lazy data
         * provider). Without one of these, activating the sorter has no effect
         * on the data.
         * <p>
         * <strong>Note:</strong> the sorter is rendered as part of the column
         * header content, so the column needs a header for the user to be able
         * to sort it.
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
            grid.updateClientSorterDirections();
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
            grid.updateClientSorterDirections();
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
         * @since 2.1
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
         * @since 2.1
         */
        public Column<T> setEditorComponent(
                SerializableFunction<T, ? extends Component> componentCallback) {

            editorComponent = null;
            if (editorRenderer == null && componentCallback != null) {
                setupEditorRenderer();
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
         * @since 2.1
         */
        public Component getEditorComponent() {
            return editorComponent;
        }

        /**
         * Sets the function that is used for generating CSS part names for
         * cells in this column. Returning {@code null} from the generator
         * results in no custom part name being set. Multiple part names can be
         * returned from the generator as space-separated.
         * <p>
         * If {@link Grid#setPartNameGenerator(SerializableFunction)} is used
         * together with this method, resulting part names from both methods
         * will be effective.
         *
         * @param partNameGenerator
         *            the part name generator to set, not {@code null}
         * @return this column
         * @throws NullPointerException
         *             if {@code partNameGenerator} is {@code null}
         * @see Grid#setPartNameGenerator(SerializableFunction)
         * @since 24.0
         */
        public Column<T> setPartNameGenerator(
                SerializableFunction<T, String> partNameGenerator) {
            Objects.requireNonNull(partNameGenerator,
                    "Part name generator can not be null");
            this.partNameGenerator = partNameGenerator;
            getGrid().refreshViewport();
            return this;
        }

        /**
         * Sets the function that is used for generating tooltip text for cells
         * in this column. Returning {@code null} from the generator results in
         * no tooltip being set.
         *
         * @param tooltipGenerator
         *            the tooltip generator to set, not {@code null}
         * @return this column
         * @throws NullPointerException
         *             if {@code tooltipGenerator} is {@code null}
         * @since 23.3
         */
        public Column<T> setTooltipGenerator(
                SerializableFunction<T, String> tooltipGenerator) {
            this.tooltipGenerator = Objects.requireNonNull(tooltipGenerator,
                    "Tooltip generator can not be null");

            grid.addTooltipElementToTooltipSlot();
            getGrid().refreshViewport();
            return this;
        }

        /**
         * Gets the function that is used for generating CSS part names for
         * cells in this column.
         *
         * @return the part name generator
         * @since 24.0
         */
        public SerializableFunction<T, String> getPartNameGenerator() {
            return partNameGenerator;
        }

        public SerializableFunction<T, String> getTooltipGenerator() {
            return tooltipGenerator;
        }

        /**
         * Gets whether cells in this column should be announced as row headers.
         *
         * @return whether cells in this column should be announced as row
         *         headers.
         * @since 24.2
         */
        public boolean isRowHeader() {
            return getElement().getProperty("rowHeader", false);
        }

        /**
         * Sets whether cells in this column should be announced as row headers.
         * When {@code true}, the cells for this column will be rendered with
         * the {@code role} attribute set as {@code rowheader}, instead of the
         * {@code gridcell} role value used by default.
         * <p>
         * When a column is set as row header, its cells will be announced by
         * screen readers while navigating to help user identify the current row
         * as uniquely as possible.
         *
         * @param rowHeader
         *            whether cells in this column should be announced as row
         *            headers
         * @since 24.2
         */
        public Column<T> setRowHeader(boolean rowHeader) {
            getElement().setProperty("rowHeader", rowHeader);
            return this;
        }

        @Override
        protected Column<?> getBottomLevelColumn() {
            return this;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void setupEditorRenderer() {
            if (editorRenderer == null) {
                editorRenderer = new EditorRenderer<>((Editor) grid.getEditor(),
                        columnInternalId);
                editorRendererRegistration = compositeDataGenerator
                        .addDataGenerator(editorRenderer);
            }

            editorRenderer.render(getElement(), null);
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

        private GridBase<?, T> grid;
        private Registration registration;

        /**
         * Constructs a new grid extension, extending the given grid.
         *
         * @param grid
         *            the grid to extend
         */
        public AbstractGridExtension(GridBase<?, T> grid) {
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
        protected void extend(GridBase<?, T> grid) {
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
        protected GridBase<?, T> getGrid() {
            return grid;
        }
    }

    /**
     * Creates a new instance, with page size of 50.
     */
    public Grid() {
        this(50);
    }

    /**
     * Creates a new grid using the given generic {@link DataProvider}.
     *
     * @param dataProvider
     *            the data provider, not {@code null}
     *
     * @since 24.1
     */
    public Grid(DataProvider<T, Void> dataProvider) {
        this();
        setItems(dataProvider);
    }

    /**
     * Creates a new grid using the given {@link BackEndDataProvider}.
     *
     * @param dataProvider
     *            the data provider, not {@code null}
     *
     * @since 24.1
     */
    public Grid(BackEndDataProvider<T, Void> dataProvider) {
        this();
        setItems(dataProvider);
    }

    /**
     * Creates a new grid using the given {@link InMemoryDataProvider}.
     *
     * @param inMemoryDataProvider
     *            the data provider, not {@code null}
     *
     * @since 24.1
     */
    public Grid(InMemoryDataProvider<T> inMemoryDataProvider) {
        this();
        setItems(inMemoryDataProvider);
    }

    /**
     * Creates a new grid using the given {@link ListDataProvider}.
     *
     * @param dataProvider
     *            the data provider, not {@code null}
     *
     * @since 24.1
     */
    public Grid(ListDataProvider<T> dataProvider) {
        this();
        setItems(dataProvider);
    }

    /**
     * Creates a new grid using the given collection of items using a
     * {@link ListDataProvider}.
     *
     * @param items
     *            the collection of items, not {@code null}
     *
     * @since 24.1
     */
    public Grid(Collection<T> items) {
        this();
        setItems(items);
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
        super(pageSize);
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
     * @since 2.0
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
     * @param dataCommunicatorBuilder
     *            Builder for {@link DataCommunicator} implementation this Grid
     *            uses to handle all data communication.
     * @param <B>
     *            the data communicator builder type
     * @param <U>
     *            the GridArrayUpdater type
     * @since 24.9
     * @deprecated Override {@link #createDataCommunicator()} instead. This
     *             constructor will be removed in Vaadin 26.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    protected <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> Grid(
            Class<T> beanType, B dataCommunicatorBuilder) {
        this(beanType, dataCommunicatorBuilder, true);
    }

    /**
     * Creates a new grid with an initial set of columns for each of the bean's
     * properties. The property-values of the bean will be converted to Strings.
     * Full names of the properties will be used as the
     * {@link Column#setKey(String) column keys} and the property captions will
     * be used as the {@link Column#setHeader(String) column headers}.
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
     * @param dataCommunicatorBuilder
     *            Builder for {@link DataCommunicator} implementation this Grid
     *            uses to handle all data communication.
     * @param <B>
     *            the data communicator builder type
     * @param <U>
     *            the GridArrayUpdater type
     * @param autoCreateColumns
     *            when <code>true</code>, columns are created automatically for
     *            the properties of the beanType
     * @since 24.9
     * @deprecated Override {@link #createDataCommunicator()} instead. This
     *             constructor will be removed in Vaadin 26.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    protected <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> Grid(
            Class<T> beanType, B dataCommunicatorBuilder,
            boolean autoCreateColumns) {
        super(beanType, dataCommunicatorBuilder, autoCreateColumns);
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
     * @param dataCommunicatorBuilder
     *            Builder for {@link DataCommunicator} implementation this Grid
     *            uses to handle all data communication.
     * @param <B>
     *            the data communicator builder type
     * @param <U>
     *            the GridArrayUpdater type
     * @since 24.9
     * @deprecated Override {@link #createDataCommunicator()} instead. This
     *             constructor will be removed in Vaadin 26.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    protected <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> Grid(
            int pageSize, B dataCommunicatorBuilder) {
        super(pageSize, dataCommunicatorBuilder);
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
     * @since 18.0
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
     * @since 18.0
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
     * @since 24.7
     */
    public interface SpringData extends Serializable {
        /**
         * Callback interface for fetching a list of items from a backend based
         * on a Spring Data Pageable.
         *
         * @param <T>
         *            the type of the items to fetch
         */
        @FunctionalInterface
        public interface FetchCallback<PAGEABLE, T> extends Serializable {

            /**
             * Fetches a list of items based on a pageable. The pageable defines
             * the paging of the items to fetch and the sorting.
             *
             * @param pageable
             *            the pageable that defines which items to fetch and the
             *            sort order
             * @return a list of items
             */
            List<T> fetch(PAGEABLE pageable);
        }

        /**
         * Callback interface for counting the number of items in a backend
         * based on a Spring Data Pageable.
         */
        @FunctionalInterface
        public interface CountCallback<PAGEABLE> extends Serializable {
            /**
             * Counts the number of available items based on a pageable. The
             * pageable defines the paging of the items to fetch and the sorting
             * and is provided although it is generally not needed for
             * determining the number of items.
             *
             * @param pageable
             *            the pageable that defines which items to fetch and the
             *            sort order
             * @return the number of available items
             */
            long count(PAGEABLE pageable);
        }
    }

    /**
     * Supply items lazily with a callback from a backend based on a Spring Data
     * Pageable. The component will automatically fetch more items and adjust
     * its size until the backend runs out of items. Usage example:
     * <p>
     * {@code component.setItemsPageable(pageable -> orderService.getOrders(pageable));}
     * <p>
     * The returned data view object can be used for further configuration, or
     * later on fetched with {@link #getLazyDataView()}. For using in-memory
     * data, like {@link java.util.Collection}, use
     * {@link HasListDataView#setItems(Collection)} instead.
     *
     * @param fetchCallback
     *            a function that returns a sorted list of items from the
     *            backend based on the given pageable
     * @return a data view for further configuration
     * @since 24.7
     */
    public GridLazyDataView<T> setItemsPageable(
            SpringData.FetchCallback<Pageable, T> fetchCallback) {
        return setItems(
                query -> handleSpringFetchCallback(query, fetchCallback));
    }

    /**
     * Supply items lazily with callbacks: the first one fetches a list of items
     * from a backend based on a Spring Data Pageable, the second provides the
     * exact count of items in the backend. Use this in case getting the count
     * is cheap and the user benefits from the component showing immediately the
     * exact size. Usage example:
     * <p>
     * {@code component.setItemsPageable(
     *                    pageable -> orderService.getOrders(pageable),
     *                    pageable -> orderService.countOrders());}
     * <p>
     * The returned data view object can be used for further configuration, or
     * later on fetched with {@link #getLazyDataView()}. For using in-memory
     * data, like {@link java.util.Collection}, use
     * {@link HasListDataView#setItems(Collection)} instead.
     *
     * @param fetchCallback
     *            a function that returns a sorted list of items from the
     *            backend based on the given pageable
     * @param countCallback
     *            a function that returns the number of items in the back end
     * @return LazyDataView instance for further configuration
     * @since 24.7
     */
    public GridLazyDataView<T> setItemsPageable(
            SpringData.FetchCallback<Pageable, T> fetchCallback,
            SpringData.CountCallback<Pageable> countCallback) {
        return setItems(
                query -> handleSpringFetchCallback(query, fetchCallback),
                query -> handleSpringCountCallback(query, countCallback));
    }

    @SuppressWarnings("unchecked")
    private static <PAGEABLE, T> Stream<T> handleSpringFetchCallback(
            Query<T, Void> query,
            SpringData.FetchCallback<PAGEABLE, T> fetchCallback) {
        PAGEABLE pageable = (PAGEABLE) VaadinSpringDataHelpers
                .toSpringPageRequest(query);
        List<T> itemList = fetchCallback.fetch(pageable);
        return itemList.stream();
    }

    @SuppressWarnings("unchecked")
    private static <PAGEABLE> int handleSpringCountCallback(
            Query<?, Void> query,
            SpringData.CountCallback<PAGEABLE> countCallback) {
        PAGEABLE pageable = (PAGEABLE) VaadinSpringDataHelpers
                .toSpringPageRequest(query);
        long count = countCallback.count(pageable);
        if (count > Integer.MAX_VALUE) {
            LoggerFactory.getLogger(Grid.class).warn(
                    "The count of items in the backend ({}) exceeds the maximum supported by the Grid.",
                    count);
            return Integer.MAX_VALUE;
        }
        return (int) count;
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
     * @since 18.0
     */
    @Override
    public GridLazyDataView<T> getLazyDataView() {
        return new GridLazyDataView<>(getDataCommunicator(), this);
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
     * @since 4.1
     */
    public void scrollToIndex(int rowIndex) {
        setViewportRangeByIndex(rowIndex);

        scheduleScrollExecution(
                () -> getElement().callJsFunction("scrollToIndex", rowIndex));
    }

    private void setViewportRangeByIndex(int rowIndex) {
        // Grid's page size
        int pageSize = getPageSize();
        // A rough approximation of the viewport size in rows. This affects the
        // count of preloaded rows.
        int viewportSizeEstimate = 40;

        // Get the index of the first item on the page that contains the
        // requested index
        int targetPageStartIndex = rowIndex - rowIndex % pageSize;

        // The last index we want to include in the preloaded range
        int lastIndex = rowIndex + viewportSizeEstimate;

        // Get the index of the last item on the page that contains the last
        // index we want to preload
        int lastIndexPageStartIndex = lastIndex - lastIndex % pageSize;
        int lastIndexPageEndIndex = lastIndexPageStartIndex + pageSize - 1;

        // Preloaded items count
        int preloadedItemsCount = lastIndexPageEndIndex - targetPageStartIndex
                + 1;
        // Preload the items
        setViewportRange(targetPageStartIndex, preloadedItemsCount);
    }

    /**
     * Scrolls to the given item unless it is already fully visible.
     * <p>
     * For this method to work with a lazy-loading data provider, an item index
     * provider must be supplied via
     * {@link GridLazyDataView#setItemIndexProvider(ItemIndexProvider)}. If none
     * is provided, an {@link UnsupportedOperationException} will be thrown.
     *
     * @param item
     *            the item to scroll to, not {@code null}.
     * @throws NullPointerException
     *             if the {@code item} parameter is {@code null}.
     * @throws NoSuchElementException
     *             if the {@code item} cannot be found.
     * @throws UnsupportedOperationException
     *             if {@link ItemIndexProvider} is missing for grid with a lazy
     *             loading data provider.
     * @since 24.4
     */
    @Override
    public void scrollToItem(T item) {
        Objects.requireNonNull(item, "Item to scroll to cannot be null.");
        AbstractDataView<T> dataView = getDataProvider().isInMemory()
                ? getListDataView()
                : getLazyDataView();
        var itemKey = getDataCommunicator().getKeyMapper().key(item);
        int itemIndex = dataView.getItemIndex(item)
                .orElseThrow(() -> new NoSuchElementException(
                        "Item to scroll to cannot be found: " + item));

        setViewportRangeByIndex(itemIndex);

        scheduleScrollExecution(() -> getElement()
                .callJsFunction("$connector.scrollToItem", itemKey, itemIndex));
    }

    @Override
    public void scrollToStart() {
        scrollToIndex(0);
    }

    /**
     * Scrolls to the last data row of the grid.
     *
     * @since 4.1
     */
    @Override
    public void scrollToEnd() {
        scheduleScrollExecution(() -> getElement()
                .executeJs("this.scrollToIndex(this._flatSize)"));
    }

    private void onInMemoryFilterOrSortingChange(
            SerializablePredicate<T> filter,
            SerializableComparator<T> sortComparator) {
        updateInMemorySorting(sortComparator);
        updateInMemoryFiltering(filter);
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
}

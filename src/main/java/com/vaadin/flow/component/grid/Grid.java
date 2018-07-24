/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.grid.GridArrayUpdater.UpdateQueueData;
import com.vaadin.flow.data.binder.BeanPropertySet;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.event.SortEvent.SortNotifier;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataGenerators;
import com.vaadin.flow.data.provider.KeyMapper;
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
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.internal.ReflectTools;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * Server-side component for the {@code <vaadin-grid>} element.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the grid bean type
 *
 */
@Tag("vaadin-grid")
@HtmlImport("frontend://bower_components/vaadin-grid/src/vaadin-grid.html")
@HtmlImport("frontend://bower_components/vaadin-grid/src/vaadin-grid-column.html")
@HtmlImport("frontend://bower_components/vaadin-grid/src/vaadin-grid-sorter.html")
@HtmlImport("frontend://bower_components/vaadin-checkbox/src/vaadin-checkbox.html")
@HtmlImport("frontend://flow-component-renderer.html")
@JavaScript("frontend://gridConnector.js")
public class Grid<T> extends Component implements HasDataProvider<T>, HasStyle,
        HasSize, Focusable<Grid<T>>, SortNotifier<Grid<T>, GridSortOrder<T>>,
        HasTheme, HasDataGenerators<T> {

    protected static class UpdateQueue implements Update {
        private final ArrayList<Runnable> queue = new ArrayList<>();
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
            queue.forEach(Runnable::run);
            queue.clear();
        }

        public void enqueue(String name, Serializable... arguments) {
            queue.add(() -> getElement().callFunction(name, arguments));
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

                    @Override
                    protected void fireSelectionEvent(
                            SelectionEvent<Grid<T>, T> event) {
                        grid.fireEvent((ComponentEvent<Grid>) event);
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
     * @param <T>
     *            type of the underlying grid this column is compatible with
     */
    @Tag("vaadin-grid-column")
    public static class Column<T> extends AbstractColumn<Column<T>> {

        private final String columnInternalId; // for internal implementation
                                               // only
        private String columnKey; // defined and used by the user

        private boolean sortingEnabled;

        private SortOrderProvider sortOrderProvider = direction -> {
            String key = getKey();
            if (key == null) {
                return Stream.empty();
            }
            return Stream.of(new QuerySortOrder(key, direction));
        };

        private SerializableComparator<T> comparator;

        private Registration columnDataGeneratorRegistration;

        private Renderer<T> renderer;

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
        public Column(Grid<T> grid, String columnId, Renderer<T> renderer) {
            super(grid);
            Objects.requireNonNull(renderer);
            this.columnInternalId = columnId;
            this.renderer = renderer;

            comparator = (a, b) -> 0;

            Rendering<T> rendering = renderer.render(getElement(),
                    (KeyMapper<T>) getGrid().getDataCommunicator()
                            .getKeyMapper());
            Optional<DataGenerator<T>> dataGenerator = rendering
                    .getDataGenerator();

            if (dataGenerator.isPresent()) {
                columnDataGeneratorRegistration = grid
                        .addDataGenerator(dataGenerator.get());
            }
        }

        protected void destroyDataGenerators() {
            if (columnDataGeneratorRegistration != null) {
                columnDataGeneratorRegistration.remove();
                columnDataGeneratorRegistration = null;
            }
        }

        protected String getInternalId() {
            return columnInternalId;
        }

        /**
         * Get the renderer used for this column.
         *
         * Note: Mutating the renderer after the Grid has been rendered on the
         * client will not change the column, and can lead to undefined
         * behavior.
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
         * @return the width of this column as a CSS-string
         */
        @Synchronize("width-changed")
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
         * Gets the currently set flex grow value, by default 1.
         *
         * @return the currently set flex grow value, by default 1
         */
        @Synchronize("flex-grow-changed")
        public int getFlexGrow() {
            return getElement().getProperty("flexGrow", 1);
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
         * {@link Column#setSortProperty(String...)}.
         * <p>
         * <strong>Note:</strong> calling this method automatically sets the
         * column as sortable with {@link #setSortable(boolean)}.
         *
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

        @Override
        protected Column<?> getBottomLevelColumn() {
            return this;
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
            getGrid().addDataGenerator(this);
        }

        /**
         * Remove this extension from its target.
         */
        protected void remove() {
            getGrid().removeDataGenerator(this);
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
     * @param <T>
     *            the grid bean type
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

        @Override
        public void destroyData(T item) {
            detailsVisible.remove(item);
            if (itemDetailsDataGenerator != null) {
                itemDetailsDataGenerator.destroyData(item);
            }
        }

        @Override
        public void destroyAllData() {
            detailsVisible.clear();
            if (itemDetailsDataGenerator != null) {
                itemDetailsDataGenerator.destroyAllData();
            }
        }

        @Override
        public void refreshData(T item) {
            if (itemDetailsDataGenerator != null && isDetailsVisible(item)) {
                itemDetailsDataGenerator.refreshData(item);
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
    private boolean detailsVisibleOnClick = true;

    private Map<String, Column<T>> idToColumnMap = new HashMap<>();
    private Map<String, Column<T>> keyToColumnMap = new HashMap<>();

    private final List<GridSortOrder<T>> sortOrder = new ArrayList<>();

    private PropertySet<T> propertySet;

    private DataGenerator<T> itemDetailsDataGenerator;

    /**
     * Keeps track of the layers of column and column-group components. The
     * layers are in order from innermost to outmost.
     */
    private List<ColumnLayer> columnLayers = new ArrayList<>();
    private HeaderRow defaultHeaderRow;

    private String uniqueKeyProperty;

    private ValueProvider<T, String> uniqueKeyProvider;

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
     * be used as the {@link Column#setHeader(String) column headers}.
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
        this();
        Objects.requireNonNull(beanType, "Bean type can't be null");
        propertySet = BeanPropertySet.get(beanType);
        propertySet.getProperties()
                .filter(property -> !property.isSubProperty()).sorted((prop1,
                        prop2) -> prop1.getName().compareTo(prop2.getName()))
                .forEach(this::addColumn);
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
     * @param updateQueueBuidler
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
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueBuidler,
            B dataCommunicatorBuilder) {
        this(50, updateQueueBuidler, dataCommunicatorBuilder);
        Objects.requireNonNull(beanType, "Bean type can't be null");
        Objects.requireNonNull(dataCommunicatorBuilder,
                "Data communicator builder can't be null");
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
     * @param updateQueueBuidler
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
    protected <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> Grid(
            int pageSize,
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueBuidler,
            B dataCommunicatorBuilder) {
        Objects.requireNonNull(dataCommunicatorBuilder,
                "Data communicator builder can't be null");
        arrayUpdater = createDefaultArrayUpdater(
                Optional.ofNullable(updateQueueBuidler)
                        .orElseGet(() -> UpdateQueue::new));
        arrayUpdater.setUpdateQueueData(
                new UpdateQueueData(getElement(), getUniqueKeyProperty()));
        gridDataGenerator = new CompositeDataGenerator<>();
        gridDataGenerator.addDataGenerator(this::generateUniqueKeyData);

        dataCommunicator = dataCommunicatorBuilder.build(getElement(),
                gridDataGenerator, (U) arrayUpdater,
                this::getUniqueKeyProvider);

        detailsManager = new DetailsManager(this);
        setPageSize(pageSize);
        setSelectionModel(SelectionMode.SINGLE.createModel(this),
                SelectionMode.SINGLE);

        columnLayers.add(new ColumnLayer(this));
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
                .getPage().executeJavaScript(
                        "window.Vaadin.Flow.gridConnector.initLazy($0)",
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
            return new DataCommunicator<>(dataGenerator, arrayUpdater,
                    data -> element.callFunction("$connector.updateData", data),
                    element.getNode());
        }
    }

    protected GridArrayUpdater createDefaultArrayUpdater(
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory) {
        return new GridArrayUpdaterImpl(updateQueueFactory);
    }

    /**
     * Adds a new text column to this {@link Grid} with a value provider. The
     * value is converted to String when sent to the client by using
     * {@link String#valueOf(Object)}.
     * <p>
     * <em>NOTE:</em> For displaying components, see
     * {@link #addComponentColumn(ValueProvider)}. For using build-in renderers,
     * see {@link #addColumn(Renderer)}.
     *
     * @param valueProvider
     *            the value provider
     * @return the created column
     * @see #addComponentColumn(ValueProvider)
     * @see #addColumn(Renderer)
     */
    public Column<T> addColumn(ValueProvider<T, ?> valueProvider) {
        String columnId = createColumnId(false);

        Column<T> column = addColumn(TemplateRenderer
                .<T> of("[[item." + columnId + "]]")
                .withProperty(columnId, value -> formatValueToSendToTheClient(
                        valueProvider.apply(value))));
        column.comparator = ((a, b) -> compareMaybeComparables(
                valueProvider.apply(a), valueProvider.apply(b)));
        return column;
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
     *
     * @param componentProvider
     *            a value provider that will return a component for the given
     *            item
     * @param <V>
     *            the component type
     * @return the new column
     * @see #addColumn(Renderer)
     */
    public <V extends Component> Column<T> addComponentColumn(
            ValueProvider<T, V> componentProvider) {
        return addColumn(new ComponentRenderer<>(componentProvider));
    }

    /**
     * Adds a new text column to this {@link Grid} with a value provider and
     * sorting properties. The value is converted to a JSON value by using
     * {@link JsonSerializer#toJson(Object)}. The sorting properties are used to
     * configure backend sorting for this column. In-memory sorting is
     * automatically configured using the return type of the given
     * {@link ValueProvider}.
     *
     * @see Column#setComparator(ValueProvider)
     * @see Column#setSortProperty(String...)
     *
     * @param valueProvider
     *            the value provider
     * @param sortingProperties
     *            the sorting properties to use with this column
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
     * Adds a new text column to this {@link Grid} with a renderer.
     * <p>
     * See implementations of the {@link Renderer} interface for built-in
     * renderer options with type safe APIs. For a renderer using template
     * binding, use {@link TemplateRenderer#of(String)}.
     * <p>
     * </><em>NOTE:</em> You can add component columns easily using the
     * {@link #addComponentColumn(ValueProvider)}, but using
     * {@link ComponentRenderer} is not as efficient as the built in renderers
     * or using {@link TemplateRenderer}.
     *
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @return the created column
     *
     * @see TemplateRenderer#of(String)
     * @see #addComponentColumn(ValueProvider)
     */
    public Column<T> addColumn(Renderer<T> renderer) {
        String columnId = createColumnId(true);

        getDataCommunicator().reset();

        Column<T> column = new Column<>(this, columnId, renderer);
        idToColumnMap.put(columnId, column);

        AbstractColumn<?> current = column;
        columnLayers.get(0).addColumn(column);

        for (int i = 1; i < columnLayers.size(); i++) {
            ColumnGroup group = new ColumnGroup(this, current);
            columnLayers.get(i).addColumn(group);
            current = group;
        }
        getElement().appendChild(current.getElement());

        return column;
    }

    /**
     * Adds a new text column to this {@link Grid} with a template renderer and
     * sorting properties. The values inside the renderer are converted to JSON
     * values by using {@link JsonSerializer#toJson(Object)}.
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
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @param sortingProperties
     *            the sorting properties to use for this column
     * @return the created column
     */
    public Column<T> addColumn(Renderer<T> renderer,
            String... sortingProperties) {
        Column<T> column = addColumn(renderer);

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
        column.setComparator(combinedComparator);
        return column;
    }

    /**
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     * <p>
     * Adds a new column for the given property name. The property values are
     * converted to Strings in the grid cells. The property's full name will be
     * used as the {@link Column#setKey(String) column key} and the property
     * caption will be used as the {@link Column#setHeader(String) column
     * header}.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     *
     * @param propertyName
     *            the property name of the new column, not <code>null</code>
     * @return the created column
     */
    public Column<T> addColumn(String propertyName) {
        if (propertySet == null) {
            throw new UnsupportedOperationException(
                    "This method can't be used for a Grid that isn't constructed from a bean type");
        }
        Objects.requireNonNull(propertyName, "Property name can't be null");

        PropertyDefinition<T, ?> property;
        try {
            property = propertySet.getProperty(propertyName).get();
        } catch (NoSuchElementException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Can't resolve property name '"
                    + propertyName + "' from '" + propertySet + "'");
        }
        return addColumn(property);
    }

    private Column<T> addColumn(PropertyDefinition<T, ?> property) {
        Column<T> column = addColumn(
                item -> runPropertyValueGetter(property, item))
                        .setHeader(property.getCaption());
        try {
            return column.setKey(property.getName());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Multiple columns for the same property: "
                            + property.getName());
        }
    }

    private Object runPropertyValueGetter(PropertyDefinition<T, ?> property,
            T item) {
        return property.getGetter().apply(item);
    }

    /**
     * <strong>Note:</strong> This method can only be used for a Grid created
     * from a bean type with {@link #Grid(Class)}.
     * <p>
     * Sets the columns and their order based on the given properties.
     * <p>
     * This is a shortcut for removing all columns and then calling
     * {@link #addColumn(String)} for each of the given propertyNames.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * Note that this also resets the headers and footers.
     * 
     * @param propertyNames
     *            the properties to create columns for
     */
    public void setColumns(String... propertyNames) {
        if (propertySet == null) {
            throw new UnsupportedOperationException(
                    "This method can't be used for a Grid that isn't constructed from a bean type");
        }
        getColumns().forEach(this::removeColumn);
        Stream.of(propertyNames).forEach(this::addColumn);
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

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider, "data provider cannot be null");
        deselectAll();
        getDataCommunicator().setDataProvider(dataProvider, null);

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
     * Returns the data provider of this grid.
     *
     * @return the data provider of this grid, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return getDataCommunicator().getDataProvider();
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
     * the dataprovider.
     * <p>
     * Note: the number of items in the server-side memory can be considerably
     * higher than the page size, since the component can show more than one
     * page at a time.
     * <p>
     * Setting the pageSize after the Grid has been rendered effectively resets
     * the component, and the current page(s) and sent over again.
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
        getElement().callFunction("$connector.reset");
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
        getElement().callFunction("$connector.setSelectionMode",
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
     *            the item to select
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
     *            the item to deselect
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

    private void callSelectionFunctionForItems(String function, Set<T> items) {
        if (items.isEmpty()) {
            return;
        }
        Serializable[] values = new Serializable[items.size() + 1];
        List<Serializable> collect = items.stream()
                .map(item -> generateJsonForSelection(item))
                .map(item -> (Serializable) item).collect(Collectors.toList());
        collect.add(1, false);
        collect.toArray(values);
        getElement().callFunction("$connector." + function, values);
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
        detailsManager.destroyAllData();
        itemDetailsDataGenerator = null;
        if (renderer == null) {
            return;
        }

        Rendering<T> rendering;
        if (detailsTemplate == null) {
            rendering = renderer.render(getElement(),
                    getDataCommunicator().getKeyMapper());
            detailsTemplate = rendering.getTemplateElement();
            detailsTemplate.setAttribute("class", "row-details");
        } else {
            rendering = renderer.render(getElement(),
                    getDataCommunicator().getKeyMapper(), detailsTemplate);
        }

        Optional<DataGenerator<T>> dataGenerator = rendering.getDataGenerator();

        if (dataGenerator.isPresent()) {
            itemDetailsDataGenerator = dataGenerator.get();
        }
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

    /**
     * Removes a column from the Grid.
     *
     * @param column
     *            the column to be removed, not <code>null</code>
     * @throws IllegalArgumentException
     *             if column is <code>null</code> or if it is not part of this
     *             Grid
     */
    public void removeColumn(Column<T> column) {
        Objects.requireNonNull(column, "column should not be null");

        if (!column.getGrid().equals(this)
                || column.getElement().getParent() == null) {
            throw new IllegalArgumentException("The column with key '"
                    + column.getKey() + "' is not part of this Grid");
        }
        removeColumnAndColumnGroupsIfNeeded(column);
        column.destroyDataGenerators();
        keyToColumnMap.remove(column.getKey());
        idToColumnMap.remove(column.getInternalId());
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
        if (this.detailsVisibleOnClick != detailsVisibleOnClick) {
            this.detailsVisibleOnClick = detailsVisibleOnClick;
            getElement().callFunction("$connector.setDetailsVisibleOnClick",
                    detailsVisibleOnClick);
        }
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
        return detailsVisibleOnClick;
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
        return multiSort == null ? false : Boolean.valueOf(multiSort);
    }

    private List<Column<T>> fetchChildColumns(ColumnGroup columnGroup) {
        List<Column<T>> ret = new ArrayList<>();
        columnGroup.getChildColumns()
                .forEach(column -> appendChildColumns(ret, column));
        return ret;
    }

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
        getSelectionModel().selectFromClient(findByKey(key));
    }

    @ClientCallable
    private void deselect(String key) {
        getSelectionModel().deselectFromClient(findByKey(key));
    }

    private T findByKey(String key) {
        T item = getDataCommunicator().getKeyMapper().get(String.valueOf(key));
        if (item == null) {
            throw new IllegalStateException("Unknown key: " + key);
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
            detailsManager.setDetailsVisibleFromClient(Collections
                    .singleton(getDataCommunicator().getKeyMapper().get(key)));
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
        setSortOrder(sortOrderBuilder.build(), true);
    }

    private void setSortOrder(List<GridSortOrder<T>> order,
            boolean userOriginated) {
        Objects.requireNonNull(order, "Sort order list cannot be null");

        // TODO: if !userOriginated update client sort indicators. Should be
        // implemented together with server side sorting (issue #2818).

        if (sortOrder.equals(order)) {
            return;
        }

        sortOrder.clear();
        if (order.isEmpty()) {
            // Grid is not sorted anymore.
            getDataCommunicator().setBackEndSorting(Collections.emptyList());
            getDataCommunicator().setInMemorySorting(null);
            fireEvent(new SortEvent<>(this, new ArrayList<>(sortOrder),
                    userOriginated));
            return;
        }
        sortOrder.addAll(order);
        sort(userOriginated);
    }

    private void sort(boolean userOriginated) {
        // Set sort orders
        // In-memory comparator
        getDataCommunicator().setInMemorySorting(createSortingComparator());

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
     * If <code>true</code>, the grid's height is defined by the number of its
     * rows. All items are fetched from the {@link DataProvider}, and the Grid
     * shows no vertical scroll bar.
     *
     * @param heightByRows
     *            <code>true</code> to make Grid compute its height by the
     *            number of rows, <code>false</code> for the default behavior
     */
    public void setHeightByRows(boolean heightByRows) {
        getElement().setProperty("heightByRows", heightByRows);
    }

    /**
     * Gets whether grid's height is defined by the number of its rows.
     *
     * @return <code>true</code> if Grid computes its height by the number of
     *         rows, <code>false</code> otherwise
     */
    @Synchronize("height-by-rows-changed")
    public boolean isHeightByRows() {
        return getElement().getProperty("heightByRows", false);
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

        return addDataGenerator((item, data) -> data.put(property,
                JsonSerializer.toJson(valueProvider.apply(item))));
    }

    @Override
    public Registration addDataGenerator(DataGenerator<T> dataGenerator) {
        return gridDataGenerator.addDataGenerator(dataGenerator);
    }

    @Override
    public void removeDataGenerator(DataGenerator<T> dataGenerator) {
        gridDataGenerator.removeDataGenerator(dataGenerator);
    }

    protected static int compareMaybeComparables(Object a, Object b) {
        if (hasCommonComparableBaseType(a, b)) {
            return compareComparables(a, b);
        }
        return compareComparables(Objects.toString(a, ""),
                Objects.toString(b, ""));
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
}

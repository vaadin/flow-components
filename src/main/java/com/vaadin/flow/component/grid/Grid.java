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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.ClientDelegate;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.data.binder.BeanPropertySet;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.event.SortEvent.SortNotifier;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.ComponentDataGenerator;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataGenerators;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.selection.SelectionModel.Single;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.selection.SingleSelectionListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.HtmlUtils;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.renderer.ComponentTemplateRenderer;
import com.vaadin.flow.renderer.TemplateRenderer;
import com.vaadin.flow.shared.Registration;

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
@HtmlImport("frontend://flow-grid-component-renderer.html")
@JavaScript("frontend://gridConnector.js")
public class Grid<T> extends Component implements HasDataProvider<T>, HasStyle,
        HasSize, Focusable<Grid<T>>, SortNotifier<Grid<T>, GridSortOrder<T>> {

    private static final String GRID_COMPONENT_RENDERER_TAG = "flow-grid-component-renderer";

    private final class UpdateQueue implements Update {
        private List<Runnable> queue = new ArrayList<>();

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
            enqueue("$connector.confirm", updateId);
            queue.forEach(Runnable::run);
            queue.clear();
        }

        private void enqueue(String name, Serializable... arguments) {
            queue.add(() -> getElement().callFunction(name, arguments));
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
                    protected void fireSelectionEvent(SelectionEvent<T> event) {
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
                    protected void fireSelectionEvent(SelectionEvent<T> event) {
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

        private String rawHeaderTemplate;

        private Registration columnDataGeneratorRegistration;
        private Registration componentDataGeneratorRegistration;

        /**
         * Constructs a new Column for use inside a Grid.
         *
         * @param grid
         *            the grid this column is attached to
         * @param columnId
         *            unique identifier of this column
         * @param renderer
         *            the renderer to use in this column
         */
        public Column(Grid<T> grid, String columnId,
                TemplateRenderer<T> renderer) {
            super(grid);
            this.columnInternalId = columnId;

            comparator = (a, b) -> 0;

            HasDataGenerators<T> gridDataGenerator = grid.getDataGenerator();
            String template;
            if (renderer instanceof ComponentTemplateRenderer) {
                ComponentTemplateRenderer<? extends Component, T> componentRenderer = (ComponentTemplateRenderer<? extends Component, T>) renderer;
                DataGenerator<T> componentDataGenerator = grid
                        .setupItemComponentRenderer(this, columnId,
                                componentRenderer);
                columnDataGeneratorRegistration = gridDataGenerator
                        .addDataGenerator(componentDataGenerator);
                template = componentRenderer
                        .getTemplate(GRID_COMPONENT_RENDERER_TAG);
            } else {
                template = renderer.getTemplate();
            }

            Element contentTemplate = new Element("template")
                    .setProperty("innerHTML", template);

            getElement().appendChild(contentTemplate);

            DataGenerator<T> columnDataGenerator = GridTemplateRendererUtil
                    .setupTemplateRenderer((TemplateRenderer) renderer,
                            contentTemplate, getElement(),
                            key -> getGrid().getDataCommunicator()
                                    .getKeyMapper().get(key));

            columnDataGeneratorRegistration = gridDataGenerator
                    .addDataGenerator(columnDataGenerator);
        }

        protected void destroyDataGenerators() {
            if (columnDataGeneratorRegistration != null) {
                columnDataGeneratorRegistration.remove();
                columnDataGeneratorRegistration = null;
            }
            if (componentDataGeneratorRegistration != null) {
                componentDataGeneratorRegistration.remove();
                componentDataGeneratorRegistration = null;
            }
        }

        protected String getInternalId() {
            return columnInternalId;
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
            setComparator(Comparator.comparing(keyExtractor));
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

            if (headerTemplate != null) {
                headerTemplate.setProperty("innerHTML",
                        sortable ? addGridSorter(rawHeaderTemplate)
                                : rawHeaderTemplate);
            }
            return this;
        }

        public boolean isSortable() {
            return sortingEnabled;
        }

        @Override
        protected String getHeaderRendererTemplate(
                TemplateRenderer<?> renderer) {
            String template = super.getHeaderRendererTemplate(renderer);
            rawHeaderTemplate = template;
            if (isSortable()) {
                template = addGridSorter(template);
            }
            return template;
        }

        private String addGridSorter(String template) {
            String escapedColumnId = HtmlUtils.escape(columnInternalId);
            return String.format(
                    "<vaadin-grid-sorter path='%s'>%s</vaadin-grid-sorter>",
                    escapedColumnId, template);
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
            getGrid().getDataGenerator().addDataGenerator(this);
        }

        /**
         * Remove this extension from its target.
         */
        protected void remove() {
            getGrid().getDataGenerator().removeDataGenerator(this);
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

        private final Set<T> detailsVisible = new HashSet<>();

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
                refresh = detailsVisible.add(item);
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
            if (isDetailsVisible(item)) {
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

    private final ArrayUpdater arrayUpdater = UpdateQueue::new;

    private final CompositeDataGenerator<T> gridDataGenerator = new CompositeDataGenerator<>();
    private final DataCommunicator<T> dataCommunicator = new DataCommunicator<>(
            gridDataGenerator, arrayUpdater,
            data -> getElement().callFunction("$connector.updateData", data),
            getElement().getNode());

    private int nextColumnId = 0;

    private GridSelectionModel<T> selectionModel;

    private final DetailsManager detailsManager = new DetailsManager(this);
    private Element detailsTemplate;
    private boolean detailsVisibleOnClick = true;

    private Map<String, Column<T>> idToColumnMap = new HashMap<>();
    private Map<String, Column<T>> keyToColumnMap = new HashMap<>();

    private final List<GridSortOrder<T>> sortOrder = new ArrayList<>();

    private PropertySet<T> propertySet;

    private DataGenerator<T> itemDetailsDataGenerator;

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
        setPageSize(pageSize);
        setSelectionModel(SelectionMode.SINGLE.createModel(this),
                SelectionMode.SINGLE);
        getElement().addSynchronizedProperty("size");

        getElement().getNode()
                .runWhenAttached(ui -> ui.getPage().executeJavaScript(
                        "window.gridConnector.initLazy($0)", getElement()));
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
     */
    public Grid(Class<T> beanType) {
        this();
        Objects.requireNonNull(beanType, "Bean type can't be null");
        propertySet = BeanPropertySet.get(beanType);
        propertySet.getProperties()
                .filter(property -> !property.isSubProperty())
                .forEach(this::addColumn);
    }

    /**
     * Adds a new text column to this {@link Grid} with a value provider. The
     * value is converted to a JSON value by using
     * {@link JsonSerializer#toJson(Object)}.
     *
     * @param valueProvider
     *            the value provider
     * @return the created column
     */
    public Column<T> addColumn(ValueProvider<T, ?> valueProvider) {
        String columnId = createColumnId(false);
        return addColumn(TemplateRenderer.<T> of("[[item." + columnId + "]]")
                .withProperty(columnId, valueProvider));
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
     * Adds a new text column to this {@link Grid} with a template renderer. The
     * values inside the renderer are converted to JSON values by using
     * {@link JsonSerializer#toJson(Object)}.
     *
     * @param renderer
     *            the renderer used to create the grid cell structure
     * @return the created column
     *
     * @see TemplateRenderer#of(String)
     */
    public Column<T> addColumn(TemplateRenderer<T> renderer) {
        String columnId = createColumnId(true);

        getDataCommunicator().reset();

        Column<T> column = new Column<>(this, columnId, renderer);
        idToColumnMap.put(columnId, column);
        getElement().appendChild(column.getElement());

        return column;
    }

    /**
     * Adds a new text column to this {@link Grid} with a template renderer and
     * sorting properties. The values inside the renderer are converted to JSON
     * values by using {@link JsonSerializer#toJson(Object)}.
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
    public Column<T> addColumn(TemplateRenderer<T> renderer,
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
                item -> String.valueOf(property.getGetter().apply(item)))
                        .setHeader(property.getCaption());
        try {
            return column.setKey(property.getFullName());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Multiple columns for the same property: "
                            + property.getFullName());
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
    protected void setColumnKey(String key, Column column) {
        if (keyToColumnMap.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Duplicate key for columns: " + key);
        }
        keyToColumnMap.put(key, column);
    }

    private String createColumnId(boolean increment) {
        int id = nextColumnId;
        if (increment) {
            nextColumnId++;
        }
        return "col" + id;
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider, "data provider cannot be null");
        getDataCommunicator().setDataProvider(dataProvider, null);
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
     * Gets the current page size.
     *
     * @return the current page size
     */
    public int getPageSize() {
        return getElement().getProperty("pageSize", 50);
    }

    /**
     * Sets the page size.
     * <p>
     * This method is private at the moment because the Grid doesn't support
     * changing the pageSize after the initial load.
     *
     * @param pageSize
     *            the maximum number of items sent per request. Should be
     *            greater than zero
     */
    private void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException(
                    "The pageSize should be greater than zero. Was "
                            + pageSize);
        }
        getElement().setProperty("pageSize", pageSize);

        // resets the size of the Grid to avoid
        // https://github.com/vaadin/vaadin-grid/issues/1200
        getElement().setProperty("size", 0);

        getDataCommunicator().setRequestedRange(0, pageSize);
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
        getElement().callFunction("$connector.setSelectionMode",
                selectionMode.name());

        if (selectionMode.equals(SelectionMode.MULTI)) {
            getElement().addSynchronizedProperty("selectAll");
            getElement().addSynchronizedProperty("deselectAll");
        } else {
            getElement().removeSynchronizedProperty("selectAll");
            getElement().removeSynchronizedProperty("deselectAll");
        }
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
    public Registration addSelectionListener(SelectionListener<T> listener) {
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
    public void setItemDetailsRenderer(TemplateRenderer<T> renderer) {
        if (detailsTemplate != null) {
            getElement().removeChild(detailsTemplate);
        }
        detailsManager.destroyAllData();
        if (renderer == null) {
            return;
        }

        String template;
        DataGenerator<T> extraGenerator = null;
        if (renderer instanceof ComponentTemplateRenderer) {
            ComponentTemplateRenderer<? extends Component, T> componentRenderer = (ComponentTemplateRenderer<? extends Component, T>) renderer;
            extraGenerator = setupItemComponentRenderer(this, null,
                    componentRenderer);
            template = componentRenderer
                    .getTemplate(GRID_COMPONENT_RENDERER_TAG);
        } else {
            template = renderer.getTemplate();
        }

        Element newDetailsTemplate = new Element("template")
                .setAttribute("class", "row-details")
                .setProperty("innerHTML", template);

        detailsTemplate = newDetailsTemplate;
        getElement().appendChild(detailsTemplate);

        CompositeDataGenerator<T> detailsGenerator = GridTemplateRendererUtil
                .setupTemplateRenderer(renderer, detailsTemplate, getElement(),
                        key -> getDataCommunicator().getKeyMapper().get(key));
        if (extraGenerator != null) {
            detailsGenerator.addDataGenerator(extraGenerator);
        }
        itemDetailsDataGenerator = detailsGenerator;
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
     * Merges two or more columns into a {@link ColumnGroup}.
     *
     * @param firstColumn
     *            the first column to merge
     * @param secondColumn
     *            the second column to merge
     * @param additionalColumns
     *            optional additional columns to merge
     * @return the column group that contains the merged columns
     */
    public ColumnGroup mergeColumns(ColumnBase<?> firstColumn,
            ColumnBase<?> secondColumn, ColumnBase<?>... additionalColumns) {
        List<ColumnBase<?>> toMerge = new ArrayList<>();
        toMerge.add(firstColumn);
        toMerge.add(secondColumn);
        toMerge.addAll(Arrays.asList(additionalColumns));
        return mergeColumns(toMerge);
    }

    /**
     * Merges two or more columns into a {@link ColumnGroup}.
     *
     * @param columnsToMerge
     *            the columns to merge, not {@code null} and size must be
     *            greater than 1
     * @return the column group that contains the merged columns
     */
    public ColumnGroup mergeColumns(Collection<ColumnBase<?>> columnsToMerge) {
        List<ColumnBase<?>> topLevelColumns = getTopLevelColumns();

        Objects.requireNonNull(columnsToMerge,
                "Columns to merge cannot be null");
        if (columnsToMerge.size() < 2) {
            throw new IllegalArgumentException(
                    "Cannot merge less than two columns");
        }
        if (columnsToMerge.contains(null)) {
            throw new NullPointerException(
                    "Columns to merge cannot contain null values");
        }
        if (columnsToMerge.stream()
                .anyMatch(column -> !topLevelColumns.contains(column))) {
            throw new IllegalArgumentException(
                    "Cannot merge a column that already belongs to a column group");
        }

        int insertIndex = topLevelColumns
                .indexOf(columnsToMerge.iterator().next());

        columnsToMerge.forEach(column -> {
            getElement().removeChild(column.getElement());
        });

        ColumnGroup columnGroup = new ColumnGroup(this, columnsToMerge);
        getElement().insertChild(insertIndex, columnGroup.getElement());

        return columnGroup;
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
     * @see Column#setKey()
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
        Element parent = column.getElement().getParent();
        parent.removeChild(column.getElement());
        if (!parent.equals(getElement())) {
            removeEmptyColumnGroups(parent);
        }
    }

    private void removeEmptyColumnGroups(Element columnGroup) {
        Element parent = columnGroup.getParent();
        if (columnGroup.getChildCount() == 0) {
            parent.removeChild(columnGroup);
            if (!parent.equals(getElement())) {
                removeEmptyColumnGroups(parent);
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
     * @see #setItemDetailsRenderer(TemplateRenderer)
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
     * @see #setItemDetailsRenderer(TemplateRenderer)
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
        getElement().setProperty("multiSort", multiSort);
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
        return getElement().getProperty("multiSort", false);
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

    @ClientDelegate
    private void select(int key) {
        getSelectionModel().selectFromClient(findByKey(key));
    }

    @ClientDelegate
    private void deselect(int key) {
        getSelectionModel().deselectFromClient(findByKey(key));
    }

    private T findByKey(int key) {
        T item = getDataCommunicator().getKeyMapper().get(String.valueOf(key));
        if (item == null) {
            throw new IllegalStateException("Unkonwn key: " + key);
        }
        return item;
    }

    @ClientDelegate
    private void confirmUpdate(int id) {
        getDataCommunicator().confirmUpdate(id);
    }

    @ClientDelegate
    private void setRequestedRange(int start, int length) {
        getDataCommunicator().setRequestedRange(start, length);
    }

    @ClientDelegate
    private void setDetailsVisible(String key) {
        if (key == null) {
            detailsManager.setDetailsVisibleFromClient(Collections.emptySet());
        } else {
            detailsManager.setDetailsVisibleFromClient(Collections
                    .singleton(getDataCommunicator().getKeyMapper().get(key)));
        }
    }

    @ClientDelegate
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

    private DataGenerator<T> setupItemComponentRenderer(Component owner,
            String ownerId,
            ComponentTemplateRenderer<? extends Component, T> componentRenderer) {

        Element container = new Element("div", false);
        owner.getElement().appendVirtualChild(container);

        String appId = UI.getCurrent().getInternals().getAppId();

        componentRenderer.setTemplateAttribute("appid", appId);
        String nodeIdPropertyName = ownerId == null ? "nodeId" : ownerId;
        componentRenderer.setTemplateAttribute("nodeid",
                "[[item." + nodeIdPropertyName + "]]");

        return new ComponentDataGenerator<>(componentRenderer, container,
                nodeIdPropertyName, getDataCommunicator().getKeyMapper());
    }

    /*
     * This method is not private because AbstractColumn uses it.
     */
    CompositeDataGenerator<T> getDataGenerator() {
        return gridDataGenerator;
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
}

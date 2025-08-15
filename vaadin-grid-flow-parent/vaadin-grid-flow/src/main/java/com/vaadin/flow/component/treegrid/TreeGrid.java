/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridArrayUpdater;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.internal.AllowInert;
import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HasHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy2.HierarchicalDataCommunicator;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Tree Grid is a component for displaying hierarchical tabular data grouped
 * into expandable and collapsible nodes. Tree Grid is an extension of the Grid
 * component and all Grid’s features are available in Tree Grid as well.
 *
 * @param <T>
 *            the grid bean type
 * @author Vaadin Ltd
 */
@JsModule("@vaadin/grid/src/vaadin-grid-tree-toggle.js")
@JsModule("./treeGridConnector.ts")
public class TreeGrid<T> extends Grid<T>
        implements HasHierarchicalDataProvider<T> {
    /**
     * Creates a new {@code TreeGrid} without support for creating columns based
     * on property names. Use an alternative constructor, such as
     * {@link TreeGrid#TreeGrid(Class)}, to create a {@code TreeGrid} that
     * automatically sets up columns based on the type of presented data.
     */
    public TreeGrid() {
        this(50, new TreeDataCommunicatorBuilder<T>());
    }

    /**
     * Creates a new {@code TreeGrid} without support for creating columns based
     * on property names. Use an alternative constructor, such as
     * {@link TreeGrid#TreeGrid(Class)}, to create a {@code TreeGrid} that
     * automatically sets up columns based on the type of presented data.
     *
     * @param pageSize
     *            the page size. Must be greater than zero.
     * @param dataCommunicatorBuilder
     *            Builder for {@link DataCommunicator} implementation this Grid
     *            uses to handle all data communication.
     */
    protected TreeGrid(int pageSize,
            DataCommunicatorBuilder<T, GridArrayUpdater> dataCommunicatorBuilder) {
        super(pageSize, dataCommunicatorBuilder);

        setUniqueKeyProperty("key");
        addTreeDataGenerator();
    }

    @Override
    protected void initConnector() {
        getUI().orElseThrow(() -> new IllegalStateException(
                "Connector can only be initialized for an attached Grid"))
                .getPage()
                .executeJs("window.Vaadin.Flow.treeGridConnector.initLazy($0)",
                        getElement());
    }

    /**
     * Adds a data generator that produces a value for the <vaadin-grid>'s
     * itemHasChildrenPath property
     */
    private void addTreeDataGenerator() {
        addDataGenerator((T item, JsonObject jsonObject) -> {
            if (getDataCommunicator().hasChildren(item)) {
                jsonObject.put("children", true);
            }

            if (getDataCommunicator().isExpanded(item)) {
                jsonObject.put("expanded", true);
            }

            jsonObject.put("level", getDataCommunicator().getDepth(item));
        });
    }

    /**
     * Creates a new {@code TreeGrid} with an initial set of columns for each of
     * the bean's properties. The property-values of the bean will be converted
     * to Strings. Full names of the properties will be used as the
     * {@link Column#setKey(String) column keys} and the property captions will
     * be used as the {@link Column#setHeader(String) column headers}.
     *
     * @param beanType
     *            the bean type to use, not {@code null}
     */
    public TreeGrid(Class<T> beanType) {
        this(beanType, true);
    }

    /**
     * Creates a new {@code TreeGrid} with an initial set of columns for each of
     * the bean's properties. The property-values of the bean will be converted
     * to Strings. Full names of the properties will be used as the
     * {@link Column#setKey(String) column keys} and the property captions will
     * be used as the {@link Column#setHeader(String) column headers}.
     * <p>
     * When autoCreateColumns is <code>true</code>, only the direct properties
     * of the bean are included, and they will be in alphabetical order. Use
     * {@link #setColumns(String...)} to define which properties to include and
     * in which order. You can also add a column for an individual property with
     * {@link #addColumn(String)}. Both of these methods support also
     * sub-properties with dot-notation, e.g.
     * <code>"property.nestedProperty"</code>.
     *
     * @param beanType
     *            the bean type to use, not <code>null</code>
     * @param autoCreateColumns
     *            when <code>true</code>, columns are created automatically for
     *            the properties of the beanType
     */
    public TreeGrid(Class<T> beanType, boolean autoCreateColumns) {
        this(beanType, new TreeDataCommunicatorBuilder<>(), autoCreateColumns);
    }

    /**
     * Creates a new {@code TreeGrid} with an initial set of columns for each of
     * the bean's properties. The property-values of the bean will be converted
     * to Strings. Full names of the properties will be used as the
     * {@link Column#setKey(String) column keys} and the property captions will
     * be used as the {@link Column#setHeader(String) column headers}.
     *
     * @param beanType
     *            the bean type to use, not {@code null}
     * @param dataCommunicatorBuilder
     *            Builder for {@link DataCommunicator} implementation this Grid
     *            uses to handle all data communication.
     */
    protected TreeGrid(Class<T> beanType,
            DataCommunicatorBuilder<T, GridArrayUpdater> dataCommunicatorBuilder) {
        this(beanType, dataCommunicatorBuilder, true);
    }

    private TreeGrid(Class<T> beanType,
            DataCommunicatorBuilder<T, GridArrayUpdater> dataCommunicatorBuilder,
            boolean autoCreateColumns) {
        super(beanType, dataCommunicatorBuilder, autoCreateColumns);

        setUniqueKeyProperty("key");
        addTreeDataGenerator();
    }

    /**
     * Creates a new {@code TreeGrid} using the given
     * {@code HierarchicalDataProvider}, without support for creating columns
     * based on property names. Use an alternative constructor, such as
     * {@link TreeGrid#TreeGrid(Class)}, to create a {@code TreeGrid} that
     * automatically sets up columns based on the type of presented data.
     *
     * @param dataProvider
     *            the data provider, not {@code null}
     */
    public TreeGrid(HierarchicalDataProvider<T, ?> dataProvider) {
        this();
        setDataProvider(dataProvider);
    }

    private static class TreeGridDataCommunicator<T>
            extends HierarchicalDataCommunicator<T> {
        private Element element;

        public TreeGridDataCommunicator(Element element,
                CompositeDataGenerator<T> dataGenerator,
                ArrayUpdater arrayUpdater,
                SerializableConsumer<JsonArray> dataUpdater,
                SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
            super(dataGenerator, arrayUpdater, dataUpdater, element.getNode(),
                    uniqueKeyProviderSupplier);
            this.element = element;
        }

        @Override
        public void reset() {
            super.reset();
            if (element != null) {
                element.callJsFunction("$connector.reset");
            }
        }

        @Override
        protected List<T> preloadFlatRangeForward(int start, int length) {
            return super.preloadFlatRangeForward(start, length);
        }

        @Override
        protected List<T> preloadFlatRangeBackward(int start, int length) {
            return super.preloadFlatRangeBackward(start, length);
        }

        @Override
        protected int resolveIndexPath(int... path) {
            return super.resolveIndexPath(path);
        }
    }

    private static class TreeDataCommunicatorBuilder<T>
            extends DataCommunicatorBuilder<T, GridArrayUpdater> {

        @Override
        protected DataCommunicator<T> build(Element element,
                CompositeDataGenerator<T> dataGenerator,
                GridArrayUpdater arrayUpdater,
                SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
            return new TreeGridDataCommunicator<>(element, dataGenerator,
                    arrayUpdater, null, uniqueKeyProviderSupplier);
        }
    }

    /**
     * Sets property name and value provider for unique key in row's generated
     * JSON.
     * <p>
     * Default property name is 'key' and value is generated by bean's hashCode
     * method.
     * </p>
     *
     * @param propertyName
     *            Property name in JSON data
     * @param uniqueKeyProvider
     *            Value provider for the target property in JSON data
     */
    public void setUniqueKeyDataGenerator(String propertyName,
            ValueProvider<T, String> uniqueKeyProvider) {
        setUniqueKeyProperty(propertyName);
        setUniqueKeyProvider(uniqueKeyProvider);

        getDataProvider().refreshAll();
    }

    /**
     * Adds an ExpandEvent listener to this TreeGrid.
     *
     * @param listener
     *            the listener to add
     * @return a registration for the listener
     * @see ExpandEvent
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addExpandListener(
            ComponentEventListener<ExpandEvent<T, TreeGrid<T>>> listener) {
        return ComponentUtil.addListener(this, ExpandEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Adds a CollapseEvent listener to this TreeGrid.
     *
     * @param listener
     *            the listener to add
     * @return a registration for the listener
     * @see CollapseEvent
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addCollapseListener(
            ComponentEventListener<CollapseEvent<T, TreeGrid<T>>> listener) {
        return ComponentUtil.addListener(this, CollapseEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Tree grid only supports hierarchical data providers. Use
     * {@link #setDataProvider(HierarchicalDataProvider)} instead.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @param dataProvider
     *            the data provider
     * @deprecated use {@link #setDataProvider(HierarchicalDataProvider)},
     *             {@link #setItems(Collection, ValueProvider)},
     *             {@link #setItems(Stream, ValueProvider)} or
     *             {@link #setTreeData(TreeData)} instead.
     */
    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        if (dataProvider instanceof HierarchicalDataProvider) {
            this.setDataProvider((HierarchicalDataProvider) dataProvider);
        } else {
            throw new IllegalArgumentException(
                    "TreeGrid only accepts hierarchical data providers. "
                            + "An example of interface to be used: HierarchicalDataProvider");
        }
    }

    @Override
    public void setDataProvider(
            HierarchicalDataProvider<T, ?> hierarchicalDataProvider) {
        super.setDataProvider(hierarchicalDataProvider);
    }

    /**
     * Tree grid does not support data views. Use
     * {@link #setDataProvider(HierarchicalDataProvider)} instead.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @param dataProvider
     *            the data provider
     * @return the data view
     * @deprecated use {@link #setDataProvider(HierarchicalDataProvider)},
     *             {@link #setItems(Collection, ValueProvider)},
     *             {@link #setItems(Stream, ValueProvider)} or
     *             {@link #setTreeData(TreeData)} instead.
     */
    @Deprecated
    @Override
    public GridLazyDataView<T> setItems(
            BackEndDataProvider<T, Void> dataProvider) {
        throw new UnsupportedOperationException(
                "TreeGrid only accepts hierarchical data providers. "
                        + "Use another setDataProvider/setItems method instead with hierarchical data."
                        + "An example of interface to be used: HierarchicalDataProvider");
    }

    /**
     * Tree grid supports only hierarchical data so use another method instead.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @param fetchCallback
     *            the fetch callback
     * @return the data view
     * @deprecated use {@link #setDataProvider(HierarchicalDataProvider)},
     *             {@link #setItems(Collection, ValueProvider)},
     *             {@link #setItems(Stream, ValueProvider)} or
     *             {@link #setTreeData(TreeData)} instead.
     */
    @Deprecated
    @Override
    public GridLazyDataView<T> setItems(
            CallbackDataProvider.FetchCallback<T, Void> fetchCallback) {
        throw new UnsupportedOperationException(
                "TreeGrid only accepts hierarchical data providers. "
                        + "Use another setDataProvider/setItems method instead with hierarchical data."
                        + "An example of interface to be used: HierarchicalDataProvider");
    }

    /**
     * Tree grid supports only hierarchical data providers so use another method
     * instead.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @param dataProvider
     *            the data provider
     * @return the data view
     * @deprecated use {@link #setDataProvider(HierarchicalDataProvider)},
     *             {@link #setItems(Collection, ValueProvider)},
     *             {@link #setItems(Stream, ValueProvider)} or
     *             {@link #setTreeData(TreeData)} instead.
     */
    @Deprecated
    @Override
    public GridListDataView<T> setItems(ListDataProvider<T> dataProvider) {
        throw new UnsupportedOperationException(
                "TreeGrid only accepts hierarchical data providers. "
                        + "Use another setDataProvider/setItems method instead with hierarchical data."
                        + "An example of interface to be used: HierarchicalDataProvider");
    }

    /**
     * Tree grid supports only hierarchical data so use another method instead.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @param items
     *            the items to display, not {@code null}
     * @return the data view
     * @deprecated use {@link #setDataProvider(HierarchicalDataProvider)},
     *             {@link #setItems(Collection, ValueProvider)},
     *             {@link #setItems(Stream, ValueProvider)} or
     *             {@link #setTreeData(TreeData)} instead.
     */
    @Deprecated
    @Override
    public GridListDataView<T> setItems(T... items) {
        throw new UnsupportedOperationException(
                "TreeGrid only accepts hierarchical data providers. "
                        + "Use another setDataProvider/setItems method instead with hierarchical data."
                        + "An example of interface to be used: HierarchicalDataProvider");
    }

    /**
     * Tree grid supports only hierarchical data, so use another method instead.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @param items
     *            the items to display, not {@code null}
     * @return the data view
     * @deprecated use {@link #setDataProvider(HierarchicalDataProvider)},
     *             {@link #setItems(Collection, ValueProvider)},
     *             {@link #setItems(Stream, ValueProvider)} or
     *             {@link #setTreeData(TreeData)} instead.
     */
    @Deprecated
    @Override
    public GridListDataView<T> setItems(Collection<T> items) {
        throw new UnsupportedOperationException(
                "TreeGrid only accepts hierarchical data providers. "
                        + "Use another setDataProvider/setItems method instead with hierarchical data."
                        + "An example of interface to be used: HierarchicalDataProvider");
    }

    /**
     * Tree grid does not support list data view.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @return exception is thrown
     * @deprecated not supported
     */
    @Deprecated
    @Override
    public GridListDataView<T> getListDataView() {
        throw new UnsupportedOperationException(
                "TreeGrid does not support list data view.");
    }

    /**
     * Tree grid does not support list data view.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @return exception is thrown
     * @deprecated not supported
     */
    @Deprecated
    @Override
    public GridLazyDataView<T> getLazyDataView() {
        throw new UnsupportedOperationException(
                "TreeGrid does not support lazy data view.");
    }

    /**
     * Tree grid does not support list data view.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @return exception is thrown
     * @deprecated not supported
     */
    @Deprecated
    @Override
    public GridDataView<T> getGenericDataView() {
        throw new UnsupportedOperationException(
                "TreeGrid does not support generic data view.");
    }

    /**
     * Adds a new Hierarchy column to this {@link Grid} with a value provider.
     * The value is converted to String when sent to the client by using
     * {@link String#valueOf(Object)}.
     * <p>
     * Hierarchy column is rendered by using 'vaadin-grid-tree-toggle' web
     * component.
     *
     * @param valueProvider
     *            the value provider
     * @return the created hierarchy column
     */
    public Column<T> addHierarchyColumn(ValueProvider<T, ?> valueProvider) {
        Column<T> column = addColumn(LitRenderer.<T> of(
                "<vaadin-grid-tree-toggle @click=${onClick} .leaf=${!model.hasChildren} .expanded=${live(model.expanded)} .level=${model.level}>"
                        + "${item.name}</vaadin-grid-tree-toggle>")
                .withProperty("name", value -> {
                    Object name = valueProvider.apply(value);
                    return name == null ? "" : String.valueOf(name);
                }).withFunction("onClick", item -> {
                    if (getDataCommunicator().hasChildren(item)) {
                        if (isExpanded(item)) {
                            collapse(List.of(item), true);
                        } else {
                            expand(List.of(item), true);
                        }
                    }
                }));

        final SerializableComparator<T> comparator = (a,
                b) -> compareMaybeComparables(valueProvider.apply(a),
                        valueProvider.apply(b));
        column.setComparator(comparator);

        return column;
    }

    /**
     * Adds a new Hierarchy column that shows components.
     * <p>
     * <em>NOTE:</em> Using {@link ComponentRenderer} is not as efficient as the
     * built in renderers.
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
    public <V extends Component> Column<T> addComponentHierarchyColumn(
            ValueProvider<T, V> componentProvider) {
        return addColumn(new HierarchyColumnComponentRenderer<>(
                componentProvider, this));
    }

    /**
     * <strong>Note:</strong> This method can only be used for a TreeGrid
     * created from a bean type with {@link #TreeGrid(Class)}.
     * <p>
     * Resets columns and their order based on bean properties.
     * <p>
     * This is a shortcut for removing all columns and then calling
     * {@link #addColumn(String)} for each property except hierarchy column in
     * the bean and {@link #addHierarchyColumn(String)} for the given
     * propertyName.
     * <p>
     * Previous column order is preserved.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * Note that this also resets the headers and footers.
     *
     * @param propertyName
     *            a target hierarchy column property name
     * @return the created hierarchy column
     */
    public Column<T> setHierarchyColumn(String propertyName) {
        return setHierarchyColumn(propertyName, null);
    }

    /**
     * <strong>Note:</strong> This method can only be used for a TreeGrid
     * created from a bean type with {@link #TreeGrid(Class)}.
     * <p>
     * Resets columns and their order based on bean properties.
     * <p>
     * This is a shortcut for removing all columns and then calling
     * {@link #addColumn(String)} for each property except hierarchy column in
     * the bean and {@link #addHierarchyColumn(String)} or
     * {@link #addHierarchyColumn(ValueProvider)} for the given propertyName.
     * <p>
     * Previous column order is preserved.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * Note that this also resets the headers and footers.
     *
     * @param propertyName
     *            a target hierarchy column property name
     * @param valueProvider
     *            optional value provider
     * @return the created hierarchy column
     */
    public Column<T> setHierarchyColumn(String propertyName,
            ValueProvider<T, ?> valueProvider) {
        List<String> currentPropertyList = getColumns().stream()
                .map(Column::getKey).filter(Objects::nonNull)
                .collect(Collectors.toList());
        resetColumns(propertyName, valueProvider, currentPropertyList);
        return getColumnByKey(propertyName);
    }

    /**
     * <strong>Note:</strong> This method can only be used for a TreeGrid
     * created from a bean type with {@link #TreeGrid(Class)}.
     * <p>
     * Sets the columns and their order based on the given properties.
     * <p>
     * This is a shortcut for removing all columns and then calling
     * {@link #addColumn(String)} for each property except hierarchy property in
     * the bean and {@link #addHierarchyColumn(String)} for the given
     * hierarchyPropertyName.
     * <p>
     * You can add columns for nested properties with dot notation, eg.
     * <code>"property.nestedProperty"</code>
     * <p>
     * Note that this also resets the headers and footers.
     *
     * @param hierarchyPropertyName
     *            a target hierarchy column property name
     * @param valueProvider
     *            optional value provider
     * @param propertyNames
     *            set of properties to create columns for. Including given
     *            hierarchyPropertyName
     * @return the hierarchy column
     */
    public Column<T> setColumns(String hierarchyPropertyName,
            ValueProvider<T, ?> valueProvider,
            Collection<String> propertyNames) {
        if (getPropertySet() == null) {
            throw new UnsupportedOperationException(
                    "This method can't be used for a Grid that isn't constructed from a bean type. "
                            + "To construct Grid from a bean type, please provide a beanType argument"
                            + "to the constructor: Grid<Person> grid = new Grid<>(Person.class)");
        }
        resetColumns(hierarchyPropertyName, valueProvider, propertyNames);
        return getColumnByKey(hierarchyPropertyName);
    }

    private void resetColumns(String hierarchyPropertyName,
            ValueProvider<T, ?> valueProvider,
            Collection<String> propertyList) {
        getColumns().forEach(this::removeColumn);
        propertyList.stream().distinct().forEach(
                key -> addColumn(key, hierarchyPropertyName, valueProvider));
    }

    private void addColumn(String key, String hierarchyPropertyName,
            ValueProvider<T, ?> valueProvider) {
        if (key.equals(hierarchyPropertyName)) {
            addHierarchyColumn(hierarchyPropertyName, valueProvider);
        } else {
            addColumn(key);
        }
    }

    private void addHierarchyColumn(String hierarchyPropertyName,
            ValueProvider<T, ?> valueProvider) {
        if (valueProvider != null) {
            addHierarchyColumn(valueProvider).setKey(hierarchyPropertyName);
        } else {
            addHierarchyColumn(hierarchyPropertyName);
        }
    }

    private Column<T> addHierarchyColumn(String propertyName) {
        if (getPropertySet() == null) {
            throw new UnsupportedOperationException(
                    "This method can't be used for a Grid that isn't constructed from a bean type. "
                            + "To construct Grid from a bean type, please provide a beanType argument"
                            + "to the constructor: Grid<Person> grid = new Grid<>(Person.class)");
        }
        Objects.requireNonNull(propertyName,
                "Hierarchy Property name can't be null");

        PropertyDefinition<T, ?> property;
        try {
            property = getPropertySet().getProperty(propertyName).get();
        } catch (NoSuchElementException | IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "There is no such hierarchy property name in the beanType used "
                            + "for construction of the grid:"
                            + "Trying to get '" + propertyName + "' from '"
                            + getPropertySet() + "'");
        }
        return addHierarchyColumn(property);
    }

    private Column<T> addHierarchyColumn(PropertyDefinition<T, ?> property) {
        Column<T> column = addHierarchyColumn(
                item -> String.valueOf(property.getGetter().apply(item)))
                .setHeader(property.getCaption());
        try {
            return column.setKey(property.getName());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Multiple columns for the same property: "
                            + property.getName());
        }
    }

    @ClientCallable(DisabledUpdateMode.ONLY_WHEN_ENABLED)
    private void updateExpandedState(String key, boolean expanded) {
        T item = getDataCommunicator().getKeyMapper().get(key);
        if (item != null) {
            if (expanded) {
                expand(Arrays.asList(item), true);
            } else {
                collapse(Arrays.asList(item), true);
            }
        }
    }

    /**
     * Expands the given items.
     * <p>
     * If an item is currently expanded, does nothing. If an item does not have
     * any children, does nothing.
     *
     * @param items
     *            the items to expand
     */
    @SuppressWarnings("unchecked")
    public void expand(T... items) {
        expand(Arrays.asList(items));
    }

    /**
     * Expands the given items.
     * <p>
     * If an item is currently expanded, does nothing. If an item does not have
     * any children, does nothing.
     *
     * @param items
     *            the items to expand
     */
    public void expand(Collection<T> items) {
        expand(items, false);
    }

    /**
     * Expands the given items.
     *
     * @param items
     *            the items to expand
     * @param userOriginated
     *            {@code true} if a {@link ExpandEvent} triggered by this
     *            operation is user originated, {@code false} otherwise.
     */
    protected void expand(Collection<T> items, boolean userOriginated) {
        Collection<T> expandedItems = getDataCommunicator().expand(items);
        fireEvent(new ExpandEvent<T, TreeGrid<T>>(this, userOriginated,
                expandedItems));
    }

    /**
     * Expands the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code expandRecursively(items, 0)} expands only
     * the given items while {@code expandRecursively(items, 2)} expands the
     * given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for expanded nodes.
     *
     * @param items
     *            the items to expand recursively
     * @param depth
     *            the maximum depth of recursion
     * @since 8.4
     */
    public void expandRecursively(Stream<T> items, int depth) {
        expandRecursively(items.collect(Collectors.toList()), depth);
    }

    /**
     * Expands the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code expandRecursively(items, 0)} expands only
     * the given items while {@code expandRecursively(items, 2)} expands the
     * given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for expanded nodes.
     *
     * @param items
     *            the items to expand recursively
     * @param depth
     *            the maximum depth of recursion
     * @since 8.4
     */
    public void expandRecursively(Collection<T> items, int depth) {
        getDataCommunicator()
                .expand(getItemsWithChildrenRecursively(items, depth));
    }

    /**
     * Collapse the given items.
     * <p>
     * For items that are already collapsed, does nothing.
     *
     * @param items
     *            the collection of items to collapse
     */
    @SuppressWarnings("unchecked")
    public void collapse(T... items) {
        collapse(Arrays.asList(items));
    }

    /**
     * Collapse the given items.
     * <p>
     * For items that are already collapsed, does nothing.
     *
     * @param items
     *            the collection of items to collapse
     */
    public void collapse(Collection<T> items) {
        collapse(items, false);
    }

    /**
     * Collapse the given items.
     *
     * @param items
     *            the collection of items to collapse
     * @param userOriginated
     *            {@code true} if a {@link CollapseEvent} triggered by this
     *            operation is user originated, {@code false} otherwise.
     */
    protected void collapse(Collection<T> items, boolean userOriginated) {
        Collection<T> collapsedItems = getDataCommunicator().collapse(items);
        fireEvent(new CollapseEvent<T, TreeGrid<T>>(this, userOriginated,
                collapsedItems));
    }

    /**
     * Collapse the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code collapseRecursively(items, 0)} collapses
     * only the given items while {@code collapseRecursively(items, 2)}
     * collapses the given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for collapsed nodes.
     *
     * @param items
     *            the items to collapse recursively
     * @param depth
     *            the maximum depth of recursion
     * @since 8.4
     */
    public void collapseRecursively(Stream<T> items, int depth) {
        collapseRecursively(items.collect(Collectors.toList()), depth);
    }

    /**
     * Collapse the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code collapseRecursively(items, 0)} collapses
     * only the given items while {@code collapseRecursively(items, 2)}
     * collapses the given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for collapsed nodes.
     *
     * @param items
     *            the items to collapse recursively
     * @param depth
     *            the maximum depth of recursion
     * @since 8.4
     */
    public void collapseRecursively(Collection<T> items, int depth) {
        getDataCommunicator()
                .collapse(getItemsWithChildrenRecursively(items, depth));
    }

    /**
     * Gets given items and their children recursively until the given depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that
     * {@code getItemsWithChildrenRecursively(items, 0)} gets only the given
     * items while {@code getItemsWithChildrenRecursively(items, 2)} gets the
     * given items as well as their children and grandchildren.
     * </p>
     *
     * @param items
     *            the items to expand recursively
     * @param depth
     *            the maximum depth of recursion
     * @return collection of given items and their children recursively until
     *         the given depth
     */
    protected Collection<T> getItemsWithChildrenRecursively(Collection<T> items,
            int depth) {
        List<T> itemsWithChildren = new ArrayList<>();
        if (depth < 0) {
            return itemsWithChildren;
        }
        items.stream().filter(getDataCommunicator()::hasChildren)
                .forEach(item -> {
                    itemsWithChildren.add(item);
                    itemsWithChildren.addAll(
                            getItemsWithChildrenRecursively(getDataProvider()
                                    .fetchChildren(
                                            new HierarchicalQuery<>(null, item))
                                    .collect(Collectors.toList()), depth - 1));
                });
        return itemsWithChildren;
    }

    /**
     * Returns whether a given item is expanded or collapsed.
     *
     * @param item
     *            the item to check
     * @return true if the item is expanded, false if collapsed
     */
    public boolean isExpanded(T item) {
        return getDataCommunicator().isExpanded(item);
    }

    @Override
    public HierarchicalDataCommunicator<T> getDataCommunicator() {
        return (HierarchicalDataCommunicator<T>) super.getDataCommunicator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public HierarchicalDataProvider<T, SerializablePredicate<T>> getDataProvider() {
        if (!(super.getDataProvider() instanceof HierarchicalDataProvider)) {
            return null;
        }
        return (HierarchicalDataProvider<T, SerializablePredicate<T>>) super.getDataProvider();
    }

    /**
     * Scrolls to the index of an item in the root level of the tree. To scroll
     * to a nested item, use {@link #scrollToIndex(int...)}.
     * <p>
     * Scrolls so that the row is shown at the start of the visible area
     * whenever possible.
     * <p>
     * If the index parameter exceeds current item set size the grid will scroll
     * to the end.
     *
     * @param rowIndex
     *            zero based index of the item in the root level of the tree
     * @see TreeGrid#scrollToIndex(int...)
     */
    @Override
    public void scrollToIndex(int rowIndex) {
        getUI().ifPresent(
                ui -> ui.beforeClientResponse(this, ctx -> getElement()
                        .executeJs("this.scrollToIndex($0);", rowIndex)));
    }

    /**
     * Scrolls to a nested item within the tree.
     * <p>
     * The `indexes` parameter can be either a single number or multiple
     * numbers. The grid will first try to scroll to the item at the first index
     * in the root level of the tree. In case the item at the first index is
     * expanded, the grid will then try scroll to the item at the second index
     * within the children of the expanded first item, and so on. Each given
     * index points to a child of the item at the previous index.
     *
     * @param indexes
     *            zero based row indexes to scroll to
     * @see TreeGrid#scrollToIndex(int)
     */
    public void scrollToIndex(int... indexes) {
        if (indexes.length == 0) {
            throw new IllegalArgumentException(
                    "At least one index should be provided.");
        }
        String joinedIndexes = Arrays.stream(indexes).mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        getUI().ifPresent(ui -> ui.beforeClientResponse(this,
                ctx -> getElement().executeJs(
                        "this.scrollToIndex(" + joinedIndexes + ");")));
    }

    @Override
    public void scrollToEnd() {
        getUI().ifPresent(ui -> ui.beforeClientResponse(this,
                ctx -> getElement().executeJs(
                        "this.scrollToIndex(...Array(10).fill(-1))")));
    }

    /**
     * Sets the viewport range centered on the item specified by a hierarchical
     * index path e.g. { 0, 1, 1 }. The {@code padding} parameter specifies how
     * many items should be added on each side of the center item in the
     * resulting range.
     * <p>
     * This method has package-private visibility to allow testing.
     *
     * @param path
     *            the path to the item to use as the center of the viewport
     *            range
     * @param padding
     *            the number of items to add on each side of the center item
     */
    @AllowInert
    @ClientCallable(DisabledUpdateMode.ALWAYS)
    int setViewportRangeByIndexPath(int[] path, int padding) {
        var pageSize = getPageSize();
        var maxAllowedItems = 10 * Math.max(50, pageSize);
        if (maxAllowedItems < padding) {
            throw new IllegalArgumentException(String.format(
                    "Requested viewport size (%d items) "
                            + "exceeds security limit (%d items max). "
                            + "Consider reducing the grid height or increasing "
                            + "the page size to at least %d if it's a valid request.",
                    padding, maxAllowedItems, (int) Math.ceil(padding / 10.0)));
        }

        var dataCommunicator = (TreeGridDataCommunicator<T>) getDataCommunicator();

        // Resolve the flat index from the given index path
        var flatIndex = dataCommunicator.resolveIndexPath(path);

        // Preload items starting at the resolved flat index and moving
        // forward, from lower to higher indexes (in flat representation).
        // Page size is added to make sure we never preload fewer items than the
        // actual viewport range will need after its boundaries are
        // page-aligned.
        dataCommunicator.preloadFlatRangeForward(flatIndex, padding + pageSize);

        // Repeat the process backward to preload enough items behing the
        // resolved flat index. Adding the page size is essential. Without it,
        // the following call to dataCommunicator.setViewportRange will try to
        // load uncovered expanded items forward, shifting the range and causing
        // underscroll.
        dataCommunicator.preloadFlatRangeBackward(flatIndex,
                padding + pageSize);

        // Update the flat index after preloading, as it might have changed
        flatIndex = dataCommunicator.resolveIndexPath(path);

        // Calculate the viewport range, placing the flat index in the center
        // and adding padding around it.
        var startIndex = flatIndex - padding;
        var endIndex = flatIndex + padding;

        // Align the viewport range with page boundaries, as the grid connector
        // supports only page-aligned ranges, see $connector.clear, for example.
        var startPage = Math.max(0, startIndex / pageSize);
        var endPage = endIndex / pageSize;

        dataCommunicator.setViewportRange(startPage * pageSize,
                (endPage - startPage + 1) * pageSize);

        return flatIndex;
    }

    /**
     * TreeGrid does not support scrolling to a given item. Use
     * {@link #scrollToIndex(int...)} instead.
     * <p>
     * This method is inherited from Grid and has been marked as deprecated to
     * indicate that it is not supported. This method will throw an
     * {@link UnsupportedOperationException}.
     *
     * @param item
     *            the item to scroll to
     * @deprecated
     */
    @Deprecated
    @Override
    public void scrollToItem(T item) {
        throw new UnsupportedOperationException(
                "scrollToItem method is not supported in TreeGrid");
    }
}

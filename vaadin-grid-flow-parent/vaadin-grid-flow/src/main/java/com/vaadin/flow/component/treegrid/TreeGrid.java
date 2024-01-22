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
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridArrayUpdater;
import com.vaadin.flow.component.grid.GridArrayUpdater.UpdateQueueData;
import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.hierarchy.HasHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater.HierarchicalUpdate;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * A grid component for displaying hierarchical tabular data.
 *
 * @param <T>
 *            the grid bean type
 * @author Vaadin Ltd
 */
@JsModule("@vaadin/vaadin-grid/src/vaadin-grid-tree-toggle.js")
@HtmlImport("frontend://bower_components/vaadin-grid/src/vaadin-grid-tree-toggle.html")
public class TreeGrid<T> extends Grid<T>
        implements HasHierarchicalDataProvider<T> {

    private static final class TreeGridUpdateQueue extends UpdateQueue
            implements HierarchicalUpdate {

        private TreeGridUpdateQueue(UpdateQueueData data, int size) {
            super(data, size);
        }

        @Override
        public void set(int start, List<JsonValue> items, String parentKey) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()), parentKey);
        }

        @Override
        public void clear(int start, int length) {
            if (!getData().getHasExpandedItems().get()) {
                enqueue("$connector.clearExpanded");
            }
            super.clear(start, length);
        }

        @Override
        public void clear(int start, int length, String parentKey) {
            enqueue("$connector.clear", start, length, parentKey);
        }

        @Override
        public void commit(int updateId, String parentKey, int levelSize) {
            enqueue("$connector.confirmParent", updateId, parentKey, levelSize);
            commit();
        }
    }

    private class TreeGridArrayUpdaterImpl implements TreeGridArrayUpdater {
        private UpdateQueueData data;
        private SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory;

        public TreeGridArrayUpdaterImpl(
                SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory) {
            this.updateQueueFactory = updateQueueFactory;
        }

        @Override
        public TreeGridUpdateQueue startUpdate(int sizeChange) {
            return (TreeGridUpdateQueue) updateQueueFactory.apply(data,
                    sizeChange);
        }

        @Override
        public void initialize() {
            initConnector();
            updateSelectionModeOnClient();
            getDataCommunicator().setRequestedRange(0, getPageSize());
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

    private Registration dataProviderRegistration;

    /**
     * Creates a new {@code TreeGrid} without support for creating columns based
     * on property names. Use an alternative constructor, such as
     * {@link TreeGrid#TreeGrid(Class)}, to create a {@code TreeGrid} that
     * automatically sets up columns based on the type of presented data.
     */
    public TreeGrid() {
        super(50, TreeGridUpdateQueue::new,
                new TreeDataCommunicatorBuilder<T>());

        setUniqueKeyProperty("key");
        getArrayUpdater().getUpdateQueueData()
                .setHasExpandedItems(getDataCommunicator()::hasExpandedItems);
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
        super(beanType, TreeGridUpdateQueue::new,
                new TreeDataCommunicatorBuilder<T>());

        setUniqueKeyProperty("key");
        getArrayUpdater().getUpdateQueueData()
                .setHasExpandedItems(getDataCommunicator()::hasExpandedItems);
    }

    @Override
    protected GridArrayUpdater createDefaultArrayUpdater(
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory) {
        return new TreeGridArrayUpdaterImpl(updateQueueFactory);
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

    private static class TreeDataCommunicatorBuilder<T>
            extends DataCommunicatorBuilder<T, TreeGridArrayUpdater> {

        @Override
        protected DataCommunicator<T> build(Element element,
                CompositeDataGenerator<T> dataGenerator,
                TreeGridArrayUpdater arrayUpdater,
                SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {

            return new HierarchicalDataCommunicator<>(dataGenerator,
                    arrayUpdater,
                    data -> element.callJsFunction(
                            "$connector.updateHierarchicalData", data),
                    element.getNode(), uniqueKeyProviderSupplier);
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

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        if (!(dataProvider instanceof HierarchicalDataProvider)) {
            throw new IllegalArgumentException(
                    "TreeGrid only accepts hierarchical data providers. "
                            + "An example of interface to be used: HierarchicalDataProvider");
        }
        if (dataProviderRegistration != null) {
            dataProviderRegistration.remove();
        }
        dataProviderRegistration = dataProvider.addDataProviderListener(e -> {
            if (!(e instanceof DataChangeEvent.DataRefreshEvent)) {
                // refreshAll was called
                getElement().executeJs("$0.$connector && $0.$connector.reset()",
                        getElement());
            }
        });
        super.setDataProvider(dataProvider);
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
        Column<T> column = addColumn(TemplateRenderer
                .<T> of("<vaadin-grid-tree-toggle "
                        + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>[[item.name]]"
                        + "</vaadin-grid-tree-toggle>")
                .withProperty("leaf",
                        item -> !getDataCommunicator().hasChildren(item))
                .withProperty("name",
                        value -> String.valueOf(valueProvider.apply(value))));
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
        return addColumn(new HierarchyColumnComponentRenderer<V, T>(
                componentProvider).withProperty("leaf",
                        item -> !getDataCommunicator().hasChildren(item)));
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

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setParentRequestedRange(int start, int length,
            String parentKey) {
        T item = getDataCommunicator().getKeyMapper().get(parentKey);
        if (item != null) {
            getDataCommunicator().setParentRequestedRange(start, length, item);
        }
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setParentRequestedRanges(JsonArray array) {
        for (int index = 0; index < array.length(); index++) {
            JsonObject object = array.getObject(index);
            setParentRequestedRange((int) object.getNumber("firstIndex"),
                    (int) object.getNumber("size"),
                    object.getString("parentKey"));
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

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void confirmParentUpdate(int id, String parentKey) {
        getDataCommunicator().confirmUpdate(id, parentKey);
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
}

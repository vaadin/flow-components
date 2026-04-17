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
package com.vaadin.flow.component.breadcrumb;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.dom.Element;

/**
 * {@code HasBreadcrumbItems} is an interface for components that can contain
 * and manage {@link BreadcrumbItem} instances. The interface defines default
 * methods for adding, removing, and accessing the items within a breadcrumb
 * trail.
 *
 * @see Breadcrumb
 * @see BreadcrumbItem
 *
 * @author Vaadin Ltd
 */
public interface HasBreadcrumbItems extends HasElement {

    /**
     * Adds breadcrumb item(s) to the end of this breadcrumb trail.
     *
     * @param items
     *            the breadcrumb item(s) to add
     */
    default void addItem(BreadcrumbItem... items) {
        assert items != null;

        for (BreadcrumbItem item : items) {
            getElement().appendChild(item.getElement());
        }
    }

    /**
     * Adds the given breadcrumb item as the first child of this breadcrumb
     * trail.
     *
     * @param item
     *            the item to add, value must not be null
     */
    default void addItemAsFirst(BreadcrumbItem item) {
        addItemAtIndex(0, item);
    }

    /**
     * Adds the given item as child of this breadcrumb trail at the specific
     * index.
     *
     * @param index
     *            the index, where the item will be added. The index must be
     *            non-negative and may not exceed the children count
     * @param item
     *            the item to add, value must not be null
     */
    default void addItemAtIndex(int index, BreadcrumbItem item) {
        assert item != null;

        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a BreadcrumbItem with a negative index");
        }

        final List<BreadcrumbItem> items = getItems();

        if (index > items.size()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot insert item with index %d when there are %d items",
                    index, items.size()));
        }

        if (index == items.size()) {
            addItem(item);
        } else {
            int insertPosition = getElement()
                    .indexOfChild(items.get(index).getElement());
            getElement().insertChild(insertPosition, item.getElement());
        }
    }

    /**
     * Gets the breadcrumb items added to this breadcrumb trail (the children of
     * this component that are instances of {@link BreadcrumbItem}). This
     * doesn't include nested items.
     *
     * @return the child {@link BreadcrumbItem} instances in this breadcrumb
     *         trail
     * @see #addItem(BreadcrumbItem...)
     */
    default List<BreadcrumbItem> getItems() {
        return getElement().getChildren().map(Element::getComponent)
                .flatMap(Optional::stream)
                .filter(component -> component instanceof BreadcrumbItem)
                .map(component -> (BreadcrumbItem) component).toList();
    }

    /**
     * Removes the breadcrumb item(s) from the trail.
     * <p>
     * If the given item is not a child of this breadcrumb, does nothing.
     *
     * @param items
     *            the breadcrumb item(s) to remove
     */
    default void remove(BreadcrumbItem... items) {
        for (BreadcrumbItem item : items) {
            Optional<Component> parent = item.getParent();
            if (parent.isPresent() && parent.get() == this) {
                getElement().removeChild(item.getElement());
            }
        }
    }

    /**
     * Removes all breadcrumb items from this breadcrumb trail.
     */
    default void removeAll() {
        final List<Element> items = getItems().stream()
                .map(Component::getElement).toList();
        getElement().removeChild(items);
    }
}

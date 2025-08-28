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
package com.vaadin.flow.component.breadcrumb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.Element;

/**
 * Breadcrumb is a navigation component that shows a hierarchical path through
 * the application's structure, allowing users to navigate back to previous
 * levels.
 * <p>
 * Use Breadcrumb when you want to show the user's current location within a
 * hierarchy and provide navigation to parent levels.
 * <p>
 * {@link BreadcrumbItem} components can be added to this component with the
 * {@link #add(BreadcrumbItem...)} method or the
 * {@link #Breadcrumb(BreadcrumbItem...)} constructor.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-breadcrumb")
@JsModule("@vaadin/breadcrumb/src/vaadin-breadcrumb.js")
@NpmPackage(value = "@vaadin/breadcrumb", version = "25.0.0-alpha16")
public class Breadcrumb extends Component
        implements HasSize, HasStyle, HasThemeVariant<BreadcrumbVariant> {

    /**
     * Constructs an empty breadcrumb component.
     */
    public Breadcrumb() {
        super();
    }

    /**
     * Constructs a breadcrumb component with the given items.
     *
     * @param items
     *            the breadcrumb items to add
     */
    public Breadcrumb(BreadcrumbItem... items) {
        this();
        add(items);
    }

    /**
     * Adds the given breadcrumb items to the component.
     *
     * @param items
     *            the items to add, not {@code null}
     */
    public void add(BreadcrumbItem... items) {
        Objects.requireNonNull(items, "Items to add cannot be null");
        Arrays.stream(items)
                .map(item -> Objects.requireNonNull(item,
                        "Individual item to add cannot be null"))
                .map(BreadcrumbItem::getElement)
                .forEach(getElement()::appendChild);
    }

    /**
     * Removes the given breadcrumb items from the component.
     *
     * @param items
     *            the items to remove, not {@code null}
     */
    public void remove(BreadcrumbItem... items) {
        Objects.requireNonNull(items, "Items to remove cannot be null");
        Arrays.stream(items)
                .map(item -> Objects.requireNonNull(item,
                        "Individual item to remove cannot be null"))
                .map(BreadcrumbItem::getElement)
                .forEach(getElement()::removeChild);
    }

    /**
     * Removes all breadcrumb items from the component.
     */
    public void removeAll() {
        getElement().removeAllChildren();
    }

    /**
     * Gets the breadcrumb item at the given index.
     *
     * @param index
     *            the index of the item to get
     * @return the item at the given index
     * @throws IndexOutOfBoundsException
     *             if the index is out of range
     */
    public BreadcrumbItem getItemAt(int index) {
        if (index < 0 || index >= getItemCount()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + getItemCount());
        }
        Iterator<BreadcrumbItem> iterator = getItems().iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * Gets the number of breadcrumb items in the component.
     *
     * @return the number of items
     */
    public int getItemCount() {
        return (int) getItems().count();
    }

    /**
     * Gets the index of the given breadcrumb item.
     *
     * @param item
     *            the item to get the index of
     * @return the index of the item, or -1 if not found
     */
    public int indexOf(BreadcrumbItem item) {
        if (item == null) {
            return -1;
        }
        Iterator<BreadcrumbItem> iterator = getItems().iterator();
        int index = 0;
        while (iterator.hasNext()) {
            if (iterator.next().equals(item)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Replaces the breadcrumb item at the given index with a new item.
     *
     * @param index
     *            the index of the item to replace
     * @param newItem
     *            the new item to set, not {@code null}
     * @throws IndexOutOfBoundsException
     *             if the index is out of range
     */
    public void replace(int index, BreadcrumbItem newItem) {
        Objects.requireNonNull(newItem, "New item cannot be null");
        if (index < 0 || index >= getItemCount()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + getItemCount());
        }

        BreadcrumbItem oldItem = getItemAt(index);
        Element parentElement = getElement();
        List<Element> children = new ArrayList<>();
        parentElement.getChildren().forEach(children::add);

        int elementIndex = children.indexOf(oldItem.getElement());
        if (elementIndex >= 0) {
            parentElement.insertChild(elementIndex, newItem.getElement());
            parentElement.removeChild(oldItem.getElement());
        }
    }

    /**
     * Gets all breadcrumb items in the component as a stream.
     *
     * @return a stream of all items
     */
    public Stream<BreadcrumbItem> getItems() {
        return getElement().getChildren()
                .filter(element -> element.getComponent()
                        .filter(BreadcrumbItem.class::isInstance).isPresent())
                .map(element -> element.getComponent()
                        .map(BreadcrumbItem.class::cast).get());
    }
}

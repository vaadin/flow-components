/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.listbox;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.HasItemsAndComponents.ItemComponent;

/**
 * Server-side component for the {@code vaadin-item} element, used to represent
 * individual items in a {@link ListBox}.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            type of the item represented by this component
 */
@Tag("vaadin-item")
@HtmlImport("frontend://bower_components/vaadin-item/vaadin-item.html")
@NpmPackage(value = "@vaadin/vaadin-item", version = "2.3.0")
@JsModule("@vaadin/vaadin-item/src/vaadin-item.js")
class VaadinItem<T> extends Component
        implements ItemComponent<T>, HasComponents {

    private final T item;

    /**
     * Constructs the component with the given item rendered as a String.
     *
     * @param item
     *            the item to be displayed by this component
     */
    public VaadinItem(T item) {
        this.item = item;
    }

    @Override
    public T getItem() {
        return item;
    }

}

/**
 * Copyright (C) 2000-2023 Vaadin Ltd
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
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.HasItemComponents;

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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "22.1.0-alpha1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/item", version = "22.1.0-alpha1")
@NpmPackage(value = "@vaadin/vaadin-item", version = "22.1.0-alpha1")
@JsModule("@vaadin/item/src/vaadin-item.js")
class VaadinItem<T> extends Component
        implements HasItemComponents.ItemComponent<T>, HasComponents {

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

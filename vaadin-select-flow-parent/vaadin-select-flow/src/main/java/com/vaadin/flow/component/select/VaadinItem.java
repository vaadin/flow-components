/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.select;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.data.binder.HasItemsAndComponents;

/**
 * Internal representation of {@code <vaadin-item>}. vaadin-select.html imports
 * vaadin-item.html.
 *
 * @param <T>
 *            the type of the bean
 */
@Tag("vaadin-item")
class VaadinItem<T> extends Component
        implements HasItemsAndComponents.ItemComponent<T>, HasComponents,
        HasStyle, HasText {

    private T item;

    VaadinItem(String key, T item) {
        this.item = item;
        getElement().setProperty("value", key);
        getElement().setAttribute("value", key);
    }

    @Override
    public T getItem() {
        return item;
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        // Not setting the disabled attribute because vaadin-item's that are
        // disabled cannot be selected at the same time.
        // the Element.setEnabled(...) that calls this method will handle
        // the disabling of the state node and triggering disabled attribute
        // for any child items.
        // When an item is disabled with the item enabled provider, select will
        // add the disabled attribute.
    }
}

/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.HasItemsAndComponents.ItemComponent;

/**
 * Server-side component for the {@code vaadin-radio-button} element.
 *
 * @author Vaadin Ltd.
 */
@NpmPackage(value = "@vaadin/vaadin-radio-button", version = "1.6.3")
class RadioButton<T> extends GeneratedVaadinRadioButton<RadioButton<T>>
        implements ItemComponent<T>, HasComponents {

    private T item;

    RadioButton(String key, T item) {
        this.item = item;
        getElement().setProperty("value", key);
    }

    @Override
    public T getItem() {
        return item;
    }

}

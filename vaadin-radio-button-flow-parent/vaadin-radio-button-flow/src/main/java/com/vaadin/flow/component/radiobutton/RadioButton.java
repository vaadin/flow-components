/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.HasItemComponents;

/**
 * Server-side component for the {@code vaadin-radio-button} element.
 *
 * @author Vaadin Ltd.
 */
@NpmPackage(value = "@vaadin/radio-group", version = "22.1.0-alpha1")
@NpmPackage(value = "@vaadin/vaadin-radio-button", version = "22.1.0-alpha1")
class RadioButton<T> extends GeneratedVaadinRadioButton<RadioButton<T>>
        implements HasItemComponents.ItemComponent<T>, HasComponents {

    private T item;

    private final Label labelElement = appendLabelElement();

    RadioButton(String key, T item) {
        this.item = item;
        getElement().setProperty("value", key);
    }

    @Override
    public T getItem() {
        return item;
    }

    /**
     * Replaces the label content with the given label component.
     *
     * @param component
     *            the component to be added to the label.
     */
    public void setLabelComponent(Component component) {
        labelElement.removeAll();
        labelElement.add(component);
    }

    private Label appendLabelElement() {
        Label label = new Label();
        label.getElement().setAttribute("slot", "label");
        getElement().appendChild(label.getElement());
        return label;
    }
}

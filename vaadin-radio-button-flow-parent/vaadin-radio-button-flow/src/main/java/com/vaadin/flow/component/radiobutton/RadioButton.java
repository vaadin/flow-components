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
@NpmPackage(value = "@vaadin/radio-group", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-radio-button", version = "23.1.0-beta1")
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

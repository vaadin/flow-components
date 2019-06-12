/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.HasItemsAndComponents.ItemComponent;

/**
 * Server-side component for the {@code vaadin-radio-button} element.
 *
 * @author Vaadin Ltd.
 */
@NpmPackage(value = "@vaadin/vaadin-radio-button", version = "1.2.2")
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

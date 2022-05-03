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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/item", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-item", version = "23.1.0-beta1")
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

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
package com.vaadin.flow.component.select;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.data.binder.HasItemComponents;

/**
 * Internal representation of {@code <vaadin-item>}. vaadin-select.html imports
 * vaadin-item.html.
 * 
 * @param <T> the type of the bean
 */
@Tag("vaadin-item")
class VaadinItem<T> extends Component
        implements HasItemComponents.ItemComponent<T>, HasComponents,
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

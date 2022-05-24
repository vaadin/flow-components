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
package com.vaadin.flow.component.combobox.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.combobox.ComboBoxBase;

/**
 * Event that is dispatched from a combo box component, if the component allows
 * setting custom values, and the user has entered a non-empty value that does
 * not match any of the existing items
 *
 * @param <TComponent>
 *            The specific combo box component type
 */
@DomEvent("custom-value-set")
public class CustomValueSetEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
        extends ComponentEvent<TComponent> {
    private final String detail;

    public CustomValueSetEvent(TComponent source, boolean fromClient,
            @EventData("event.detail") String detail) {
        super(source, fromClient);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }
}

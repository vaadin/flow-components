/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import java.util.List;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * View used to verify that genuine user value commits (dropdown selection and
 * deselection, clear-button click, chip removal, Esc-clear) still propagate the
 * value to the server after the value sync was switched from the
 * {@code selected-items-changed} property notification to the {@code change}
 * event (see
 * <a href="https://github.com/vaadin/flow-components/issues/9611">#9611</a>).
 */
@Route("vaadin-multi-select-combo-box/user-value-change")
public class MultiSelectComboBoxUserValueChangePage extends Div {
    public MultiSelectComboBoxUserValueChangePage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        comboBox.setItems(List.of("Item 1", "Item 2", "Item 3"));
        comboBox.setWidth("500px");
        // Clear button must be visible for the clear-button and Esc-to-clear
        // user paths to clear the selection.
        comboBox.setClearButtonVisible(true);

        Span eventValue = new Span();
        eventValue.setId("event-value");
        Span eventOrigin = new Span();
        eventOrigin.setId("event-origin");
        Span eventCount = new Span("0");
        eventCount.setId("event-count");

        comboBox.addValueChangeListener(e -> {
            eventValue.setText(String.join(",", e.getValue()));
            eventOrigin.setText(e.isFromClient() ? "client" : "server");
            eventCount.setText(
                    String.valueOf(Integer.parseInt(eventCount.getText()) + 1));
        });

        add(comboBox);
        add(new Div(new Span("Event value: "), eventValue));
        add(new Div(new Span("Event origin: "), eventOrigin));
        add(new Div(new Span("Event count: "), eventCount));
    }
}

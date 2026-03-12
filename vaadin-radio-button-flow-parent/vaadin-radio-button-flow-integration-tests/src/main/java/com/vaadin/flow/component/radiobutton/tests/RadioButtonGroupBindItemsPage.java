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
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;

@Route("vaadin-radio-button/radio-button-group-bind-items")
public class RadioButtonGroupBindItemsPage extends Div {

    public static final String RADIO_GROUP_ID = "test-radio-group";
    public static final String ADD_ITEM_BUTTON = "add-item-button";
    public static final String REMOVE_ITEM_BUTTON = "remove-item-button";
    public static final String ITEM_COUNT_SPAN = "item-count";
    public static final String SELECTED_VALUE_SPAN = "selected-value";

    private final ListSignal<String> itemsSignal;

    public RadioButtonGroupBindItemsPage() {
        // Create list signal and add initial items
        itemsSignal = new ListSignal<>();
        itemsSignal.insertLast("Option 1");
        itemsSignal.insertLast("Option 2");
        itemsSignal.insertLast("Option 3");

        // Create and configure radio button group
        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setId(RADIO_GROUP_ID);
        radioGroup.setLabel("Select an option");
        radioGroup.bindItems(itemsSignal);

        // Item count display
        Span itemCount = new Span(String.valueOf(itemsSignal.peek().size()));
        itemCount.setId(ITEM_COUNT_SPAN);

        // Selected value display
        Span selectedValue = new Span("None");
        selectedValue.setId(SELECTED_VALUE_SPAN);
        radioGroup.addValueChangeListener(event -> {
            selectedValue.setText(
                    event.getValue() != null ? event.getValue() : "None");
        });

        // Button to add a new item
        NativeButton addItemButton = new NativeButton("Add Item", event -> {
            int currentSize = itemsSignal.peek().size();
            itemsSignal.insertLast("Option " + (currentSize + 1));

            // Update display
            itemCount.setText(String.valueOf(itemsSignal.peek().size()));
        });
        addItemButton.setId(ADD_ITEM_BUTTON);

        // Button to remove the last item
        NativeButton removeItemButton = new NativeButton("Remove Last Item",
                event -> {
                    var currentItems = itemsSignal.peek();
                    if (!currentItems.isEmpty()) {
                        itemsSignal.remove(currentItems.getLast());

                        // Update display
                        itemCount.setText(
                                String.valueOf(itemsSignal.peek().size()));
                    }
                });
        removeItemButton.setId(REMOVE_ITEM_BUTTON);

        add(radioGroup, new Div(new Span("Item Count: "), itemCount),
                new Div(new Span("Selected Value: "), selectedValue),
                addItemButton, removeItemButton);
    }
}

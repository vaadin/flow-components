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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.test.entity.Person;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * Test page for ComboBox bind items functionality demonstrating reactive signal
 * binding.
 */
@Route("vaadin-combo-box/combo-box-bind-items")
public class ComboBoxBindItemsPage extends Div {

    public static final String COMBO_BOX_ID = "test-combo-box";
    public static final String ADD_ITEM_BUTTON = "add-item-button";
    public static final String REMOVE_ITEM_BUTTON = "remove-item-button";
    public static final String UPDATE_FIRST_ITEM_BUTTON = "update-first-item-button";
    public static final String ITEM_COUNT_SPAN = "item-count";
    public static final String SELECTED_VALUE_SPAN = "selected-value";

    private final ListSignal<Person> itemsSignal = new ListSignal<>();

    public ComboBoxBindItemsPage() {
        // Create a list signal with explicitly created ValueSignals for each
        // item
        itemsSignal.insertLast(createPerson(1, "Alice", "Smith"));
        itemsSignal.insertLast(createPerson(2, "Bob", "Johnson"));
        itemsSignal.insertLast(createPerson(3, "Charlie", "Brown"));

        // Create and configure combo box
        ComboBox<Person> comboBox = new ComboBox<>("Select Person");
        comboBox.setId(COMBO_BOX_ID);
        comboBox.setItemLabelGenerator(Person::toString);

        // Bind items signal with filter converter
        comboBox.bindItems(itemsSignal, filterText -> person -> person
                .toString().toLowerCase().contains(filterText.toLowerCase()));

        // Item count display
        Span itemCount = new Span(String.valueOf(itemsSignal.peek().size()));
        itemCount.setId(ITEM_COUNT_SPAN);

        // Selected value display
        Span selectedValue = new Span("None");
        selectedValue.setId(SELECTED_VALUE_SPAN);
        comboBox.addValueChangeListener(event -> {
            Person value = event.getValue();
            selectedValue.setText(value != null ? value.toString() : "None");
        });

        // Button to add a new item
        Button addItemButton = new Button("Add Item", event -> {
            int currentSize = itemsSignal.peek().size();
            itemsSignal.insertLast(
                    createPerson(currentSize + 1, "Person", "Lastname"));

            // Update display
            itemCount.setText(String.valueOf(itemsSignal.peek().size()));
        });
        addItemButton.setId(ADD_ITEM_BUTTON);

        // Button to remove the last item
        Button removeItemButton = new Button("Remove Last Item", event -> {
            var currentItems = itemsSignal.peek();
            if (!currentItems.isEmpty()) {
                itemsSignal.remove(currentItems.getLast());

                // Update display
                itemCount.setText(String.valueOf(itemsSignal.peek().size()));
            }
        });
        removeItemButton.setId(REMOVE_ITEM_BUTTON);

        // Button to update the first item
        Button updateFirstItemButton = new Button("Update First Item",
                event -> {
                    ValueSignal<Person> aliceSignal = itemsSignal.peek()
                            .getFirst();
                    // Update Alice's signal directly
                    Person updatedPerson = createPerson(1, "Alice Updated",
                            "Smith");
                    aliceSignal.set(updatedPerson);
                });
        updateFirstItemButton.setId(UPDATE_FIRST_ITEM_BUTTON);

        add(comboBox, new Div(new Span("Item Count: "), itemCount),
                new Div(new Span("Selected: "), selectedValue), addItemButton,
                removeItemButton, updateFirstItemButton);
    }

    private Person createPerson(int id, String firstName, String lastName) {
        Person person = new Person();
        person.setId(id);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        return person;
    }
}

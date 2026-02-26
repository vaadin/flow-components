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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for ListBox component.
 */
@Route(value = "list-box", layout = MainLayout.class)
@PageTitle("List Box | Vaadin Kitchen Sink")
public class ListBoxDemoView extends VerticalLayout {

    public ListBoxDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("List Box Component"));
        add(new Paragraph("ListBox displays a list of selectable items."));

        // Basic list box
        ListBox<String> basic = new ListBox<>();
        basic.setItems("Option 1", "Option 2", "Option 3", "Option 4", "Option 5");
        basic.addValueChangeListener(e ->
            Notification.show("Selected: " + e.getValue()));
        addSection("Basic List Box", basic);

        // Pre-selected value
        ListBox<String> preSelected = new ListBox<>();
        preSelected.setItems("Apple", "Banana", "Cherry", "Date", "Elderberry");
        preSelected.setValue("Cherry");
        addSection("Pre-selected Value", preSelected);

        // Multi-select list box
        MultiSelectListBox<String> multiSelect = new MultiSelectListBox<>();
        multiSelect.setItems("Java", "JavaScript", "Python", "TypeScript", "Go", "Rust");
        multiSelect.addValueChangeListener(e ->
            Notification.show("Selected: " + e.getValue()));
        addSection("Multi-Select List Box", multiSelect);

        // Pre-selected multi-select
        MultiSelectListBox<String> preSelectedMulti = new MultiSelectListBox<>();
        preSelectedMulti.setItems("Red", "Green", "Blue", "Yellow", "Orange", "Purple");
        preSelectedMulti.select("Red", "Blue");
        addSection("Pre-selected Multi-Select", preSelectedMulti);

        // With disabled items
        ListBox<String> withDisabled = new ListBox<>();
        withDisabled.setItems("Available 1", "Unavailable", "Available 2", "Also Unavailable", "Available 3");
        withDisabled.setItemEnabledProvider(item -> !item.contains("Unavailable"));
        addSection("With Disabled Items", withDisabled);

        // Read-only
        ListBox<String> readonly = new ListBox<>();
        readonly.setItems("Item A", "Item B", "Item C");
        readonly.setValue("Item B");
        readonly.setReadOnly(true);
        addSection("Read-only", readonly);

        // Disabled
        ListBox<String> disabled = new ListBox<>();
        disabled.setItems("Disabled 1", "Disabled 2", "Disabled 3");
        disabled.setValue("Disabled 1");
        disabled.setEnabled(false);
        addSection("Disabled List Box", disabled);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
